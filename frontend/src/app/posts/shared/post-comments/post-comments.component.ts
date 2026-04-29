import { Component, Input, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommentService } from '../../services/comment.service';
import { Comment, CommentPage } from '../../models/comment.model';

@Component({
    selector: 'app-post-comments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './post-comments.component.html',
    styleUrl: './post-comments.component.css'
})
export class PostCommentsComponent implements OnInit {
    @Input() postId!: number;
    @Input() userId: number = 1; // TODO: Get from auth service

    comments = signal<Comment[]>([]);
    newCommentContent = signal('');
    replyContent = signal('');
    replyingTo = signal<number | null>(null);
    
    currentPage = signal(0);
    pageSize = signal(10);
    totalComments = signal(0);
    totalPages = signal(0);
    
    isLoading = signal(false);
    errorMessage = signal('');

    constructor(private commentService: CommentService) {}

    ngOnInit() {
        this.loadComments();
    }

    loadComments() {
        this.isLoading.set(true);
        this.errorMessage.set('');
        
        this.commentService.getComments(this.postId, this.currentPage(), this.pageSize())
            .subscribe({
                next: (page: CommentPage) => {
                    this.comments.set(page.content);
                    this.totalComments.set(page.totalElements);
                    this.totalPages.set(page.totalPages);
                    this.isLoading.set(false);
                },
                error: (error) => {
                    console.error('Error loading comments:', error);
                    this.errorMessage.set('Failed to load comments');
                    this.isLoading.set(false);
                }
            });
    }

    addComment() {
        const content = this.newCommentContent().trim();
        if (!content) return;

        this.commentService.addComment(this.postId, this.userId, content)
            .subscribe({
                next: () => {
                    this.newCommentContent.set('');
                    this.loadComments();
                },
                error: (error) => {
                    console.error('Error adding comment:', error);
                    this.errorMessage.set('Failed to add comment');
                }
            });
    }

    startReply(comment: Comment) {
        this.replyingTo.set(comment.id);
        this.replyContent.set('');
    }

    addReply(parentCommentId: number) {
        const content = this.replyContent().trim();
        if (!content) return;

        this.commentService.addComment(this.postId, this.userId, content, parentCommentId)
            .subscribe({
                next: () => {
                    this.cancelReply();
                    this.loadComments();
                },
                error: (error) => {
                    console.error('Error adding reply:', error);
                    this.errorMessage.set('Failed to add reply');
                }
            });
    }

    cancelReply() {
        this.replyingTo.set(null);
        this.replyContent.set('');
    }

    deleteComment(commentId: number) {
        if (!confirm('Are you sure you want to delete this comment?')) {
            return;
        }

        this.commentService.deleteComment(this.postId, commentId, this.userId)
            .subscribe({
                next: () => this.loadComments(),
                error: (error) => {
                    console.error('Error deleting comment:', error);
                    this.errorMessage.set('Failed to delete comment');
                }
            });
    }

    loadPage(page: number) {
        this.currentPage.set(page);
        this.loadComments();
    }

    formatDate(date: string): string {
        const commentDate = new Date(date);
        const now = new Date();
        const diffMs = now.getTime() - commentDate.getTime();
        const diffMins = Math.floor(diffMs / 60000);
        const diffHours = Math.floor(diffMs / 3600000);
        const diffDays = Math.floor(diffMs / 86400000);

        if (diffMins < 1) return 'Just now';
        if (diffMins < 60) return `${diffMins}m ago`;
        if (diffHours < 24) return `${diffHours}h ago`;
        if (diffDays < 7) return `${diffDays}d ago`;
        
        return commentDate.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric' 
        });
    }
}
