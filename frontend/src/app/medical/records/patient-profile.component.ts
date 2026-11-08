import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { User } from '../../auth/models/user.model';
import { Role } from '../../auth/models/sign-up.model';
import { AnalyseIRMResponse, DetectionService, DossierMedicalResponse } from '../services/detection';

/**
 * Dossier médical de l'utilisateur connecté : ses analyses IRM (Dossier-Medical-Service)
 * et l'évolution du stade estimé. Les soignants consultent les dossiers de leurs
 * patients dans « Suivi ».
 */
@Component({
    selector: 'app-patient-profile',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './patient-profile.component.html',
    styleUrl: './patient-profile.component.css'
})
export class PatientProfileComponent implements OnInit {
    user: User | null = null;
    dossier: DossierMedicalResponse | null = null;
    isLoading = false;
    /** Aucun dossier : l'utilisateur n'a encore passé aucune analyse IRM. */
    noDossier = false;
    errorMessage = '';

    private readonly stages: Record<string, { label: string; color: string; score: number }> = {
        Non_Demented:       { label: 'Normal',     color: '#38bdf8', score: 28 },
        Very_Mild_Demented: { label: 'Très léger', color: '#818cf8', score: 22 },
        Mild_Demented:      { label: 'Léger',      color: '#c084fc', score: 16 },
        Moderate_Demented:  { label: 'Modéré',     color: '#fb7185', score: 9 },
    };

    constructor(private authService: AuthService, private detectionService: DetectionService) {}

    ngOnInit(): void {
        this.user = this.authService.getCurrentUser();
        const userId = this.authService.getCurrentUserId();
        if (!userId) {
            this.errorMessage = 'Connectez-vous pour consulter votre dossier médical.';
            return;
        }
        this.loadDossier(userId);
    }

    get isPatient(): boolean {
        return !this.user || this.user.role === Role.PATIENT_PROFILE;
    }

    get fullName(): string {
        return this.user ? `${this.user.prenom} ${this.user.nom}`.trim() : 'Mon dossier';
    }

    get initials(): string {
        return this.fullName.split(' ').filter(p => p).slice(0, 2).map(p => p[0].toUpperCase()).join('');
    }

    /** Analyses de la plus récente à la plus ancienne. */
    get analyses(): AnalyseIRMResponse[] {
        return [...(this.dossier?.analyses ?? [])].sort(
            (a, b) => this.toDate(b.dateAnalyse).getTime() - this.toDate(a.dateAnalyse).getTime()
        );
    }

    /** Score indicatif (échelle MMSE /30) déduit du stade IA, comme dans « Suivi ». */
    get evolution(): { date: string; score: number; color: string }[] {
        return [...this.analyses].reverse().slice(-8).map(a => ({
            date: this.toDate(a.dateAnalyse).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short' }),
            score: this.stage(a.prediction).score,
            color: this.stage(a.prediction).color,
        }));
    }

    loadDossier(userId: string): void {
        this.isLoading = true;
        this.errorMessage = '';
        this.noDossier = false;
        this.detectionService.getDossierByPatientId(userId).subscribe({
            next: (dossier) => {
                this.dossier = dossier;
                this.isLoading = false;
            },
            error: (err) => {
                this.isLoading = false;
                if (err?.status === 404) {
                    this.noDossier = true;
                } else {
                    this.errorMessage = 'Impossible de charger le dossier médical. Réessayez plus tard.';
                }
            }
        });
    }

    stage(prediction: string): { label: string; color: string; score: number } {
        return this.stages[prediction] ?? { label: prediction?.replace(/_/g, ' ') || '—', color: '#64748b', score: 20 };
    }

    getBarHeight(score: number): string {
        return (score / 30) * 100 + '%';
    }

    /** Les services renvoient l'heure UTC sans fuseau. */
    toDate(value: string): Date {
        if (!value) return new Date(NaN);
        return new Date(/(Z|[+-]\d{2}:?\d{2})$/.test(value) ? value : `${value}Z`);
    }

    formatDate(value: string): string {
        return this.toDate(value).toLocaleDateString('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' });
    }
}
