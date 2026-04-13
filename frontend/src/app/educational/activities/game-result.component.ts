import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GameSessionResultDto, SessionAnswerDetailDto } from '../../admin/core/services/activity.service';

export interface GameResultView {
    outcome: 'success' | 'failure' | 'neutral';
    headline: string;
    detail: string;
    scorePercent: number | null;
    activityTitle?: string;
    answersDetail: SessionAnswerDetailDto[];
    detailsRevealed: boolean;
}

@Component({
    selector: 'app-game-result',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './game-result.component.html',
    styleUrls: ['./game-result.component.css', './activities-shared.css']
})
export class GameResultComponent {
    @Input({ required: true }) view!: GameResultView;
    @Output() dismiss = new EventEmitter<void>();
    @Output() revealDetails = new EventEmitter<void>();
    @Output() exportSummary = new EventEmitter<void>();

    onDismiss(): void {
        this.dismiss.emit();
    }

    onReveal(): void {
        this.revealDetails.emit();
    }

    onExport(): void {
        this.exportSummary.emit();
    }

    recapImageUrl(row: SessionAnswerDetailDto): string | null {
        const u = (row.questionImageUrl ?? row.imageUrl ?? '').toString().trim();
        return u.length > 0 ? u : null;
    }

    static fromApi(res: GameSessionResultDto | null | undefined, title?: string): GameResultView | null {
        if (!res) {
            return null;
        }
        const pct = res?.scorePercent ?? res?.percentage ?? null;
        const scoreLabel =
            pct != null && !Number.isNaN(pct) ? `${Math.round(Number(pct))} %` : '—';
        const st = res?.status;
        const answersDetail = Array.isArray(res.answers) ? res.answers : [];
        const detailsRevealed = answersDetail.length === 0;
        const base = {
            scorePercent: pct != null && !Number.isNaN(Number(pct)) ? Math.round(Number(pct)) : null,
            activityTitle: title ?? res.activityTitle,
            answersDetail,
            detailsRevealed
        };
        if (st === 'SUCCESS') {
            return {
                outcome: 'success',
                headline: 'Bravo, session réussie',
                detail: `Votre score : ${scoreLabel} (seuil atteint ou dépassé).`,
                ...base
            };
        }
        if (st === 'FAILURE') {
            return {
                outcome: 'failure',
                headline: 'Session terminée — non validée',
                detail: `Score : ${scoreLabel}. Vous pouvez relancer l’activité pour réessayer.`,
                ...base
            };
        }
        return {
            outcome: 'neutral',
            headline: 'Session terminée',
            detail: `Score : ${scoreLabel}.`,
            ...base
        };
    }
}
