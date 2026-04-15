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

export interface CommentRequest {
    userId: number;
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
