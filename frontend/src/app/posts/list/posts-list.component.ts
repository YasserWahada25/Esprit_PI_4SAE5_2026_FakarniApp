import { Component, OnInit, signal, ChangeDetectorRef, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PostsService } from '../services/posts.service';
import { PostReactionsComponent } from '../shared/post-reactions/post-reactions.component';
import { PostCommentsComponent } from '../shared/post-comments/post-comments.component';

@Component({
    selector: 'app-posts-list',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule, PostReactionsComponent, PostCommentsComponent],
    templateUrl: './posts-list.component.html',
    styleUrl: './posts-list.component.css'
})
export class PostsListComponent implements OnInit {
    searchQuery: string = '';
    sortBy: string = 'recent';
    isLoading = signal(true);
    errorMessage = signal('');
    posts = signal<any[]>([]);
    expandedComments = signal<Set<number>>(new Set());

    private platformId = inject(PLATFORM_ID);
    private isBrowser: boolean;

    constructor(
        private postsService: PostsService,
        private cdr: ChangeDetectorRef,
        private router: Router
    ) {
        this.isBrowser = isPlatformBrowser(this.platformId);
    }

    ngOnInit() {
        // Only load posts in browser, not during SSR
        if (this.isBrowser) {
            // Small delay to ensure browser is fully ready
            setTimeout(() => {
                this.loadPosts();
            }, 100);
        } else {
            this.isLoading.set(false);
        }
    }

    loadPosts() {
        this.isLoading.set(true);
        this.errorMessage.set('');
        
        this.postsService.getPosts().subscribe({
            next: (response) => {
                // Transform response to Post model
                const postsData = response.map((p, index) => ({
                    id: p.id,
                    content: p.content,
                    imageUrl: p.imageUrl,
                    createdAt: p.createdAt,
                    updatedAt: p.updatedAt
                }));
                this.posts.set(postsData);
                this.isLoading.set(false);
                this.cdr.markForCheck();
            },
            error: (error) => {
                console.error('Error loading posts:', error);
                this.isLoading.set(false);
                
                // Fallback: show error message only, no demo posts
                this.posts.set([]);
                
                if (error.status === 0) {
                    this.errorMessage.set('Unable to connect to the server. Make sure the backend services (Gateway and Post-Service) are running.');
                } else {
                    this.errorMessage.set(error?.error?.message || 'Failed to load posts');
                }
                this.cdr.markForCheck();
            }
        });
    }

    get filteredPosts(): any[] {
        let filtered = this.posts();

        // Filter by search query (content only)
        if (this.searchQuery.trim()) {
            const query = this.searchQuery.toLowerCase();
            filtered = filtered.filter(p =>
                p.content.toLowerCase().includes(query)
            );
        }

        // Sort
        if (this.sortBy === 'recent') {
            filtered.sort((a, b) => {
                const dateA = new Date(a.createdAt || 0).getTime();
                const dateB = new Date(b.createdAt || 0).getTime();
                return dateB - dateA;
            });
        }

        return filtered;
    }

    toggleLike(post: any) {
        // Like functionality not available in this backend version
        console.log('Like functionality not available');
    }

    formatDate(date: string | undefined): string {
        if (!date) return 'Unknown';

        const postDate = new Date(date);
        const today = new Date();
        const yesterday = new Date(today);
        yesterday.setDate(yesterday.getDate() - 1);

        if (postDate.toDateString() === today.toDateString()) {
            return 'Today';
        } else if (postDate.toDateString() === yesterday.toDateString()) {
            return 'Yesterday';
        } else {
            return postDate.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        }
    }

    deletePost(postId: number) {
        if (!confirm('Are you sure you want to delete this post? This action cannot be undone.')) {
            return;
        }

        // Add deleting class for animation
        const postElement = document.querySelector(`[data-post-id="${postId}"]`);
        if (postElement) {
            postElement.classList.add('deleting');
        }

        // Wait for animation before actually deleting (500ms to match CSS animation)
        setTimeout(() => {
            this.postsService.deletePost(postId).subscribe({
                next: () => {
                    // Remove post from local array
                    const updatedPosts = this.posts().filter(p => p.id !== postId);
                    this.posts.set(updatedPosts);
                    this.cdr.markForCheck();
                },
                error: (error) => {
                    console.error('Error deleting post:', error);
                    this.errorMessage.set('Failed to delete post. Please try again.');
                    // Remove deleting class on error
                    if (postElement) {
                        postElement.classList.remove('deleting');
                    }
                    this.cdr.markForCheck();
                }
            });
        }, 500);
    }

    editPost(postId: number) {
        // Navigate to edit page
        this.router.navigate(['/posts/edit', postId]);
    }

    toggleComments(postId: number) {
        const expanded = this.expandedComments();
        if (expanded.has(postId)) {
            expanded.delete(postId);
        } else {
            expanded.add(postId);
        }
        this.expandedComments.set(new Set(expanded));
    }

    isCommentsExpanded(postId: number): boolean {
        return this.expandedComments().has(postId);
    }
}