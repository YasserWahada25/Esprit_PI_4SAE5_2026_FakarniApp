import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GeofencingService, Alert, NotificationPreference } from '../shared/geofencing.service';
import { AuthService } from '../../auth/services/auth.service';

@Component({
    selector: 'app-alerts-history',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './alerts-history.component.html',
    styleUrl: './alerts-history.component.css'
})
export class AlertsHistoryComponent implements OnInit, OnDestroy {

    // ── User ──────────────────────────────────────────────────────
    userId   = '';
    userRole = '';
    get isPatient():  boolean { return this.userRole === 'PATIENT_PROFILE'; }
    get isSoignant(): boolean { return this.userRole === 'CARE_OWNER' || this.userRole === 'DOCTOR_PROFILE'; }

    // ── Alerts ────────────────────────────────────────────────────
    alerts:         Alert[] = [];
    filteredAlerts: Alert[] = [];
    filterType:   string = 'All';
    filterStatus: string = 'All';
    isLoading = true;
    errorMsg  = '';

    // ── Notification Preferences (soignant) ───────────────────────
    pref: NotificationPreference = { soignantId: '', emailEnabled: true, voiceEnabled: false };
    settingsSaved    = false;
    isSavingSettings = false;
    settingsError    = '';

    private sub!: Subscription;

    constructor(
        private geofencingService: GeofencingService,
        private authService:       AuthService
    ) {
        const user    = this.authService.getCurrentUser();
        this.userId   = user?.id   ?? '';
        this.userRole = user?.role ?? '';
    }

    ngOnInit(): void {
        // Charger alertes selon le rôle
        const alerts$ = this.isPatient
            ? this.geofencingService.getAlertsRealtime(this.userId)
            : this.geofencingService.getAlertsRealtime(undefined, this.userId);

        this.sub = alerts$.subscribe({
            next: (data) => {
                this.alerts    = data;
                this.isLoading = false;
                this.errorMsg  = '';
                this.applyFilters();
            },
            error: () => {
                this.errorMsg  = 'Failed to load alerts.';
                this.isLoading = false;
            }
        });

        // Charger préférences si soignant
        if (this.isSoignant) {
            this.geofencingService.getNotificationPreferences(this.userId)
                .subscribe(p => {
                    this.pref = p;
                    this.pref.soignantId = this.userId;
                });
        }
    }

    ngOnDestroy(): void { this.sub?.unsubscribe(); }

    // ── Alerts ────────────────────────────────────────────────────
    applyFilters(): void {
        this.filteredAlerts = this.alerts.filter(a => {
            const typeOk   = this.filterType   === 'All' || a.type   === this.filterType;
            const statusOk = this.filterStatus === 'All' || a.status === this.filterStatus;
            return typeOk && statusOk;
        });
    }

    resolveAlert(alert: Alert): void {
        this.geofencingService.resolveAlert(alert.id).subscribe({
            next: (updated) => {
                const idx = this.alerts.findIndex(a => a.id === updated.id);
                if (idx !== -1) this.alerts[idx] = updated;
                this.applyFilters();
            }
        });
    }

    // ── Notification Preferences (soignant uniquement) ────────────
    saveSettings(): void {
        this.isSavingSettings = true;
        this.settingsError    = '';
        this.pref.soignantId  = this.userId;

        this.geofencingService.saveNotificationPreferences(this.pref).subscribe({
            next: (saved) => {
                this.pref             = saved;
                this.isSavingSettings = false;
                this.settingsSaved    = true;
                setTimeout(() => this.settingsSaved = false, 3000);
            },
            error: () => {
                this.settingsError    = 'Erreur lors de la sauvegarde.';
                this.isSavingSettings = false;
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────
    formatDate(ts: any): Date {
        if (!ts) return new Date();
        if (Array.isArray(ts)) { const [y,m,d,h,min,s] = ts; return new Date(y, m-1, d, h, min, s??0); }
        return new Date(ts);
    }

    get recentAlerts(): Alert[] {
        return [...this.alerts]
            .sort((a, b) => this.formatDate(b.timestamp).getTime() - this.formatDate(a.timestamp).getTime())
            .slice(0, 3);
    }

    get alertTypes(): string[] {
        return [...new Set(this.alerts.map(a => a.type).filter(Boolean))].sort();
    }

    get activeCount():   number { return this.alerts.filter(a => a.status === 'Active').length; }
    get resolvedCount(): number { return this.alerts.filter(a => a.status === 'Resolved').length; }
}