import { Component, OnInit, OnDestroy, AfterViewInit,
         ChangeDetectorRef, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Subscription, forkJoin, of, interval, switchMap, catchError } from 'rxjs';
import { timeout } from 'rxjs/operators';
import { GeofencingService, Alert, Zone, PatientPosition } from '../shared/geofencing.service';
import { AuthService } from '../../auth/services/auth.service';
import { User } from '../../auth/models/user.model';
import jsPDF from 'jspdf';

interface PatientStats {
    patient:      User;
    zone:         Zone | null;
    lastPosition: PatientPosition | null;
    activeAlerts: number;
    totalAlerts:  number;
    safetyScore:  number;
    status:       'Online' | 'Offline' | 'Alert';
    lastSeen:     Date | null;
}

@Component({
    selector: 'app-supervision-dashboard',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './supervision-dashboard.component.html',
    styleUrl:    './supervision-dashboard.component.css'
})
export class SupervisionDashboardComponent implements OnInit, OnDestroy, AfterViewInit {

    // ── User & role ───────────────────────────────────────────────
    userId      = '';
    userRole    = '';
    get isPatient():  boolean { return this.userRole === 'PATIENT_PROFILE'; }
    get isSoignant(): boolean {
        return this.userRole === 'CARE_OWNER' || this.userRole === 'DOCTOR_PROFILE';
    }

    // ── Soignant data ─────────────────────────────────────────────
    stats = { activeMonitoring: 0, safePatients: 0, riskWarnings: 0, totalAlerts24h: 0 };
    patientStats:  PatientStats[] = [];
    alertsByType:  { label: string; count: number; heightPx: number; pct: number }[] = [];
    alertsByDay:   { date: string;  count: number; heightPx: number }[] = [];
    activeTab:     'overview' | 'patients' = 'overview';
    exportInProgress = false;

    // ── Patient data ──────────────────────────────────────────────
    myZone:        Zone | null = null;
    myAlerts:      Alert[]     = [];
    totalAlerts    = 0;
    activeAlerts   = 0;
    resolvedAlerts = 0;
    safetyScore    = 95;
    patientName    = '';

    // ── Map (patient) ─────────────────────────────────────────────
    private map:     any;
    private L:       any;
    private mapReady = false;
    isBrowser:       boolean;

    // ── Common ────────────────────────────────────────────────────
    isLoading = true;
    errorMsg  = '';

    private sub!:     Subscription;
    private pollSub!: Subscription;

    constructor(
        private geofencingService: GeofencingService,
        private authService:       AuthService,
        private cdr:               ChangeDetectorRef,
        @Inject(PLATFORM_ID) platformId: Object
    ) {
        this.isBrowser  = isPlatformBrowser(platformId);
        const user      = this.authService.getCurrentUser();
        this.userId     = user?.id   ?? '';
        this.userRole   = (user?.role as string) ?? '';
        this.patientName = user ? `${user.prenom} ${user.nom}` : '';
    }

    ngOnInit(): void {
        this.loadData();
        this.pollSub = interval(30000).pipe(
            switchMap(() => this.buildData$())
        ).subscribe(data => { this.applyData(data); this.cdr.detectChanges(); });
    }

    async ngAfterViewInit(): Promise<void> {
        if (!this.isBrowser || !this.isPatient) return;
        this.L = await import('leaflet');
        setTimeout(() => this.initMap(), 300);
    }

    ngOnDestroy(): void {
        this.sub?.unsubscribe();
        this.pollSub?.unsubscribe();
        this.map?.remove();
    }

    // ─────────────────────────────────────────────────────────────
    loadData(): void {
        this.isLoading = true;
        this.errorMsg  = '';
        this.sub = this.buildData$().subscribe({
            next:  (data) => { this.applyData(data); this.isLoading = false; this.cdr.detectChanges(); },
            error: ()     => { this.errorMsg = 'Erreur de chargement.'; this.isLoading = false; this.cdr.detectChanges(); }
        });
    }

    private buildData$() {
        if (this.isPatient) {
            return forkJoin({
                zones:    this.geofencingService.getZonesByPatient(this.userId)
                              .pipe(timeout(8000), catchError(() => of([] as Zone[]))),
                alerts:   this.geofencingService.getAlertsByPatient(this.userId)
                              .pipe(timeout(8000), catchError(() => of([] as Alert[]))),
                patients: of([] as User[]),
                positions:of([] as PatientPosition[])
            });
        }
        // Soignant
        return forkJoin({
            zones:     this.geofencingService.getZonesBySoignant(this.userId)
                           .pipe(timeout(8000), catchError(() => of([] as Zone[]))),
            alerts:    this.geofencingService.getAlertsBySoignant(this.userId)
                           .pipe(timeout(8000), catchError(() => of([] as Alert[]))),
            patients:  this.authService.getPatients()
                           .pipe(timeout(8000), catchError(() => of([] as User[]))),
            positions: this.geofencingService.getAllLastPositions()
                           .pipe(timeout(8000), catchError(() => of([] as PatientPosition[])))
        });
    }

    private applyData({ zones, alerts, patients, positions }: {
        zones: Zone[]; alerts: Alert[]; patients: User[]; positions: PatientPosition[];
    }): void {
        if (this.isPatient) {
            this.applyPatientData(zones, alerts);
        } else {
            this.applySoignantData(zones, alerts, patients, positions);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  VUE PATIENT
    // ─────────────────────────────────────────────────────────────
    private applyPatientData(zones: Zone[], alerts: Alert[]): void {
        this.myZone        = zones[0] ?? null;
        this.myAlerts      = [...alerts].sort((a, b) =>
            this.formatDate(b.timestamp).getTime() - this.formatDate(a.timestamp).getTime()
        );
        this.totalAlerts    = alerts.length;
        this.activeAlerts   = alerts.filter(a => a.status === 'Active').length;
        this.resolvedAlerts = alerts.filter(a => a.status === 'Resolved').length;
        this.safetyScore    = this.activeAlerts === 0 ? 95 : this.activeAlerts === 1 ? 60 : 30;

        if (this.mapReady) this.drawZone();
    }

    private initMap(): void {
        const container = document.getElementById('patient-zone-map');
        if (!container || !this.L) return;
        this.map = this.L.map('patient-zone-map').setView([36.8065, 10.1815], 14);
        this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap'
        }).addTo(this.map);
        this.mapReady = true;
        this.drawZone();
    }

    private drawZone(): void {
        if (!this.map || !this.L || !this.myZone) return;
        this.map.eachLayer((l: any) => { if (!l._url) this.map.removeLayer(l); });
        const color = this.myZone.type === 'DANGER' ? '#e53935' : '#1565C0';
        this.L.circle([this.myZone.centreLat, this.myZone.centreLon], {
            radius: this.myZone.rayon, color, fillColor: color,
            fillOpacity: 0.15, weight: 2.5,
            dashArray: this.myZone.type === 'DANGER' ? '7,5' : undefined
        }).bindPopup(`<b>${this.myZone.nomZone}</b><br>${this.myZone.type} — ${this.myZone.rayon}m`)
          .addTo(this.map);
        this.map.setView([this.myZone.centreLat, this.myZone.centreLon], 15);
    }

    get scoreColor(): string {
        if (this.safetyScore >= 80) return '#388e3c';
        if (this.safetyScore >= 50) return '#fb8c00';
        return '#e53935';
    }

    get recentAlerts(): Alert[] { return this.myAlerts.slice(0, 5); }

    // ─────────────────────────────────────────────────────────────
    //  VUE SOIGNANT
    // ─────────────────────────────────────────────────────────────
    private applySoignantData(zones: Zone[], alerts: Alert[], patients: User[], positions: PatientPosition[]): void {
        const now     = new Date();
        const last24h = alerts.filter(a => (now.getTime() - this.formatDate(a.timestamp).getTime()) < 86400000).length;

        this.stats = {
            activeMonitoring: zones.length,
            safePatients:     zones.filter(z => z.type === 'SAFE').length,
            riskWarnings:     alerts.filter(a => a.status === 'Active').length,
            totalAlerts24h:   last24h
        };

        // Patient stats
        const linkedIds      = [...new Set(zones.map(z => z.patientId))];
        const linkedPatients = patients.filter(p => linkedIds.includes(p.id));

        this.patientStats = linkedPatients.map(patient => {
            const zone        = zones.find(z => z.patientId === patient.id) ?? null;
            const lastPos     = positions.find(p => p.patientId === patient.id) ?? null;
            const patAlerts   = alerts.filter(a => a.patientId === patient.id);
            const activeCount = patAlerts.filter(a => a.status === 'Active').length;
            let   status: 'Online' | 'Offline' | 'Alert' = 'Offline';
            if (activeCount > 0) {
                status = 'Alert';
            } else if (lastPos && (now.getTime() - this.formatDate(lastPos.timestamp).getTime()) < 600000) {
                status = 'Online';
            }
            const safetyScore = activeCount === 0 ? 92 : activeCount === 1 ? 50 : 20;
            return { patient, zone, lastPosition: lastPos, activeAlerts: activeCount,
                     totalAlerts: patAlerts.length, safetyScore, status,
                     lastSeen: lastPos ? this.formatDate(lastPos.timestamp) : null };
        });

        this.buildAlertsByType(alerts);
        this.buildAlertsByDay(alerts);
    }

    private buildAlertsByType(alerts: Alert[]): void {
        const map: Record<string, number> = {};
        alerts.forEach(a => { map[a.type] = (map[a.type] || 0) + 1; });
        const max   = Math.max(...Object.values(map), 1);
        const total = alerts.length || 1;
        this.alertsByType = Object.entries(map).sort((a,b) => b[1]-a[1]).map(([type, count]) => ({
            label: this.getTypeLabel(type), count,
            pct:      Math.round(count/total*100),
            heightPx: Math.max(Math.round((count/max)*140), 6)
        }));
    }

    private buildAlertsByDay(alerts: Alert[]): void {
        const map: Record<string, number> = {};
        for (let i = 6; i >= 0; i--) {
            const d = new Date(); d.setDate(d.getDate()-i);
            const k = `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}`;
            map[k] = 0;
        }
        alerts.forEach(a => {
            const d = this.formatDate(a.timestamp);
            const k = `${String(d.getDate()).padStart(2,'0')}/${String(d.getMonth()+1).padStart(2,'0')}`;
            if (k in map) map[k]++;
        });
        const max = Math.max(...Object.values(map), 1);
        this.alertsByDay = Object.entries(map).map(([date, count]) => ({
            date, count, heightPx: Math.max(Math.round((count/max)*140), 4)
        }));
    }

    setTab(tab: 'overview' | 'patients'): void { this.activeTab = tab; }

    get onlineCount():   number { return this.patientStats.filter(p => p.status==='Online').length; }
    get alertPatients(): number { return this.patientStats.filter(p => p.status==='Alert').length; }

    // ─────────────────────────────────────────────────────────────
    //  Export PDF (soignant)
    // ─────────────────────────────────────────────────────────────
    exportReport(): void {
        this.exportInProgress = true;
        try {
            const pdf  = new jsPDF('p','mm','a4');
            const W    = pdf.internal.pageSize.getWidth();
            const H    = pdf.internal.pageSize.getHeight();
            const mL   = 14;
            const date = new Date().toLocaleDateString('fr-FR', { day:'2-digit', month:'long', year:'numeric' });

            pdf.setFillColor(142,68,173); pdf.rect(0,0,W,30,'F');
            pdf.setTextColor(255,255,255);
            pdf.setFontSize(20); pdf.setFont('helvetica','bold');
            pdf.text('Fakarni — Rapport de Supervision', mL, 18);
            pdf.setFontSize(10); pdf.setFont('helvetica','normal');
            pdf.text(`Généré le ${date}`, W-mL, 25, { align:'right' });

            let y = 42;
            pdf.setTextColor(50,50,50); pdf.setFontSize(13); pdf.setFont('helvetica','bold');
            pdf.text('Statistiques Globales', mL, y); y += 6;

            const cards = [
                { label:'Zones actives',   value:this.stats.activeMonitoring, rgb:[142,68,173] },
                { label:'Zones SAFE',      value:this.stats.safePatients,     rgb:[39,174,96]  },
                { label:'Alertes actives', value:this.stats.riskWarnings,     rgb:[231,76,60]  },
                { label:'Alertes (24h)',   value:this.stats.totalAlerts24h,   rgb:[52,152,219] }
            ];
            const cW = (W-mL*2-9)/4;
            cards.forEach((c,i) => {
                const x = mL+i*(cW+3);
                pdf.setFillColor(c.rgb[0],c.rgb[1],c.rgb[2]);
                pdf.roundedRect(x,y,cW,22,3,3,'F');
                pdf.setTextColor(255,255,255);
                pdf.setFontSize(20); pdf.setFont('helvetica','bold');
                pdf.text(String(c.value), x+cW/2, y+13, {align:'center'});
                pdf.setFontSize(8); pdf.setFont('helvetica','normal');
                pdf.text(c.label, x+cW/2, y+19, {align:'center'});
            });
            y += 30;

            pdf.setTextColor(50,50,50); pdf.setFontSize(13); pdf.setFont('helvetica','bold');
            pdf.text('Suivi des Patients', mL, y); y += 5;
            pdf.setFillColor(142,68,173); pdf.rect(mL,y,W-mL*2,8,'F');
            pdf.setTextColor(255,255,255); pdf.setFontSize(9); pdf.setFont('helvetica','bold');
            ['Patient','Zone','Statut','Score','Alertes','Dernière position'].forEach((h,i) => {
                pdf.text(h, [mL+2,mL+45,mL+85,mL+105,mL+125,mL+145][i], y+5.5);
            });
            y += 8;

            this.patientStats.forEach((ps,idx) => {
                if (y > H-30) { pdf.addPage(); y = 20; }
                if (idx%2===0) { pdf.setFillColor(248,240,255); pdf.rect(mL,y,W-mL*2,8,'F'); }
                pdf.setTextColor(50,50,50); pdf.setFontSize(9); pdf.setFont('helvetica','normal');
                pdf.text(`${ps.patient.prenom} ${ps.patient.nom}`, mL+2, y+5.5);
                pdf.text(ps.zone?.nomZone ?? '—', mL+45, y+5.5);
                const stC = ps.status==='Online'?[39,174,96]:ps.status==='Alert'?[231,76,60]:[150,150,150];
                pdf.setFillColor(stC[0],stC[1],stC[2]); pdf.roundedRect(mL+83,y+1,18,6,2,2,'F');
                pdf.setTextColor(255,255,255); pdf.setFontSize(7);
                pdf.text(ps.status, mL+92, y+5.5, {align:'center'});
                pdf.setTextColor(50,50,50); pdf.setFontSize(9);
                pdf.text(`${ps.safetyScore}%`, mL+105, y+5.5);
                pdf.text(String(ps.totalAlerts), mL+125, y+5.5);
                pdf.text(ps.lastSeen ? ps.lastSeen.toLocaleTimeString('fr-FR') : '—', mL+145, y+5.5);
                y += 8;
            });

            const pages = pdf.getNumberOfPages();
            for (let i = 1; i <= pages; i++) {
                pdf.setPage(i);
                pdf.setDrawColor(200,200,200); pdf.line(mL,H-12,W-mL,H-12);
                pdf.setTextColor(150,150,150); pdf.setFontSize(8);
                pdf.text('Fakarni — Système de géolocalisation patient', mL, H-6);
                pdf.text(`Page ${i}/${pages}`, W-mL, H-6, {align:'right'});
            }
            pdf.save(`Fakarni_Rapport_${new Date().toISOString().split('T')[0]}.pdf`);
        } catch(e) { console.error('PDF error:', e); }
        finally    { this.exportInProgress = false; }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers communs
    // ─────────────────────────────────────────────────────────────
    formatDate(ts: any): Date {
        if (!ts) return new Date();
        if (Array.isArray(ts)) { const [y,m,d,h,min,s]=ts; return new Date(y,m-1,d,h,min,s??0); }
        return new Date(ts);
    }

    getTypeLabel(type: string): string {
        const map: Record<string,string> = {
            'SORTIE_ZONE_SAFE':   'Sortie zone sécurisée',
            'ENTREE_ZONE_DANGER': 'Entrée zone dangereuse',
            'SORTIE_ZONE_DANGER': 'Sortie zone dangereuse',
            'ENTREE_ZONE_SAFE':   'Entrée zone sécurisée'
        };
        return map[type] || type;
    }

    getStatusClass(s: string): string {
        return { 'Online':'online', 'Offline':'offline', 'Alert':'alert' }[s] ?? '';
    }

    getSeverityClass(s: string): string {
        return { 'High':'sev-high', 'Medium':'sev-medium', 'Low':'sev-low' }[s] ?? '';
    }

    timeSince(date: Date | null): string {
        if (!date) return '—';
        const diff = Math.floor((Date.now()-date.getTime())/1000);
        if (diff < 60)   return `il y a ${diff}s`;
        if (diff < 3600) return `il y a ${Math.floor(diff/60)}min`;
        return `il y a ${Math.floor(diff/3600)}h`;
    }
}