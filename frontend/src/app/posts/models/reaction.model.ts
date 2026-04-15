export type ReactionType = 'LIKE' | 'HEART' | 'SUPPORT';

export interface ReactionCounts {
    counts: {
        LIKE: number;
        HEART: number;
        SUPPORT: number;
    };
    userReaction: ReactionType | null;
}

export interface ReactionRequest {
    userId: number;
    type: ReactionType;
}
