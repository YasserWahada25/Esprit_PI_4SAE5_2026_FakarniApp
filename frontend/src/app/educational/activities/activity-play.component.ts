import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ActivityService, GameSessionResultDto } from '../../admin/core/services/activity.service';
import { EducationalActivity } from '../../admin/core/models/educational-activity.model';
import { QuizGameComponent } from './quiz-game.component';
import { ImageGameComponent } from './image-game.component';
import { GameResultComponent, GameResultView } from './game-result.component';

@Component({
    selector: 'app-activity-play',
    standalone: true,
    imports: [CommonModule, RouterLink, QuizGameComponent, ImageGameComponent, GameResultComponent],
    templateUrl: './activity-play.component.html',
    styleUrls: ['./activity-play.component.css', './activities-shared.css']
})
export class ActivityPlayComponent implements OnInit {
    readonly demoUserId = 1;

    activity: EducationalActivity | null = null;
    loadError: string | null = null;
    actionError: string | null = null;
    sessionResultView: GameResultView | null = null;

    contentSheet: { title: string; text: string; videoUrl?: string; durationMin?: number } | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private activityService: ActivityService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        const id = Number(this.route.snapshot.paramMap.get('id'));
        if (!Number.isFinite(id) || id <= 0) {
            this.loadError = 'Identifiant d’activité invalide.';
            return;
        }
        this.activityService.getActivityById(id, this.demoUserId).subscribe({
            next: a => {
                if (!a) {
                    this.loadError = 'Activité introuvable.';
                    return;
                }
                this.activity = a;
                this.openSheetIfStatic(a);
                this.cdr.markForCheck();
            },
            error: err => {
                this.loadError = err?.error?.message ?? err?.message ?? 'Chargement impossible.';
                this.cdr.markForCheck();
            }
        });
    }

    private openSheetIfStatic(a: EducationalActivity): void {
        if (a.type !== 'content' && a.type !== 'video') {
            return;
        }
        const desc = a.description?.trim().length ? a.description : 'Aucune description.';
        this.contentSheet = {
            title: a.name || 'Activité',
            text: desc,
            videoUrl: a.type === 'video' ? a.content?.videoUrl?.trim() || undefined : undefined,
            durationMin:
                a.type === 'video' && a.content?.duration != null ? a.content.duration : undefined
        };
    }

    get showQuiz(): boolean {
        const a = this.activity;
        return !!a && (a.type === 'quiz' || a.type === 'cognitive_game');
    }

    get showMemory(): boolean {
        const a = this.activity;
        return !!a && a.type === 'image_game';
    }

    onQuizDone(res: GameSessionResultDto): void {
        this.sessionResultView = GameResultComponent.fromApi(res, this.activity?.name) ?? null;
        this.cdr.markForCheck();
    }

    onDismissResult(): void {
        this.sessionResultView = null;
        this.router.navigate(['/educational/activities']);
    }

    onReveal(): void {
        if (!this.sessionResultView) {
            return;
        }
        this.sessionResultView = { ...this.sessionResultView, detailsRevealed: true };
        this.cdr.markForCheck();
    }

    exportSummary(): void {
        if (!this.sessionResultView) {
            return;
        }
        const r = this.sessionResultView;
        const lines: string[] = [];
        const title = r.activityTitle?.trim() || 'Session';
        lines.push(`Récapitulatif — ${title}`);
        lines.push('');
        lines.push(r.headline);
        lines.push(r.detail);
        lines.push('');
        if (r.answersDetail.length > 0) {
            lines.push('--- Détail ---');
            r.answersDetail.forEach((row, i) => {
                lines.push(`\nQuestion ${i + 1}: ${row.prompt}`);
                lines.push(`  Votre réponse : ${row.userAnswer} (${row.correct ? 'correct' : 'incorrect'})`);
                if (!row.correct && row.expectedAnswer) {
                    lines.push(`  Bonne réponse : ${row.expectedAnswer}`);
                }
            });
        }
        const blob = new Blob([lines.join('\n')], { type: 'text/plain;charset=utf-8' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `session-recap-${Date.now()}.txt`;
        a.click();
        URL.revokeObjectURL(url);
    }

    onCancelGame(): void {
        this.router.navigate(['/educational/activities']);
    }

    onGameError(msg: string): void {
        this.actionError = msg;
        this.cdr.markForCheck();
    }

    dismissActionError(): void {
        this.actionError = null;
    }

    dismissSheet(): void {
        this.contentSheet = null;
        this.router.navigate(['/educational/activities']);
    }
}
