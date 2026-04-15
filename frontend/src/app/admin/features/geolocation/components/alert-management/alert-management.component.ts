import { Component, OnInit, OnDestroy, ViewChild, AfterViewInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { Subscription, timer } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Alert } from '../../../../core/models/alert.model';
import { AlertSettings } from '../../../../core/models/alert-settings.model';
import { AlertService } from '../../../../core/services/alert.service';
import { AlertDetailComponent } from '../alert-detail/alert-detail.component';

@Component({
    selector: 'app-alert-management',
    standalone: false,
    templateUrl: './alert-management.component.html',
    styleUrls: ['./alert-management.component.scss']
})
export class AlertManagementComponent implements OnInit, AfterViewInit, OnDestroy {

    alerts:           Alert[] = [];
    dataSource        = new MatTableDataSource<Alert>([]);
    displayedColumns  = ['timestamp', 'patient', 'type', 'zone', 'status', 'actions'];
    settings:         AlertSettings | null = null;

    isLoading         = true;
    errorMsg          = '';
    settingsSaved     = false;
    isSavingSettings  = false;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort)      sort!: MatSort;

    private pollSub!: Subscription;

    constructor(
        private alertService: AlertService,
        private dialog: MatDialog
    ) {}

    // ─────────────────────────────────────────────────────────
    ngOnInit(): void {
        this.loadAlerts();
        this.loadSettings();
        this.startPolling();
    }

    ngAfterViewInit(): void {
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort      = this.sort;

        // Tri timestamp DESC par défaut
        this.dataSource.sortingDataAccessor = (item, property) => {
            if (property === 'timestamp') return this.formatDate(item.timestamp).getTime();
            return (item as any)[property] ?? '';
        };
    }

    ngOnDestroy(): void { this.pollSub?.unsubscribe(); }

    // ── Polling toutes les 5s ─────────────────────────────────
    private startPolling(): void {
        this.pollSub = timer(5000, 5000).pipe(
            switchMap(() => this.alertService.getAlerts())
        ).subscribe({
            next: (alerts) => this.updateTable(alerts),
            error: (err)   => console.error('Poll error:', err)
        });
    }

    // ── Chargement initial ────────────────────────────────────
    loadAlerts(): void {
        this.isLoading = true;
        this.errorMsg  = '';

        this.alertService.getAlerts().subscribe({
            next:  (alerts) => { this.updateTable(alerts); this.isLoading = false; },
            error: (err)    => {
                this.errorMsg  = 'Erreur de chargement. Vérifiez la connexion backend.';
                this.isLoading = false;
                console.error(err);
            }
        });
    }

    private updateTable(alerts: Alert[]): void {
        // Trier par date décroissante
        this.alerts = [...alerts].sort((a, b) =>
            this.formatDate(b.timestamp).getTime() - this.formatDate(a.timestamp).getTime()
        );
        this.dataSource.data      = this.alerts;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort      = this.sort;
    }

    loadSettings(): void {
        this.alertService.getSettings().subscribe(s => this.settings = s);
    }

    // ── Filtrer ───────────────────────────────────────────────
    applyFilter(event: Event): void {
        const val = (event.target as HTMLInputElement).value;
        this.dataSource.filter = val.trim().toLowerCase();
        if (this.dataSource.paginator) this.dataSource.paginator.firstPage();
    }

    // ── Détail + Résolution ───────────────────────────────────
    openAlertDetail(alert: Alert): void {
        this.dialog.open(AlertDetailComponent, { width: '700px', data: alert })
            .afterClosed().subscribe(result => { if (result) this.loadAlerts(); });
    }

    markAsResolved(alert: Alert): void {
        this.alertService.updateAlertStatus(alert.id, 'resolved').subscribe({
            next:  () => this.loadAlerts(),
            error: (err) => console.error('Erreur résolution:', err)
        });
    }

    // ── Sauvegarder settings ──────────────────────────────────
    saveSettings(): void {
        if (!this.settings) return;
        this.isSavingSettings = true;

        this.alertService.updateSettings(this.settings).subscribe({
            next: () => {
                this.isSavingSettings = false;
                this.settingsSaved    = true;
                setTimeout(() => this.settingsSaved = false, 3000);
            },
            error: () => { this.isSavingSettings = false; }
        });
    }

    // ── Helpers ───────────────────────────────────────────────
    formatDate(ts: any): Date {
        if (!ts) return new Date();
        if (Array.isArray(ts)) {
            const [y, m, d, h, min, s] = ts;
            return new Date(y, m - 1, d, h, min, s ?? 0);
        }
        return new Date(ts);
    }

    getAlertTypeLabel(type: string): string {
        const labels: Record<string, string> = {
            'SORTIE_ZONE_SAFE':   'Sortie de zone',
            'ENTREE_ZONE_DANGER': 'Entrée zone dangereuse',
            'zone_exit':          'Sortie de zone',
            'forbidden_entry':    'Entrée interdite',
            'gps_loss':           'Perte GPS',
            'low_battery':        'Batterie faible'
        };
        return labels[type] || type;
    }

    getStatusClass(status: string): string {
        const map: Record<string, string> = {
            'Active':   'status-active',
            'Resolved': 'status-resolved',
            'ignored':  'status-ignored',
            'new':      'status-new'
        };
        return map[status] || 'status-default';
    }

    getStatusLabel(status: string): string {
        const map: Record<string, string> = {
            'Active':   'En cours',
            'Resolved': 'Résolue',
            'ignored':  'Ignorée',
            'new':      'Nouvelle'
        };
        return map[status] || status;
    }

    get activeCount():   number { return this.alerts.filter(a => a.status === 'Active').length; }
    get resolvedCount(): number { return this.alerts.filter(a => a.status === 'Resolved').length; }
}