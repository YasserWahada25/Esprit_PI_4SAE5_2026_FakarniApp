import { ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityService } from '../../admin/core/services/activity.service';
import { EducationalActivity } from '../../admin/core/models/educational-activity.model';

type Selection =
    | { source: 'tray'; index: number }
    | { source: 'board'; index: number }
    | null;

@Component({
    selector: 'app-puzzle-game',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './puzzle-game.component.html',
    styleUrl: './puzzle-game.component.css'
})
export class PuzzleGameComponent implements OnInit {
    @Input({ required: true }) activity!: EducationalActivity;
    @Output() solved = new EventEmitter<void>();
    @Output() cancelled = new EventEmitter<void>();
    @Output() errorMsg = new EventEmitter<string>();

    readonly rows = 3;
    readonly cols = 4;
    readonly size = this.rows * this.cols;
    readonly tilePx = 120;

    imageUrl: string | null = null;
    loading = true;
    moves = 0;

    // board[slot] = pieceId (0..size-1) or null if empty
    board: Array<number | null> = Array.from({ length: this.size }, () => null);
    // pieces still outside board
    tray: number[] = [];
    selection: Selection = null;

    constructor(
        private activityService: ActivityService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        const fallback = (this.activity?.thumbnailUrl || '').trim();
        this.activityService.getImageCards(this.activity.id).subscribe({
            next: cards => {
                const firstCard = (cards || []).find(c => (c.imageUrl || '').trim().length > 0);
                this.imageUrl = (firstCard?.imageUrl || fallback || '').trim() || null;
                if (!this.imageUrl) {
                    this.errorMsg.emit('Ajoute au moins une image à cette activité puzzle.');
                    this.loading = false;
                    this.cdr.markForCheck();
                    return;
                }
                this.resetGame();
            },
            error: err => {
                this.imageUrl = fallback || null;
                if (!this.imageUrl) {
                    this.errorMsg.emit(
                        err?.error?.message ?? err?.message ?? 'Impossible de charger l’image du puzzle.'
                    );
                    this.loading = false;
                    this.cdr.markForCheck();
                    return;
                }
                this.resetGame();
            }
        });
    }

    resetGame(): void {
        this.loading = false;
        this.moves = 0;
        this.selection = null;
        this.board = Array.from({ length: this.size }, () => null);
        this.tray = this.shuffle(Array.from({ length: this.size }, (_, i) => i));
        this.cdr.markForCheck();
    }

    selectTray(index: number): void {
        if (index < 0 || index >= this.tray.length) {
            return;
        }
        this.selection = { source: 'tray', index };
    }

    selectBoard(slot: number): void {
        if (slot < 0 || slot >= this.board.length) {
            return;
        }
        if (!this.selection) {
            if (this.board[slot] != null) {
                this.selection = { source: 'board', index: slot };
            }
            return;
        }

        const moved = this.placeSelectionIntoSlot(slot);
        if (moved) {
            this.moves++;
            this.selection = null;
            if (this.isSolved()) {
                this.solved.emit();
            }
        }
    }

    isSelectedTray(index: number): boolean {
        return this.selection?.source === 'tray' && this.selection.index === index;
    }

    isSelectedBoard(slot: number): boolean {
        return this.selection?.source === 'board' && this.selection.index === slot;
    }

    isMisplaced(slot: number): boolean {
        const piece = this.board[slot];
        return piece != null && piece !== slot;
    }

    cancel(): void {
        this.cancelled.emit();
    }

    pieceStyle(pieceId: number): Record<string, string> {
        const x = pieceId % this.cols;
        const y = Math.floor(pieceId / this.cols);
        return {
            'background-image': `url("${this.imageUrl}")`,
            'background-size': `${this.cols * this.tilePx}px ${this.rows * this.tilePx}px`,
            'background-position': `${-x * this.tilePx}px ${-y * this.tilePx}px`
        };
    }

    private placeSelectionIntoSlot(slot: number): boolean {
        if (!this.selection) {
            return false;
        }
        if (this.selection.source === 'tray') {
            const fromTray = this.selection.index;
            const piece = this.tray[fromTray];
            if (piece == null) {
                return false;
            }
            const existing = this.board[slot];
            this.board[slot] = piece;
            this.tray.splice(fromTray, 1);
            if (existing != null) {
                this.tray.push(existing);
            }
            return true;
        }

        const fromSlot = this.selection.index;
        if (fromSlot === slot) {
            return false;
        }
        const tmp = this.board[fromSlot];
        this.board[fromSlot] = this.board[slot];
        this.board[slot] = tmp;
        return true;
    }

    private isSolved(): boolean {
        return this.board.every((piece, slot) => piece === slot);
    }

    private shuffle<T>(arr: T[]): T[] {
        const out = [...arr];
        for (let i = out.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [out[i], out[j]] = [out[j], out[i]];
        }
        return out;
    }
}
