export interface Post {
    id: number;
    content: string;
    imageUrl?: string;
    createdAt: string;
    updatedAt: string;
}

export interface CreatePostRequest {
    content: string;
    imageUrl?: string;
}

export interface PostResponse {
    id: number;
    content: string;
    imageUrl?: string;
    createdAt: string;
    updatedAt: string;
}

