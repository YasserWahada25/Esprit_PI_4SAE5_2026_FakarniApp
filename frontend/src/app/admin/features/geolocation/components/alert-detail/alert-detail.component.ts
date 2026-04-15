import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Alert } from '../../../../core/models/alert.model';
import { AlertService } from '../../../../core/services/alert.service';

@Component({
    selector: 'app-alert-detail',
    standalone: false,
    templateUrl: './alert-detail.component.html',
    styleUrls: ['./alert-detail.component.scss']
})
export class AlertDetailComponent {
    notes: string = '';

    constructor(
        public dialogRef: MatDialogRef<AlertDetailComponent>,
        @Inject(MAT_DIALOG_DATA) public alert: Alert,
        private alertService: AlertService
    ) {
        this.notes = alert.notes || '';
    }

    markAsResolved(): void {
        this.alertService.updateAlertStatus(this.alert.id, 'resolved', this.notes).subscribe({
            next: () => this.dialogRef.close(true),
            error: (err) => console.error('Erreur résolution:', err)
        });
    }

    markAsIgnored(): void {
        this.alertService.updateAlertStatus(this.alert.id, 'ignored', this.notes).subscribe({
            next: () => this.dialogRef.close(true),
            error: (err) => console.error('Erreur ignore:', err)
        });
    }

    close(): void {
        this.dialogRef.close();
    }

    // Gère le format [y,m,d,h,min,s] du backend
    formatDate(ts: any): Date {
        if (!ts) return new Date();
        if (Array.isArray(ts)) {
            const [y, m, d, h, min, s] = ts;
            return new Date(y, m - 1, d, h, min, s ?? 0);
        }
        return new Date(ts);
    }

    getStatusLabel(status: string): string {
        const map: Record<string, string> = {
            'Active':   'En cours',
            'Resolved': 'Résolue',
            'ignored':  'Ignorée'
        };
        return map[status] || status;
    }

    getTypeLabel(type: string): string {
        const map: Record<string, string> = {
            'SORTIE_ZONE_SAFE':   'Sortie de zone sécurisée',
            'ENTREE_ZONE_DANGER': 'Entrée zone dangereuse',
            'zone_exit':          'Sortie de zone',
            'forbidden_entry':    'Entrée interdite',
            'gps_loss':           'Perte GPS',
            'low_battery':        'Batterie faible'
        };
        return map[type] || type;
    }
}