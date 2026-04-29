# Guide d'Intégration Frontend - Réactions et Commentaires

## Services Angular à Créer

### 1. Reaction Service

```typescript
// src/app/services/reaction.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ReactionCounts {
  counts: {
    LIKE: number;
    HEART: number;
    SUPPORT: number;
  };
  userReaction: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ReactionService {
  private apiUrl = 'http://localhost:8069/api/posts';

  constructor(private http: HttpClient) {}

  toggleReaction(postId: number, userId: number, type: 'LIKE' | 'HEART' | 'SUPPORT'): Observable<ReactionCounts> {
    return this.http.post<ReactionCounts>(
      `${this.apiUrl}/${postId}/reactions/toggle`,
      { userId, type }
    );
  }

  getReactionCounts(postId: number, userId?: number): Observable<ReactionCounts> {
    const params = userId ? { userId: userId.toString() } : {};
    return this.http.get<ReactionCounts>(
      `${this.apiUrl}/${postId}/reactions`,
      { params }
    );
  }
}
```

### 2. Comment Service

```typescript
// src/app/services/comment.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comment {
  id: number;
  postId: number;
  userId: number;
  content: string;
  parentCommentId: number | null;
  createdAt: string;
  updatedAt: string;
  replies: Comment[];
}

export interface CommentPage {
  content: Comment[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = 'http://localhost:8069/api/posts';

  constructor(private http: HttpClient) {}

  addComment(postId: number, userId: number, content: string, parentCommentId?: number): Observable<Comment> {
    return this.http.post<Comment>(
      `${this.apiUrl}/${postId}/comments`,
      { userId, content, parentCommentId }
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
```

## Composants Angular

### 1. Reaction Component

```typescript
// src/app/components/post-reactions/post-reactions.component.ts
import { Component, Input, OnInit } from '@angular/core';
import { ReactionService, ReactionCounts } from '../../services/reaction.service';

@Component({
  selector: 'app-post-reactions',
  template: `
    <div class="reactions">
      <button 
        class="reaction-btn" 
        [class.active]="reactionCounts?.userReaction === 'LIKE'"
        (click)="toggleReaction('LIKE')">
        👍 {{ reactionCounts?.counts.LIKE || 0 }}
      </button>
      <button 
        class="reaction-btn" 
        [class.active]="reactionCounts?.userReaction === 'HEART'"
        (click)="toggleReaction('HEART')">
        ❤️ {{ reactionCounts?.counts.HEART || 0 }}
      </button>
      <button 
        class="reaction-btn" 
        [class.active]="reactionCounts?.userReaction === 'SUPPORT'"
        (click)="toggleReaction('SUPPORT')">
        🤝 {{ reactionCounts?.counts.SUPPORT || 0 }}
      </button>
    </div>
  `,
  styles: [`
    .reactions {
      display: flex;
      gap: 10px;
    }
    .reaction-btn {
      padding: 8px 16px;
      border: 1px solid #ddd;
      border-radius: 20px;
      background: white;
      cursor: pointer;
      transition: all 0.3s;
    }
    .reaction-btn:hover {
      background: #f0f0f0;
    }
    .reaction-btn.active {
      background: #e3f2fd;
      border-color: #2196f3;
      color: #2196f3;
    }
  `]
})
export class PostReactionsComponent implements OnInit {
  @Input() postId!: number;
  @Input() userId!: number;
  
  reactionCounts?: ReactionCounts;

  constructor(private reactionService: ReactionService) {}

  ngOnInit() {
    this.loadReactions();
  }

  loadReactions() {
    this.reactionService.getReactionCounts(this.postId, this.userId)
      .subscribe(counts => this.reactionCounts = counts);
  }

  toggleReaction(type: 'LIKE' | 'HEART' | 'SUPPORT') {
    this.reactionService.toggleReaction(this.postId, this.userId, type)
      .subscribe(counts => this.reactionCounts = counts);
  }
}
```

### 2. Comments Component

```typescript
// src/app/components/post-comments/post-comments.component.ts
import { Component, Input, OnInit } from '@angular/core';
import { CommentService, Comment, CommentPage } from '../../services/comment.service';

@Component({
  selector: 'app-post-comments',
  template: `
    <div class="comments-section">
      <h3>Commentaires ({{ totalComments }})</h3>
      
      <!-- Add Comment Form -->
      <div class="add-comment">
        <textarea 
          [(ngModel)]="newCommentContent"
          placeholder="Ajouter un commentaire..."
          maxlength="1000">
        </textarea>
        <button (click)="addComment()">Publier</button>
      </div>

      <!-- Comments List -->
      <div class="comments-list">
        <div *ngFor="let comment of comments" class="comment">
          <div class="comment-header">
            <span class="user">Utilisateur {{ comment.userId }}</span>
            <span class="date">{{ comment.createdAt | date:'short' }}</span>
          </div>
          <p class="comment-content">{{ comment.content }}</p>
          
          <div class="comment-actions">
            <button (click)="startReply(comment)">Répondre</button>
            <button 
              *ngIf="comment.userId === userId"
              (click)="deleteComment(comment.id)">
              Supprimer
            </button>
          </div>

          <!-- Reply Form -->
          <div *ngIf="replyingTo === comment.id" class="reply-form">
            <textarea 
              [(ngModel)]="replyContent"
              placeholder="Votre réponse..."
              maxlength="1000">
            </textarea>
            <button (click)="addReply(comment.id)">Répondre</button>
            <button (click)="cancelReply()">Annuler</button>
          </div>

          <!-- Replies -->
          <div class="replies" *ngIf="comment.replies?.length > 0">
            <div *ngFor="let reply of comment.replies" class="reply">
              <div class="comment-header">
                <span class="user">Utilisateur {{ reply.userId }}</span>
                <span class="date">{{ reply.createdAt | date:'short' }}</span>
              </div>
              <p class="comment-content">{{ reply.content }}</p>
              <button 
                *ngIf="reply.userId === userId"
                (click)="deleteComment(reply.id)">
                Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination" *ngIf="totalPages > 1">
        <button 
          [disabled]="currentPage === 0"
          (click)="loadPage(currentPage - 1)">
          Précédent
        </button>
        <span>Page {{ currentPage + 1 }} / {{ totalPages }}</span>
        <button 
          [disabled]="currentPage === totalPages - 1"
          (click)="loadPage(currentPage + 1)">
          Suivant
        </button>
      </div>
    </div>
  `,
  styles: [`
    .comments-section {
      margin-top: 20px;
    }
    .add-comment textarea {
      width: 100%;
      min-height: 80px;
      padding: 10px;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    .comment {
      border-left: 3px solid #2196f3;
      padding: 10px;
      margin: 10px 0;
      background: #f9f9f9;
    }
    .replies {
      margin-left: 30px;
      margin-top: 10px;
    }
    .reply {
      border-left: 3px solid #4caf50;
      padding: 10px;
      margin: 5px 0;
      background: white;
    }
  `]
})
export class PostCommentsComponent implements OnInit {
  @Input() postId!: number;
  @Input() userId!: number;

  comments: Comment[] = [];
  newCommentContent = '';
  replyContent = '';
  replyingTo: number | null = null;
  
  currentPage = 0;
  pageSize = 10;
  totalComments = 0;
  totalPages = 0;

  constructor(private commentService: CommentService) {}

  ngOnInit() {
    this.loadComments();
  }

  loadComments() {
    this.commentService.getComments(this.postId, this.currentPage, this.pageSize)
      .subscribe(page => {
        this.comments = page.content;
        this.totalComments = page.totalElements;
        this.totalPages = page.totalPages;
      });
  }

  addComment() {
    if (!this.newCommentContent.trim()) return;
    
    this.commentService.addComment(this.postId, this.userId, this.newCommentContent)
      .subscribe(() => {
        this.newCommentContent = '';
        this.loadComments();
      });
  }

  startReply(comment: Comment) {
    this.replyingTo = comment.id;
    this.replyContent = '';
  }

  addReply(parentCommentId: number) {
    if (!this.replyContent.trim()) return;
    
    this.commentService.addComment(this.postId, this.userId, this.replyContent, parentCommentId)
      .subscribe(() => {
        this.cancelReply();
        this.loadComments();
      });
  }

  cancelReply() {
    this.replyingTo = null;
    this.replyContent = '';
  }

  deleteComment(commentId: number) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce commentaire ?')) {
      this.commentService.deleteComment(this.postId, commentId, this.userId)
        .subscribe(() => this.loadComments());
    }
  }

  loadPage(page: number) {
    this.currentPage = page;
    this.loadComments();
  }
}
```

## Utilisation dans un Post Component

```typescript
// Dans votre composant de post
<div class="post">
  <div class="post-content">
    {{ post.content }}
  </div>
  
  <!-- Réactions -->
  <app-post-reactions 
    [postId]="post.id" 
    [userId]="currentUserId">
  </app-post-reactions>
  
  <!-- Commentaires -->
  <app-post-comments 
    [postId]="post.id" 
    [userId]="currentUserId">
  </app-post-comments>
</div>
```

## Configuration du Module

```typescript
// app.module.ts
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';

@NgModule({
  imports: [
    FormsModule,
    HttpClientModule,
    // ... autres imports
  ],
  // ...
})
```

## Notes d'Implémentation

1. Remplacez `currentUserId` par l'ID de l'utilisateur connecté (depuis votre service d'authentification)
2. Ajoutez la gestion d'erreurs appropriée dans les services
3. Personnalisez les styles selon votre design system
4. Ajoutez des animations pour améliorer l'UX
5. Implémentez le lazy loading pour les commentaires si nécessaire
