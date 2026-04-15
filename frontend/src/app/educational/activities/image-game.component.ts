import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
    ActivityService,
    GameSessionResultDto,
    GameSessionStartResponseDto,
    ImageCardPlayDto
} from '../../admin/core/services/activity.service';
import { EducationalActivity } from '../../admin/core/models/educational-activity.model';

@Component({
    selector: 'app-image-game',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './image-game.component.html',
    styleUrl: './image-game.component.css'
})
export class ImageGameComponent implements OnInit {
    @Input({ required: true }) activity!: EducationalActivity;
    @Input({ required: true }) userId!: number;
    @Output() completed = new EventEmitter<GameSessionResultDto>();
    @Output() cancelled = new EventEmitter<void>();
    @Output() errorMsg = new EventEmitter<string>();

    session: GameSessionStartResponseDto | null = null;
    cards: ImageCardPlayDto[] = [];
    matchedIds = new Set<number>();
    /** 0, 1 ou 2 cartes retournées pour une tentative en cours. */
    peeking: number[] = [];
    /** Cartes dont l’URL distante (ex. Wikimedia) a échoué au chargement — affichage du libellé en secours. */
    failedImageIds = new Set<number>();
    busy = false;
    moves = 0;
    pairsFound = 0;
    pairTotal = 0;

    constructor(
        private activityService: ActivityService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.activityService.startGameSession(this.activity.id, this.userId).subscribe({
            next: s => {
                this.session = s;
                this.cards = [...(s.imageCards ?? [])];
                this.pairTotal = s.totalQuestions;
                this.moves = 0;
                this.pairsFound = 0;
                this.matchedIds.clear();
                this.peeking = [];
                this.failedImageIds.clear();
                this.cdr.markForCheck();
            },
            error: err =>
                this.errorMsg.emit(
                    err?.error?.message ?? err?.message ?? 'Impossible de démarrer le memory.'
                )
        });
    }

    isFaceUp(id: number): boolean {
        return this.matchedIds.has(id) || this.peeking.includes(id);
    }

    onCardClick(card: ImageCardPlayDto): void {
        if (this.busy || !this.session || this.matchedIds.has(card.id)) {
            return;
        }
        if (this.peeking.length === 0) {
            this.peeking = [card.id];
            this.cdr.markForCheck();
            return;
        }
        if (this.peeking.length === 1) {
            if (this.peeking[0] === card.id) {
                return;
            }
            this.peeking = [this.peeking[0], card.id];
            this.tryPair();
        }
    }

    private tryPair(): void {
        if (!this.session || this.peeking.length !== 2) {
            return;
        }
        const [a, b] = this.peeking;
        this.busy = true;
        this.activityService.submitMemoryMove(this.session.sessionId, a, b).subscribe({
            next: res => {
                this.moves = res.movesCount;
                this.pairsFound = res.pairsFound;
                this.pairTotal = res.pairTotal;
                if (res.match) {
                    this.matchedIds.add(a);
                    this.matchedIds.add(b);
                    this.peeking = [];
                    if (res.gameCompleted && res.sessionResult) {
                        this.completed.emit(res.sessionResult);
                        this.session = null;
                    }
                    this.busy = false;
                    this.cdr.markForCheck();
                    return;
                }
                setTimeout(() => {
                    this.peeking = [];
                    this.busy = false;
                    this.cdr.markForCheck();
                }, 750);
                this.cdr.markForCheck();
            },
            error: err => {
                this.busy = false;
                this.peeking = [];
                this.errorMsg.emit(
                    err?.error?.message ?? err?.message ?? 'Erreur lors du coup.'
                );
                this.cdr.markForCheck();
            }
        });
    }

    cancel(): void {
        this.session = null;
        this.cancelled.emit();
    }

    onCardImageError(cardId: number): void {
        this.failedImageIds.add(cardId);
        this.cdr.markForCheck();
    }
}
