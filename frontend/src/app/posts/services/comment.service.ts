import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment, CommentRequest, CommentPage } from '../models/comment.model';

@Injectable({
    providedIn: 'root'
})
export class CommentService {
    private apiUrl = '/api/posts';

    constructor(private http: HttpClient) {}

    addComment(postId: number, userId: number, content: string, parentCommentId?: number): Observable<Comment> {
        const request: CommentRequest = { userId, content, parentCommentId };
        return this.http.post<Comment>(
            `${this.apiUrl}/${postId}/comments`,
            request
        );
    }

    getComments(postId: number, page: number = 0, size: number = 10): Observable<CommentPage> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        
        return this.http.get<CommentPage>(
            `${this.apiUrl}/${postId}/comments`,
            { params }
        );
    }

    deleteComment(postId: number, commentId: number, userId: number): Observable<void> {
        const params = new HttpParams().set('userId', userId.toString());
        return this.http.delete<void>(
            `${this.apiUrl}/${postId}/comments/${commentId}`,
            { params }
        );
    }
}
