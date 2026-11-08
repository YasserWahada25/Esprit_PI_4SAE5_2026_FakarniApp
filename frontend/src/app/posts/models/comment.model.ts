export interface CommentAuthor {
    id: string;
    nom: string;
    prenom: string;
    email?: string;
}

export interface Comment {
    id: number;
    postId: number;
    /** Id User-Service (chaîne Mongo) de l'auteur. */
    userId: string;
    /** Auteur résolu par Post-Service ; absent si User-Service est indisponible. */
    user?: CommentAuthor | null;
    content: string;
    parentCommentId: number | null;
    createdAt: string;
    updatedAt: string;
    replies: Comment[];
}

export interface CommentRequest {
    userId: string;
    content: string;
    parentCommentId?: number;
}

export interface CommentPage {
    content: Comment[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
}
