import { Component, OnInit, signal, computed, ChangeDetectorRef, PLATFORM_ID, inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { PostsService } from '../services/posts.service';

@Component({
    selector: 'app-posts-list',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterModule],
    templateUrl: './posts-list.component.html',
    styleUrl: './posts-list.component.css'
})
export class PostsListComponent implements OnInit {
    searchQuery: string = '';
    sortBy: string = 'recent';
    isLoading = signal(true);
    errorMessage = signal('');
    posts = signal<any[]>([]);

    private platformId = inject(PLATFORM_ID);

    constructor(
        private postsService: PostsService,
        private cdr: ChangeDetectorRef,
        private router: Router
    ) { }

    ngOnInit() {
        // Only load posts in browser, not during SSR
        if (isPlatformBrowser(this.platformId)) {
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

        // Wait for animation before actually deleting
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
                }
            });
        }, 100);
    }

    editPost(postId: number) {
        // Navigate to edit page
        this.router.navigate(['/posts/edit', postId]);
    }
}