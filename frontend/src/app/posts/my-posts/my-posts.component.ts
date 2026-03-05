import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PostsService } from '../services/posts.service';
import { Post } from '../models/post.model';

@Component({
    selector: 'app-my-posts',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './my-posts.component.html',
    styleUrl: './my-posts.component.css'
})
export class MyPostsComponent implements OnInit {
    myPosts: any[] = [];
    isLoading: boolean = true;
    errorMessage: string = '';

    constructor(private postsService: PostsService) { }

    ngOnInit() {
        this.loadUserPosts();
    }

    loadUserPosts() {
        this.isLoading = true;
        this.errorMessage = '';

        // Since backend doesn't have user-specific endpoint, fetch all posts
        this.postsService.getPosts().subscribe({
            next: (response) => {
                // Show all posts as user's posts
                this.myPosts = response.map(p => ({
                    id: p.id,
                    content: p.content,
                    imageUrl: p.imageUrl,
                    createdAt: p.createdAt,
                    updatedAt: p.updatedAt
                }));

                this.isLoading = false;
            },
            error: (error) => {
                console.error('Error loading posts:', error);
                this.isLoading = false;

                this.errorMessage = error?.error?.message || 'Failed to load posts.';
            }
        });
    }

    deletePost(id: number) {
        if (confirm('Are you sure you want to delete this post?')) {
            this.postsService.deletePost(id).subscribe({
                next: () => {
                    this.myPosts = this.myPosts.filter(p => p.id !== id);
                },
                error: (error) => {
                    console.error('Error deleting post:', error);
                    alert('Failed to delete post');
                }
            });
        }
    }

    formatDate(date: string | undefined): string {
        if (!date) return 'Unknown';
        const postDate = new Date(date);
        return postDate.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
    }

    getStats() {
        return { totalPosts: this.myPosts.length };
    }
}
