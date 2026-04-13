import { ActivityType } from '../../admin/core/models/educational-activity.model';

/** Vignettes locales (style catalogue) si l’API ne fournit pas thumbnailUrl. */
export const DEFAULT_GAME_THUMB = '/assets/default/default-game.svg';

const THUMB_BY_TYPE: Partial<Record<ActivityType, string>> = {
    quiz: '/assets/games/quiz-memory.svg',
    cognitive_game: '/assets/games/image-recognition.svg',
    image_game: '/assets/games/memory-pairs.svg',
    content: '/assets/games/content.svg',
    video: '/assets/games/video.svg'
};

export function catalogThumbForActivity(type: ActivityType | undefined): string {
    return (type && THUMB_BY_TYPE[type]) || DEFAULT_GAME_THUMB;
}
