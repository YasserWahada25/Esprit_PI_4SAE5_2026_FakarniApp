import { Component, AfterViewInit, OnDestroy, ChangeDetectorRef,
         Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { interval, Subscription, switchMap, forkJoin, of, catchError } from 'rxjs';
import { GeofencingService, Zone, Alert, PatientPosition } from '../shared/geofencing.service';
import { AuthService } from '../../auth/services/auth.service';
import { User } from '../../auth/models/user.model';

@Component({
    selector: 'app-live-tracking',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './live-tracking.component.html',
    styleUrl: './live-tracking.component.css'
})
export class LiveTrackingComponent implements AfterViewInit, OnDestroy {

    // ── User ──────────────────────────────────────────────────────
    userId   = '';
    userRole = '';
    get isPatient():  boolean { return this.userRole === 'PATIENT_PROFILE'; }
    get isSoignant(): boolean {
        return this.userRole === 'CARE_OWNER' || this.userRole === 'DOCTOR_PROFILE';
    }

    // ── Data ──────────────────────────────────────────────────────
    zones:            Zone[]            = [];
    activeAlerts:     Alert[]           = [];
    patientPositions: PatientPosition[] = [];
    myZone:           Zone | null       = null;
    patients:         User[]            = [];
    isLoading = true;

    // ── GPS (patient) ─────────────────────────────────────────────
    gpsStatus: 'idle' | 'requesting' | 'active' | 'denied' = 'idle';
    currentLat  = 0;
    currentLon  = 0;
    accuracy    = 0;
    sosTrigering = false;
    sosSuccess   = false;

    // ── Map ───────────────────────────────────────────────────────
    private map:      any;
    private L:        any;
    private mapReady  = false;
    private myMarker: any;
    private zoneCircles    = new Map<number, any>();
    private patientMarkers = new Map<string, any>();
    private tempMarker:    any = null;

    // ── CRUD Modal (caregiver) ────────────────────────────────────
    showModal       = false;
    isEditMode      = false;
    editingZoneId:  number | null = null;
    pickingLocation = false;
    formError       = '';
    isSaving        = false;
    errors: { [key: string]: string } = {};
    zoneForm: Partial<Zone> = {
        nomZone: '', patientId: '',
        centreLat: null as any, centreLon: null as any,
        rayon: 200, type: 'SAFE'
    };

    private gpsSub!:  Subscription;
    private pollSub!: Subscription;

    constructor(
        private geofencingService: GeofencingService,
        private authService:       AuthService,
        private cdr:               ChangeDetectorRef,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {
        const user    = this.authService.getCurrentUser();
        this.userId   = user?.id   ?? '';
        this.userRole = (user?.role as string) ?? '';
    }

    // ─────────────────────────────────────────────────────────────
    async ngAfterViewInit(): Promise<void> {
        if (!isPlatformBrowser(this.platformId)) return;

        this.L = await import('leaflet');

        // Wait for DOM to render based on role
        setTimeout(() => {
            this.initMap();
            this.mapReady = true;

            if (this.isPatient)  this.initPatientView();
            if (this.isSoignant) this.initSoignantView();
        }, 150);
    }

    ngOnDestroy(): void {
        this.gpsSub?.unsubscribe();
        this.pollSub?.unsubscribe();
        this.geofencingService.stopTracking();
        this.map?.remove();
    }

    // ─────────────────────────────────────────────────────────────
    //  MAP INIT
    // ─────────────────────────────────────────────────────────────
    private initMap(): void {
        const mapId    = this.isPatient ? 'map-patient' : 'map';
        const container = document.getElementById(mapId);
        if (!container) {
            console.error('Map container not found:', mapId);
            return;
        }

        this.map = this.L.map(mapId).setView([36.8065, 10.1815], 14);
        this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
        }).addTo(this.map);

        this.map.on('click', (e: any) => {
            if (!this.pickingLocation) return;
            this.zoneForm.centreLat = parseFloat(e.latlng.lat.toFixed(6));
            this.zoneForm.centreLon = parseFloat(e.latlng.lng.toFixed(6));
            if (this.tempMarker) this.tempMarker.remove();
            this.tempMarker = this.L.marker([e.latlng.lat, e.latlng.lng])
                .bindPopup('✅ Center selected').addTo(this.map).openPopup();
            this.pickingLocation = false;
            this.showModal       = true;
            this.cdr.detectChanges();
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  PATIENT VIEW
    // ─────────────────────────────────────────────────────────────
    private initPatientView(): void {
        this.geofencingService.getZonesByPatient(this.userId)
            .pipe(catchError(() => of([] as Zone[])))
            .subscribe(zones => {
                this.zones  = zones;
                this.myZone = zones[0] ?? null;
                this.isLoading = false;
                if (this.myZone) {
                    this.addZoneToMap(this.myZone);
                    this.map?.setView([this.myZone.centreLat, this.myZone.centreLon], 15);
                }
                this.cdr.detectChanges();
            });

        this.geofencingService.getAlertsByPatient(this.userId)
            .pipe(catchError(() => of([] as Alert[])))
            .subscribe(alerts => {
                this.activeAlerts = alerts.filter(a => a.status === 'Active');
                this.cdr.detectChanges();
            });

        this.startGPS();
    }

    private startGPS(): void {
        this.gpsStatus = 'requesting';
        this.gpsSub = this.geofencingService.startTracking(this.userId).subscribe({
            next: (pos) => {
                this.gpsStatus  = 'active';
                this.currentLat = pos.coords.latitude;
                this.currentLon = pos.coords.longitude;
                this.accuracy   = Math.round(pos.coords.accuracy);
                this.updateMyMarker(pos.coords.latitude, pos.coords.longitude);
                this.cdr.detectChanges();
            },
            error: () => { this.gpsStatus = 'denied'; this.cdr.detectChanges(); }
        });
    }

    private updateMyMarker(lat: number, lon: number): void {
        if (!this.map || !this.L) return;
        const icon = this.L.divIcon({
            className: '',
            html: `<div style="position:relative;width:20px;height:20px">
                     <div style="position:absolute;inset:0;background:rgba(56,142,60,.25);border-radius:50%;animation:safeRipple 2s ease-out infinite"></div>
                     <div style="position:absolute;inset:4px;background:#388e3c;border-radius:50%;border:2px solid white;box-shadow:0 1px 4px rgba(0,0,0,.3)"></div>
                   </div>`,
            iconSize: [20, 20], iconAnchor: [10, 10]
        });
        if (this.myMarker) {
            this.myMarker.setLatLng([lat, lon]);
        } else {
            this.myMarker = this.L.marker([lat, lon], { icon })
                .bindPopup('<b>📍 My location</b>').addTo(this.map);
            this.map.setView([lat, lon], 16, { animate: true });
        }
    }

    triggerSos(): void {
        if (!this.myZone?.soignantId || this.sosTrigering) return;
        this.sosTrigering = true;
        this.geofencingService.triggerSos(
            this.userId, this.myZone.soignantId, this.currentLat, this.currentLon
        ).subscribe({
            next: () => {
                this.sosTrigering = false;
                this.sosSuccess   = true;
                setTimeout(() => { this.sosSuccess = false; this.cdr.detectChanges(); }, 4000);
                this.cdr.detectChanges();
            },
            error: () => { this.sosTrigering = false; this.cdr.detectChanges(); }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  CAREGIVER VIEW
    // ─────────────────────────────────────────────────────────────
    private initSoignantView(): void {
        this.loadPatients();
        this.loadAll();

        this.pollSub = interval(5000).pipe(
            switchMap(() => forkJoin({
                zones:     this.geofencingService.getZonesBySoignant(this.userId)
                               .pipe(catchError(() => of(this.zones))),
                positions: this.geofencingService.getAllLastPositions()
                               .pipe(catchError(() => of(this.patientPositions))),
                alerts:    this.geofencingService.getAlertsBySoignant(this.userId)
                               .pipe(catchError(() => of([] as Alert[])))
            }))
        ).subscribe(({ zones, positions, alerts }) => {
            this.zones            = zones;
            this.patientPositions = positions;
            this.activeAlerts     = alerts.filter(a => a.status === 'Active');
            this.redrawZones();
            this.refreshAllMarkers();
            this.cdr.detectChanges();
        });
    }

    private loadPatients(): void {
        this.authService.getPatients().subscribe({
            next:  (p) => { this.patients = p; },
            error: (e) => console.error('Failed to load patients', e)
        });
    }

    private loadAll(): void {
        this.isLoading = true;
        forkJoin({
            zones:     this.geofencingService.getZonesBySoignant(this.userId)
                           .pipe(catchError(() => of([] as Zone[]))),
            positions: this.geofencingService.getAllLastPositions()
                           .pipe(catchError(() => of([] as PatientPosition[]))),
            alerts:    this.geofencingService.getAlertsBySoignant(this.userId)
                           .pipe(catchError(() => of([] as Alert[])))
        }).subscribe(({ zones, positions, alerts }) => {
            this.zones            = zones;
            this.patientPositions = positions;
            this.activeAlerts     = alerts.filter(a => a.status === 'Active');
            this.isLoading        = false;
            this.redrawZones();
            this.refreshAllMarkers();
            this.cdr.detectChanges();
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  MAP HELPERS
    // ─────────────────────────────────────────────────────────────
    private redrawZones(): void {
        if (!this.mapReady) return;
        this.zoneCircles.forEach(c => c.remove());
        this.zoneCircles.clear();
        this.zones.forEach(z => this.addZoneToMap(z));
    }

    addZoneToMap(zone: Zone): void {
        if (!this.L || !this.map) return;
        const isDanger = zone.type === 'DANGER';
        const color    = isDanger ? '#e53935' : '#1565C0';
        const circle   = this.L.circle([zone.centreLat, zone.centreLon], {
            radius: zone.rayon, color, fillColor: color,
            fillOpacity: 0.12, weight: 2.5,
            dashArray: isDanger ? '7,5' : undefined
        }).bindPopup(`
            <div style="min-width:160px;font-family:sans-serif">
                <b style="color:${color}">📍 ${zone.nomZone}</b><br>
                <small>Patient: <b>${zone.patientId}</b></small><br>
                <small>Radius: <b>${zone.rayon}m</b> — ${zone.type}</small>
            </div>`).addTo(this.map);
        this.zoneCircles.set(zone.id, circle);
    }

    centerMapOnZone(zone: Zone): void {
        if (!this.map) return;
        this.map.setView([zone.centreLat, zone.centreLon], 16, { animate: true });
        this.zoneCircles.get(zone.id)?.openPopup();
    }

    private refreshAllMarkers(): void {
        if (!this.L || !this.map) return;
        this.patientPositions.forEach(pos => {
            const zone = this.zones.find(z => z.patientId === pos.patientId);
            let ok = true;
            if (zone) {
                const d = this.haversine(pos.latitude, pos.longitude, zone.centreLat, zone.centreLon);
                ok = zone.type === 'SAFE' ? d <= zone.rayon : d > zone.rayon;
            }
            const name  = this.getPatientName(pos.patientId);
            const icon  = ok ? this.buildGreenIcon(name) : this.buildRedIcon(name);
            const ll    = [pos.latitude, pos.longitude];
            const popup = `<div style="font-family:sans-serif;min-width:160px">
                <b>${name}</b><br>
                <small style="color:#90a4ae">ID: ${pos.patientId}</small><br>
                <small>📍 ${pos.latitude.toFixed(5)}, ${pos.longitude.toFixed(5)}</small><br>
                <small>🕒 ${this.parseDate(pos.timestamp).toLocaleTimeString('en-US')}</small>
            </div>`;
            if (this.patientMarkers.has(pos.patientId)) {
                const m = this.patientMarkers.get(pos.patientId);
                m.setLatLng(ll); m.setIcon(icon); m.setPopupContent(popup);
            } else {
                this.patientMarkers.set(pos.patientId,
                    this.L.marker(ll, { icon })
                        .bindPopup(popup)
                        .addTo(this.map));
            }
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  CRUD Zones
    // ─────────────────────────────────────────────────────────────
    openAddModal(): void {
        this.isEditMode = false; this.editingZoneId = null;
        this.formError  = ''; this.errors = {};
        this.zoneForm   = {
            nomZone: '', patientId: '',
            soignantId: this.userId,
            centreLat: null as any, centreLon: null as any,
            rayon: 200, type: 'SAFE'
        };
        this.showModal = true;
    }

    openEditModal(zone: Zone): void {
        this.isEditMode    = true;
        this.editingZoneId = zone.id;
        this.formError     = ''; this.errors = {};
        this.zoneForm      = { ...zone };
        this.showModal     = true;
    }

    closeModal(): void {
        this.showModal = false; this.pickingLocation = false;
        if (this.tempMarker) { this.tempMarker.remove(); this.tempMarker = null; }
    }

    startPickLocation(): void { this.showModal = false; this.pickingLocation = true; }

    private validate(): boolean {
        this.errors = {};
        if (!this.zoneForm.nomZone?.trim())
            this.errors['nomZone'] = 'Zone name is required.';
        if (!this.zoneForm.patientId?.trim())
            this.errors['patientId'] = 'Please select a patient.';
        if (this.zoneForm.centreLat == null || isNaN(this.zoneForm.centreLat))
            this.errors['centreLat'] = 'Latitude is required.';
        if (this.zoneForm.centreLon == null || isNaN(this.zoneForm.centreLon))
            this.errors['centreLon'] = 'Longitude is required.';
        if (!this.zoneForm.rayon || this.zoneForm.rayon < 10 || this.zoneForm.rayon > 10000)
            this.errors['rayon'] = 'Radius must be between 10m and 10,000m.';
        return Object.keys(this.errors).length === 0;
    }

    saveZone(): void {
        if (!this.validate()) return;
        this.isSaving = true; this.formError = '';
        const obs = this.isEditMode && this.editingZoneId !== null
            ? this.geofencingService.updateZone(this.editingZoneId, this.zoneForm)
            : this.geofencingService.createZone(this.zoneForm);
        obs.subscribe({
            next: (saved: Zone) => {
                if (this.isEditMode) {
                    const idx = this.zones.findIndex(z => z.id === saved.id);
                    if (idx > -1) this.zones[idx] = saved;
                    this.zoneCircles.get(saved.id)?.remove();
                    this.zoneCircles.delete(saved.id);
                } else {
                    this.zones = [...this.zones, saved];
                }
                this.addZoneToMap(saved);
                this.isSaving = false;
                this.closeModal();
                this.cdr.detectChanges();
            },
            error: (err: any) => {
                this.formError = `Error: ${err?.error?.message ?? 'Check backend logs.'}`;
                this.isSaving  = false;
            }
        });
    }

    deleteZone(zone: Zone): void {
        if (!confirm(`Delete zone "${zone.nomZone}"?`)) return;
        this.geofencingService.deleteZone(zone.id).subscribe({
            next: () => {
                this.zones = this.zones.filter(z => z.id !== zone.id);
                this.zoneCircles.get(zone.id)?.remove();
                this.zoneCircles.delete(zone.id);
                this.cdr.detectChanges();
            }
        });
    }

    resolveAlert(alert: Alert): void {
        this.geofencingService.resolveAlert(alert.id).subscribe(() => {
            this.activeAlerts = this.activeAlerts.filter(a => a.id !== alert.id);
            this.cdr.detectChanges();
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    parseDate(ts: any): Date {
        if (!ts) return new Date();
        if (Array.isArray(ts)) { const [y,m,d,h,min,s] = ts; return new Date(y, m-1, d, h, min, s??0); }
        return new Date(ts);
    }

    private haversine(lat1: number, lon1: number, lat2: number, lon2: number): number {
        const R = 6371000;
        const f1 = lat1*Math.PI/180, f2 = lat2*Math.PI/180;
        const df = (lat2-lat1)*Math.PI/180, dl = (lon2-lon1)*Math.PI/180;
        const a  = Math.sin(df/2)**2 + Math.cos(f1)*Math.cos(f2)*Math.sin(dl/2)**2;
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    private getPatientName(patientId: string): string {
        const p = this.patients.find(p => p.id === patientId);
        return p ? `${p.prenom} ${p.nom}` : patientId;
    }

    private buildGreenIcon(label: string): any {
        return this.L.divIcon({ className: '',
            html: `<div style="position:relative;width:24px;height:24px">
              <div style="position:absolute;inset:0;background:rgba(56,142,60,.25);border-radius:50%;animation:safeRipple 2.5s ease-out infinite"></div>
              <div style="position:absolute;inset:5px;background:#388e3c;border-radius:50%;border:2.5px solid white"></div>
              <div style="position:absolute;top:-22px;left:50%;transform:translateX(-50%);background:#388e3c;color:#fff;font-size:9px;font-weight:700;padding:2px 6px;border-radius:5px;white-space:nowrap">🟢 ${label}</div>
            </div>`, iconSize:[24,24], iconAnchor:[12,12] });
    }

    private buildRedIcon(label: string): any {
        return this.L.divIcon({ className: '',
            html: `<div style="position:relative;width:24px;height:24px">
              <div style="position:absolute;inset:0;background:rgba(229,57,53,.3);border-radius:50%;animation:alertRipple 1.2s ease-out infinite"></div>
              <div style="position:absolute;inset:5px;background:#e53935;border-radius:50%;border:2.5px solid white"></div>
              <div style="position:absolute;top:-22px;left:50%;transform:translateX(-50%);background:#e53935;color:#fff;font-size:9px;font-weight:700;padding:2px 6px;border-radius:5px;white-space:nowrap">🔴 ${label}</div>
            </div>`, iconSize:[24,24], iconAnchor:[12,12] });
    }
}