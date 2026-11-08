import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReactionCounts, ReactionRequest, ReactionType } from '../models/reaction.model';

@Injectable({
    providedIn: 'root'
})
export class ReactionService {
    private apiUrl = '/api/posts';

    constructor(private http: HttpClient) {}

    toggleReaction(postId: number, userId: string, type: ReactionType): Observable<ReactionCounts> {
        const request: ReactionRequest = { userId, type };
        return this.http.post<ReactionCounts>(
            `${this.apiUrl}/${postId}/reactions/toggle`,
            request
        );
    }

    getReactionCounts(postId: number, userId?: string | null): Observable<ReactionCounts> {
        let params = new HttpParams();
        if (userId) {
            params = params.set('userId', userId);
        }
        
        return this.http.get<ReactionCounts>(
            `${this.apiUrl}/${postId}/reactions`,
            { params }
        );
    }
}
