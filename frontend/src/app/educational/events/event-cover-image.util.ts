/**
 * Illustration d’en-tête pour les cartes événement.
 *
 * Choisit une **photo thématique** selon des mots-clés dans le titre et la description
 * (stimulation cognitive, café, aidants, musique, etc.) — pas une image aléatoire sans rapport.
 * Variante parmi quelques photos du même thème selon un hash stable (changement de texte → autre photo du thème).
 *
 * Pour une image strictement « générée » par IA à partir du texte : API images + stockage.
 */
export interface EventCoverSource {
    id: number;
    title: string;
    description?: string | null;
}

export type EventVisualTheme =
    | 'cognitive_memory'
    | 'social_cafe'
    | 'caregiving_family'
    | 'wellness_movement'
    | 'nutrition'
    | 'culture_outing'
    | 'music_creativity'
    | 'information_session'
    | 'safety_practical'
    | 'generic_support';

/** Mots-clés (sans accents, minuscules côté comparaison). */
const THEME_KEYWORDS: Record<EventVisualTheme, readonly string[]> = {
    cognitive_memory: [
        'memoire',
        'mémoire',
        'memory',
        'cognitif',
        'cognitive',
        'stimulation',
        'attention',
        'neuro',
        'trouble neuro',
        'quiz',
        'association',
        'image',
        'puzzle'
    ],
    social_cafe: [
        'cafe',
        'café',
        'coffee',
        'convivial',
        'gouter',
        'goûter',
        'the ',
        'thé ',
        'pause cafe',
        'pause café',
        'echanger',
        'échanger'
    ],
    caregiving_family: [
        'aidant',
        'aidants',
        'proche',
        'proches',
        'famille',
        'accompagn',
        'soignant',
        'paroles',
        'psychologue',
        'emotion',
        'émotion'
    ],
    wellness_movement: [
        'gym',
        'relax',
        'yoga',
        'marche',
        'mouvement',
        'plein air',
        'sport',
        'bien-etre',
        'bien-être',
        'respiration',
        'douc'
    ],
    nutrition: [
        'nutrition',
        'manger',
        'repas',
        'diet',
        'diét',
        'aliment',
        'hydrat',
        'appetit',
        'appétit'
    ],
    culture_outing: [
        'musee',
        'musée',
        'culture',
        'visite',
        'sortie',
        'exposition',
        'galerie',
        'patrimoine'
    ],
    music_creativity: [
        'musique',
        'chant',
        'rythme',
        'instrument',
        'piano',
        'guitar',
        'creatif',
        'créatif',
        'atelier mus'
    ],
    information_session: [
        'comprendre',
        'conference',
        'conférence',
        'information',
        'present',
        'présent',
        'questions',
        'stade',
        'maladie',
        'alzheimer',
        'mdph',
        'ressource'
    ],
    safety_practical: [
        'arnaque',
        'securite',
        'sécurité',
        'telephone',
        'téléphone',
        'frauda',
        'compte',
        'demarche',
        'démarche'
    ],
    generic_support: []
};

/** Photos Unsplash (thème soin / lien social / calme — pas de sujets hors contexte). */
const THEME_URLS: Record<EventVisualTheme, readonly string[]> = {
    cognitive_memory: [
        'https://images.unsplash.com/photo-1503676260728-1c00da094a0b?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1456513080510-7bf3a84b629f?auto=format&fit=crop&w=800&q=72'
    ],
    social_cafe: [
        'https://images.unsplash.com/photo-1445116303074-79421f590b1e?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=72'
    ],
    caregiving_family: [
        'https://images.unsplash.com/photo-1511895426328-dc8714191300?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1581578949510-fa7315c4cdeb?auto=format&fit=crop&w=800&q=72'
    ],
    wellness_movement: [
        'https://images.unsplash.com/photo-1544367567-0f2fcb09e99d?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1506126613408-eca07ce68773?auto=format&fit=crop&w=800&q=72'
    ],
    nutrition: [
        'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=800&q=72'
    ],
    culture_outing: [
        'https://images.unsplash.com/photo-1565129763404-92109b3556b5?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1577083505173-ee8333779ef9?auto=format&fit=crop&w=800&q=72'
    ],
    music_creativity: [
        'https://images.unsplash.com/photo-1510915369354-204e0c3121eb?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1520523839890-bd9b0f27788e?auto=format&fit=crop&w=800&q=72'
    ],
    information_session: [
        'https://images.unsplash.com/photo-1552664730-d307ca884978?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1524178232363-1fb2b075b655?auto=format&fit=crop&w=800&q=72'
    ],
    safety_practical: [
        'https://images.unsplash.com/photo-1516321496887-815b6c1398d4?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1554224155-6726b3ff858f?auto=format&fit=crop&w=800&q=72'
    ],
    generic_support: [
        'https://images.unsplash.com/photo-1516293963049-f932ee1d4886?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1495567720983-ce4129699a78?auto=format&fit=crop&w=800&q=72',
        'https://images.unsplash.com/photo-1464828026522-6ace7af9c22c?auto=format&fit=crop&w=800&q=72'
    ]
};

const THEME_GRADIENTS: Record<EventVisualTheme, string> = {
    cognitive_memory: 'linear-gradient(135deg, #5c4d7d 0%, #7b6fa2 100%)',
    social_cafe: 'linear-gradient(135deg, #6d4c41 0%, #8d6e63 100%)',
    caregiving_family: 'linear-gradient(135deg, #7b2d8b 0%, #9c4dcc 100%)',
    wellness_movement: 'linear-gradient(135deg, #2e7d6d 0%, #4db6ac 100%)',
    nutrition: 'linear-gradient(135deg, #c67c38 0%, #e89b4e 100%)',
    culture_outing: 'linear-gradient(135deg, #455a64 0%, #78909c 100%)',
    music_creativity: 'linear-gradient(135deg, #5e35b1 0%, #7e57c2 100%)',
    information_session: 'linear-gradient(135deg, #1565c0 0%, #42a5f5 100%)',
    safety_practical: 'linear-gradient(135deg, #37474f 0%, #607d8b 100%)',
    generic_support: 'linear-gradient(135deg, #6a1b9a 0%, #8e24aa 100%)'
};

function stripAccents(s: string): string {
    return s.normalize('NFD').replace(/\p{M}/gu, '');
}

function normalizeForMatch(s: string): string {
    return stripAccents(s.toLowerCase());
}

function djb2Hex(input: string): string {
    let h = 5381;
    for (let i = 0; i < input.length; i++) {
        h = (h * 33) ^ input.charCodeAt(i);
    }
    return (h >>> 0).toString(16);
}

export function eventCoverSeed(event: EventCoverSource): string {
    const desc = (event.description ?? '').trim();
    const payload = `${event.id}|${(event.title ?? '').trim()}|${desc}`;
    return djb2Hex(payload);
}

export function detectEventVisualTheme(event: EventCoverSource): EventVisualTheme {
    const blob = normalizeForMatch(`${event.title ?? ''} ${event.description ?? ''}`);
    let best: EventVisualTheme = 'generic_support';
    let bestScore = 0;

    (Object.keys(THEME_KEYWORDS) as EventVisualTheme[]).forEach(theme => {
        if (theme === 'generic_support') {
            return;
        }
        let score = 0;
        for (const kw of THEME_KEYWORDS[theme]) {
            const k = normalizeForMatch(kw);
            if (k.length > 0 && blob.includes(k)) {
                score += k.length >= 6 ? 2 : 1;
            }
        }
        if (score > bestScore) {
            bestScore = score;
            best = theme;
        }
    });

    return best;
}

export function eventCoverImageUrl(event: EventCoverSource): string {
    const theme = detectEventVisualTheme(event);
    const urls = THEME_URLS[theme];
    const seed = eventCoverSeed(event);
    const idx = (parseInt(seed.slice(0, 8), 16) || 0) % urls.length;
    return urls[idx];
}

export function eventCoverGradientCss(event: EventCoverSource): string {
    return THEME_GRADIENTS[detectEventVisualTheme(event)];
}
