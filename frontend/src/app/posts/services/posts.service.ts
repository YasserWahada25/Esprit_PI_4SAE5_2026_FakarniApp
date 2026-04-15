import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject, of } from 'rxjs';
import { Post, CreatePostRequest, PostResponse } from '../models/post.model';

@Injectable({
    providedIn: 'root'
})
export class PostsService {
    // Use proxy configuration - Angular will forward /api to localhost:8090
    private apiUrl = '/api/posts';

    private postsSubject = new BehaviorSubject<Post[]>([]);
    public posts$ = this.postsSubject.asObservable();

    private platformId = inject(PLATFORM_ID);

    constructor(private http: HttpClient) {
        // Don't load posts in constructor - let components call it explicitly
    }

    /**
     * Get all posts
     */
    getPosts(): Observable<PostResponse[]> {
        return this.http.get<PostResponse[]>(`${this.apiUrl}`);
    }

    /**
     * Get post by ID
     */
    getPostById(id: number): Observable<PostResponse> {
        return this.http.get<PostResponse>(`${this.apiUrl}/${id}`);
    }

    /**
     * Create a new post
     */
    createPost(request: CreatePostRequest): Observable<PostResponse> {
        const headers = new HttpHeaders({
            'Content-Type': 'application/json'
        });

        return this.http.post<PostResponse>(
            `${this.apiUrl}`,
            request,
            { headers }
        );
    }

    /**
     * Update a post
     */
    updatePost(id: number, request: CreatePostRequest): Observable<PostResponse> {
        return this.http.put<PostResponse>(
            `${this.apiUrl}/${id}`,
            request
        );
    }

    /**
     * Delete a post
     */
    deletePost(id: number): Observable<any> {
        return this.http.delete(`${this.apiUrl}/${id}`);
    }

    /**
     * Load posts from database and update cache
     */
    loadPosts(): void {
        this.getPosts().subscribe(
            (posts: PostResponse[]) => {
                const formattedPosts = posts.map(p => this.formatPost(p));
                this.postsSubject.next(formattedPosts);
            },
            (error) => {
                console.error('Error loading posts from database:', error);
            }
        );
    }

    /**
     * Format backend response to frontend model
     */
    private formatPost(response: PostResponse): Post {
        return {
            id: response.id,
            content: response.content,
            imageUrl: response.imageUrl,
            createdAt: response.createdAt,
            updatedAt: response.updatedAt
        };
    }
}
