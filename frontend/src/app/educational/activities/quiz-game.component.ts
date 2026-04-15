import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { finalize, timeout } from 'rxjs/operators';
import {
    ActivityService,
    GameSessionResultDto,
    GameSessionStartResponseDto,
    QuestionPlayDto,
    SubmitAnswerResponseDto
} from '../../admin/core/services/activity.service';
import { EducationalActivity } from '../../admin/core/models/educational-activity.model';

@Component({
    selector: 'app-quiz-game',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './quiz-game.component.html',
    styleUrl: './quiz-game.component.css'
})
export class QuizGameComponent implements OnInit {
    @Input({ required: true }) activity!: EducationalActivity;
    @Input({ required: true }) userId!: number;
    @Output() completed = new EventEmitter<GameSessionResultDto>();
    @Output() cancelled = new EventEmitter<void>();
    @Output() errorMsg = new EventEmitter<string>();

    readonly requestTimeoutMs = 45_000;
    private static readonly quizImagePlaceholder = '/assets/quiz/placeholder-scene.svg';

    /** Primary URL failed → show placeholder for this question id. */
    private questionImageFallbackQuestionId: number | null = null;
    /** Placeholder also failed → hide block for this question id. */
    private questionImageHiddenQuestionId: number | null = null;

    session: GameSessionStartResponseDto | null = null;
    questionIndex = 0;
    selectedAnswer: string | null = null;
    answerRequestPending = false;
    finishRequestPending = false;

    constructor(
        private activityService: ActivityService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.activityService.startGameSession(this.activity.id, this.userId).subscribe({
            next: s => {
                this.session = s;
                this.questionIndex = 0;
                this.selectedAnswer = null;
                this.resetQuestionImageState();
                this.cdr.markForCheck();
            },
            error: err => {
                this.errorMsg.emit(
                    err?.error?.message ?? err?.message ?? 'Impossible de démarrer la session.'
                );
            }
        });
    }

    get quizBusy(): boolean {
        return this.answerRequestPending || this.finishRequestPending;
    }

    get currentQuestion(): QuestionPlayDto | null {
        if (!this.session?.questions?.length) {
            return null;
        }
        return this.session.questions[this.questionIndex] ?? null;
    }

    get progress(): number {
        if (!this.session?.questions?.length) {
            return 0;
        }
        return ((this.questionIndex + 1) / this.session.questions.length) * 100;
    }

    /** URL affichée pour la scène de la question (API `questionImageUrl` ou `imageUrl`). */
    get questionSceneUrl(): string | null {
        const q = this.currentQuestion;
        if (!q) {
            return null;
        }
        if (this.questionImageHiddenQuestionId === q.id) {
            return null;
        }
        if (this.questionImageFallbackQuestionId === q.id) {
            return QuizGameComponent.quizImagePlaceholder;
        }
        const raw = (q.questionImageUrl ?? q.imageUrl ?? '').toString().trim();
        return raw.length > 0 ? raw : null;
    }

    onQuestionImageError(event: Event): void {
        const q = this.currentQuestion;
        const img = event.target as HTMLImageElement | null;
        if (!q || !img) {
            return;
        }
        const src = (img.currentSrc || img.src || '').toString();
        if (src.includes('placeholder-scene')) {
            this.questionImageHiddenQuestionId = q.id;
            this.questionImageFallbackQuestionId = null;
        } else {
            this.questionImageFallbackQuestionId = q.id;
        }
        this.cdr.markForCheck();
    }

    private resetQuestionImageState(): void {
        this.questionImageFallbackQuestionId = null;
        this.questionImageHiddenQuestionId = null;
    }

    selectOption(value: string): void {
        if (this.quizBusy) {
            return;
        }
        this.selectedAnswer = value;
        this.cdr.markForCheck();
    }

    isSelected(value: string): boolean {
        return this.selectedAnswer != null && this.selectedAnswer === value;
    }

    optionRows(q: QuestionPlayDto): { label: string; imageUrl?: string | null }[] {
        if (q.quizOptions && q.quizOptions.length > 0) {
            return q.quizOptions.map(o => ({ label: o.label, imageUrl: o.imageUrl }));
        }
        return (q.options ?? []).map(label => ({ label, imageUrl: null }));
    }

    submitCurrent(): void {
        if (!this.session || !this.currentQuestion || this.selectedAnswer == null || this.quizBusy) {
            return;
        }
        const sid = this.session.sessionId;
        const qid = this.currentQuestion.id;
        const aid = this.session.activityId;
        const total = this.session.questions.length;
        const idx = this.questionIndex;
        this.answerRequestPending = true;
        this.activityService
            .submitAnswer(aid, sid, qid, this.selectedAnswer)
            .pipe(
                timeout(this.requestTimeoutMs),
                finalize(() => {
                    this.answerRequestPending = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: res => this.handleSubmit(res, total, idx),
                error: err => {
                    const name = err?.name ?? '';
                    this.errorMsg.emit(
                        name === 'TimeoutError'
                            ? 'Délai dépassé. Vérifiez le service (8084).'
                            : err?.error?.message ?? err?.message ?? 'Erreur lors de l’envoi.'
                    );
                }
            });
    }

    private handleSubmit(res: SubmitAnswerResponseDto | null, totalQuestions: number, currentIndex: number): void {
        if (!res || typeof res !== 'object') {
            this.errorMsg.emit('Réponse serveur invalide.');
            return;
        }
        this.selectedAnswer = null;
        if (res.sessionFinished && res.sessionResult) {
            this.completed.emit(res.sessionResult);
            this.session = null;
            return;
        }
        if (res.sessionFinished && !res.sessionResult) {
            this.finishSession();
            return;
        }
        const last = currentIndex >= totalQuestions - 1;
        if (last) {
            this.finishSession();
        } else {
            this.resetQuestionImageState();
            this.questionIndex = currentIndex + 1;
        }
        this.cdr.markForCheck();
    }

    finishSession(): void {
        if (!this.session || this.finishRequestPending) {
            return;
        }
        const sid = this.session.sessionId;
        this.finishRequestPending = true;
        this.activityService
            .finishSession(sid)
            .pipe(
                timeout(this.requestTimeoutMs),
                finalize(() => {
                    this.finishRequestPending = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: res => {
                    if (res) {
                        this.completed.emit(res);
                    }
                    this.session = null;
                },
                error: err =>
                    this.errorMsg.emit(
                        err?.error?.message ?? err?.message ?? 'Erreur à la fin de session.'
                    )
            });
    }

    cancel(): void {
        this.session = null;
        this.cancelled.emit();
    }
}
