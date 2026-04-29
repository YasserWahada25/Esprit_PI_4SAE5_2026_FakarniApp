export interface User {
    id: string;
    nom: string;
    prenom: string;
    email: string;
}

export interface Post {
    id: number;
    content: string;
    imageUrl?: string;
    userId?: string;
    user?: User;
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
    userId?: string;
    user?: User;
    createdAt: string;
    updatedAt: string;
}

