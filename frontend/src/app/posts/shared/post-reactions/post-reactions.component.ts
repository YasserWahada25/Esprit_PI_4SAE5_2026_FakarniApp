import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactionService } from '../../services/reaction.service';
import { ReactionCounts, ReactionType } from '../../models/reaction.model';

@Component({
    selector: 'app-post-reactions',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './post-reactions.component.html',
    styleUrl: './post-reactions.component.css'
})
export class PostReactionsComponent implements OnInit {
    @Input() postId!: number;
    @Input() userId: number = 1; // TODO: Get from auth service
    
    reactionCounts = signal<ReactionCounts | null>(null);
    isLoading = signal(false);

    constructor(private reactionService: ReactionService) {}

    ngOnInit() {
        this.loadReactions();
    }

    loadReactions() {
        this.reactionService.getReactionCounts(this.postId, this.userId)
            .subscribe({
                next: (counts) => this.reactionCounts.set(counts),
                error: (error) => console.error('Error loading reactions:', error)
            });
    }

    toggleReaction(type: ReactionType) {
        if (this.isLoading()) return;
        
        this.isLoading.set(true);
        this.reactionService.toggleReaction(this.postId, this.userId, type)
            .subscribe({
                next: (counts) => {
                    this.reactionCounts.set(counts);
                    this.isLoading.set(false);
                },
                error: (error) => {
                    console.error('Error toggling reaction:', error);
                    this.isLoading.set(false);
                }
            });
    }

    isActive(type: ReactionType): boolean {
        return this.reactionCounts()?.userReaction === type;
    }

    getCount(type: ReactionType): number {
        return this.reactionCounts()?.counts[type] || 0;
    }
}
