export interface EducationalActivity {
    id: number;
    name: string;
    type: ActivityType;
    description: string;
    createdDate: Date;
    status: ActivityStatus;
    content: ActivityContent;
    /** Réponse API activite-educative-service */
    gameType?: 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | null;
    iconKey?: string | null;
    /** Seuil API (0–100) pour marquer la session SUCCESS vs FAILURE */
    scoreThreshold?: number | null;
    latestScorePercent?: number | null;
    thumbnailUrl?: string | null;
}

/** quiz = QCM texte ou images (MEMORY_QUIZ / IMAGE_RECOGNITION) ; image_game = memory paires (MEMORY_MATCH) */
export type ActivityType = 'quiz' | 'cognitive_game' | 'image_game' | 'video' | 'content';

export type ActivityStatus = 'active' | 'inactive';

export interface ActivityContent {
    questions?: QuizQuestion[];
    gameConfig?: GameConfig;
    videoUrl?: string;
    duration?: number;
}

export interface QuizQuestion {
    question: string;
    options: string[];
    correctAnswer: number;
}

export interface GameConfig {
    difficulty: 'easy' | 'medium' | 'hard';
    timeLimit?: number;
    instructions: string;
}
