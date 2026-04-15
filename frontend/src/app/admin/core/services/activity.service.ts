import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';
import { Observable, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
    ActivityContent,
    ActivityStatus,
    ActivityType,
    EducationalActivity
} from '../models/educational-activity.model';

/** Aligné sur le microservice activite-educative-service */
interface ActiviteEducativeResponseDto {
    id: number;
    title: string;
    description: string | null;
    type: 'CONTENT' | 'GAME' | 'QUIZ' | 'VIDEO' | string;
    gameType?: 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | 'PUZZLE' | null;
    iconKey?: string | null;
    scoreThreshold?: number | null;
    createdAt: string;
    status: 'ACTIVE' | 'INACTIVE' | string;
    latestScorePercent?: number | null;
    thumbnailUrl?: string | null;
    updatedAt?: string | null;
}

export interface QuestionAdminDto {
    id: number;
    activityId?: number;
    orderIndex: number;
    prompt: string;
    imageUrl?: string | null;
    questionImageUrl?: string | null;
    options: string[];
    correctAnswer: string;
}

/** Corps JSON envoyé dans la part {@code question} (multipart). */
export interface QuestionAdminWriteDto {
    orderIndex: number;
    prompt: string;
    imageUrl?: string | null;
    options: string[];
    correctAnswer: string;
}

interface ActiviteEducativeRequestDto {
    title: string;
    description?: string | null;
    type: 'CONTENT' | 'GAME' | 'VIDEO';
    gameType?: 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | 'PUZZLE' | null;
    scoreThreshold?: number | null;
    status: 'ACTIVE' | 'INACTIVE';
    iconKey?: string | null;
    thumbnailUrl?: string | null;
}

/** Suffixes techniques dans la description API (pas de colonnes dédiées côté microservice). */
const VIDEO_URL_MARKER = '\n__VIDEO_URL__:';
const VIDEO_DURATION_MARKER = '\n__VIDEO_DURATION__:';

export interface QuizOptionPlayDto {
    label: string;
    imageUrl?: string | null;
}

export interface QuestionPlayDto {
    id: number;
    orderIndex: number;
    prompt: string;
    imageUrl?: string | null;
    /** Alias API (identique à imageUrl pour les quiz image). */
    questionImageUrl?: string | null;
    options: string[];
    quizOptions?: QuizOptionPlayDto[];
}

export interface ImageCardPlayDto {
    id: number;
    imageUrl: string;
    backLabel?: string | null;
}

export interface GameSessionStartResponseDto {
    sessionId: number;
    activityId: number;
    userId: number;
    status: string;
    totalQuestions: number;
    /** MEMORY_QUIZ, IMAGE_RECOGNITION (QCM), MEMORY_MATCH (paires). */
    gameType?: 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | 'PUZZLE' | null;
    questions: QuestionPlayDto[];
    imageCards?: ImageCardPlayDto[];
}

export interface ImageCardPublicDto {
    id: number;
    imageUrl: string;
    label?: string | null;
}

export interface MemoryMoveResponseDto {
    match: boolean;
    movesCount: number;
    pairsFound: number;
    pairTotal: number;
    gameCompleted: boolean;
    sessionResult?: GameSessionResultDto | null;
}

export interface SessionAnswerDetailDto {
    questionId: number;
    prompt: string;
    userAnswer: string;
    expectedAnswer: string;
    correct: boolean;
    imageUrl?: string | null;
    questionImageUrl?: string | null;
}

export interface GameSessionResultDto {
    sessionId: number;
    activityId: number;
    activityTitle?: string;
    userId?: number;
    status: string;
    totalQuestions?: number;
    correctCount?: number;
    scorePercent?: number | null;
    score?: number | null;
    scoreMax?: number | null;
    percentage?: number | null;
    dateCompleted?: string | null;
    startedAt?: string;
    finishedAt?: string | null;
    answers?: SessionAnswerDetailDto[];
}

export interface SubmitAnswerResponseDto {
    correct: boolean;
    correctAnswersSoFar: number;
    answeredCount: number;
    sessionFinished?: boolean;
    sessionResult?: GameSessionResultDto | null;
}

@Injectable({
    providedIn: 'root'
})
export class ActivityService {
    /** Ex. /api/activities ou http://host:8090/api/activities */
    private readonly apiUrl: string;
    /** Ex. /api/activities/game-sessions (start, finish, move memory, résultat GET …/{id}). */
    private readonly gameSessionsRoot: string;

    constructor(private http: HttpClient) {
        const base = (environment.apiBaseUrl ?? '').trim().replace(/\/$/, '');
        this.apiUrl = base ? `${base}/api/activities` : '/api/activities';
        this.gameSessionsRoot = base ? `${base}/api/activities/game-sessions` : '/api/activities/game-sessions';
    }

    getActivities(userId?: number): Observable<EducationalActivity[]> {
        let params = new HttpParams();
        if (userId != null) {
            params = params.set('userId', String(userId));
        }
        return this.http.get<ActiviteEducativeResponseDto[]>(this.apiUrl, { params }).pipe(
            map(raw => {
                const rows = Array.isArray(raw) ? raw : [];
                return rows
                    .map(dto => this.safeToEducationalActivity(dto))
                    .filter((x): x is EducationalActivity => x != null);
            }),
            catchError(err => {
                console.error('[ActivityService] getActivities failed', err);
                return throwError(() => err);
            })
        );
    }

    getActivityById(id: number, userId?: number): Observable<EducationalActivity | undefined> {
        let params = new HttpParams();
        if (userId != null) {
            params = params.set('userId', String(userId));
        }
        return this.http.get<ActiviteEducativeResponseDto>(`${this.apiUrl}/${id}`, { params }).pipe(
            map(dto => this.safeToEducationalActivity(dto) ?? undefined),
            catchError(err => {
                console.error('[ActivityService] getActivityById failed', id, err);
                return throwError(() => err);
            })
        );
    }

    getActivitiesByType(type: ActivityType): Observable<EducationalActivity[]> {
        return this.getActivities().pipe(
            map(activities => activities.filter(a => a.type === type))
        );
    }

    getActivitiesByStatus(status: ActivityStatus): Observable<EducationalActivity[]> {
        return this.getActivities().pipe(
            map(activities => activities.filter(a => a.status === status))
        );
    }

    createActivity(
        activity: Omit<EducationalActivity, 'id'>,
        thumbnailFile?: File | null
    ): Observable<EducationalActivity> {
        return this.postActivityMedia(activity, thumbnailFile ?? null);
    }

    updateActivity(
        id: number,
        activity: Partial<EducationalActivity>,
        thumbnailFile?: File | null
    ): Observable<EducationalActivity | undefined> {
        return this.putActivityMedia(id, activity, thumbnailFile ?? null);
    }

    private postActivityMedia(
        activity: Omit<EducationalActivity, 'id'>,
        thumbnailFile: File | null
    ): Observable<EducationalActivity> {
        const fd = new FormData();
        fd.append(
            'activity',
            new Blob([JSON.stringify(this.toRequestDto(activity))], { type: 'application/json' })
        );
        if (thumbnailFile) {
            fd.append('thumbnail', thumbnailFile, thumbnailFile.name);
        }
        return this.http.post<ActiviteEducativeResponseDto>(`${this.apiUrl}/media`, fd).pipe(
            map(dto => this.toEducationalActivity(dto))
        );
    }

    private putActivityMedia(
        id: number,
        activity: Partial<EducationalActivity>,
        thumbnailFile: File | null
    ): Observable<EducationalActivity | undefined> {
        const fd = new FormData();
        fd.append(
            'activity',
            new Blob([JSON.stringify(this.toRequestDto(activity))], { type: 'application/json' })
        );
        if (thumbnailFile) {
            fd.append('thumbnail', thumbnailFile, thumbnailFile.name);
        }
        return this.http.put<ActiviteEducativeResponseDto>(`${this.apiUrl}/${id}/media`, fd).pipe(
            map(dto => this.toEducationalActivity(dto))
        );
    }

    deleteActivity(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    /** Questions d’une activité (admin). */
    getQuestions(activityId: number): Observable<QuestionAdminDto[]> {
        return this.http.get<unknown[]>(`${this.apiUrl}/${activityId}/questions`).pipe(
            map(raw => (Array.isArray(raw) ? raw : []).map((q: unknown) => this.normalizeQuestionAdmin(q)))
        );
    }

    deleteQuestion(activityId: number, questionId: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${activityId}/questions/${questionId}`);
    }

    createQuestionMultipart(
        activityId: number,
        body: QuestionAdminWriteDto,
        imageFile: File | null
    ): Observable<QuestionAdminDto> {
        const fd = new FormData();
        fd.append(
            'question',
            new Blob([JSON.stringify(body)], { type: 'application/json' })
        );
        if (imageFile) {
            fd.append('image', imageFile, imageFile.name);
        }
        return this.http
            .post<unknown>(`${this.apiUrl}/${activityId}/questions/media`, fd)
            .pipe(map(raw => this.normalizeQuestionAdmin(raw)));
    }

    updateQuestionMultipart(
        activityId: number,
        questionId: number,
        body: QuestionAdminWriteDto,
        imageFile: File | null
    ): Observable<QuestionAdminDto> {
        const fd = new FormData();
        fd.append(
            'question',
            new Blob([JSON.stringify(body)], { type: 'application/json' })
        );
        if (imageFile) {
            fd.append('image', imageFile, imageFile.name);
        }
        return this.http
            .put<unknown>(`${this.apiUrl}/${activityId}/questions/${questionId}/media`, fd)
            .pipe(map(raw => this.normalizeQuestionAdmin(raw)));
    }

    startGameSession(activityId: number, userId: number): Observable<GameSessionStartResponseDto> {
        return this.http
            .post<unknown>(`${this.apiUrl}/${activityId}/start`, { userId })
            .pipe(map(raw => this.normalizeGameSessionStartResponse(raw)));
    }

    submitAnswer(
        activityId: number,
        sessionId: number,
        questionId: number,
        answer: string
    ): Observable<SubmitAnswerResponseDto> {
        return this.http
            .post<unknown>(`${this.apiUrl}/${activityId}/submit-answer`, {
                sessionId,
                questionId,
                answer
            })
            .pipe(map(raw => this.normalizeSubmitAnswerResponse(raw)));
    }

    finishSession(sessionId: number): Observable<GameSessionResultDto> {
        return this.http
            .post<unknown>(`${this.gameSessionsRoot}/${sessionId}/finish`, null)
            .pipe(map(raw => this.normalizeGameSessionResult(raw)));
    }

    getImageCards(activityId: number): Observable<ImageCardPublicDto[]> {
        return this.http.get<unknown>(`${this.apiUrl}/${activityId}/image-cards`).pipe(
            map(raw => (Array.isArray(raw) ? raw : []).map((c: unknown) => this.normalizeImageCardPublic(c)))
        );
    }

    submitMemoryMove(sessionId: number, firstCardId: number, secondCardId: number): Observable<MemoryMoveResponseDto> {
        return this.http
            .post<unknown>(`${this.gameSessionsRoot}/${sessionId}/move`, {
                firstCardId,
                secondCardId
            })
            .pipe(map(raw => this.normalizeMemoryMoveResponse(raw)));
    }

    getGameSessionResultFlat(sessionId: number): Observable<GameSessionResultDto> {
        return this.http
            .get<unknown>(`${this.gameSessionsRoot}/${sessionId}`)
            .pipe(map(raw => this.normalizeGameSessionResult(raw)));
    }

    private normalizeSubmitAnswerResponse(raw: unknown): SubmitAnswerResponseDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const sr = o['sessionResult'] ?? o['session_result'];
        return {
            correct: Boolean(o['correct']),
            correctAnswersSoFar: Number(
                o['correctAnswersSoFar'] ?? o['correct_answers_so_far'] ?? 0
            ),
            answeredCount: Number(o['answeredCount'] ?? o['answered_count'] ?? 0),
            sessionFinished: Boolean(o['sessionFinished'] ?? o['session_finished']),
            sessionResult: sr ? this.normalizeGameSessionResult(sr) : null
        };
    }

    private normalizeGameSessionResult(raw: unknown): GameSessionResultDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const answersRaw = o['answers'];
        const answers = Array.isArray(answersRaw)
            ? answersRaw.map((a: unknown) => this.normalizeSessionAnswerDetail(a))
            : [];
        return {
            sessionId: Number(o['sessionId'] ?? o['session_id'] ?? 0),
            activityId: Number(o['activityId'] ?? o['activity_id'] ?? 0),
            activityTitle: o['activityTitle'] != null ? String(o['activityTitle']) : undefined,
            userId:
                o['userId'] != null || o['user_id'] != null
                    ? Number(o['userId'] ?? o['user_id'])
                    : undefined,
            status: String(o['status'] ?? ''),
            totalQuestions:
                o['totalQuestions'] != null || o['total_questions'] != null
                    ? Number(o['totalQuestions'] ?? o['total_questions'])
                    : undefined,
            correctCount:
                o['correctCount'] != null || o['correct_count'] != null
                    ? Number(o['correctCount'] ?? o['correct_count'])
                    : undefined,
            scorePercent: this.numOrNull(o['scorePercent'] ?? o['score_percent']),
            score: this.numOrNull(o['score']),
            scoreMax: this.numOrNull(o['scoreMax'] ?? o['score_max']),
            percentage: this.numOrNull(o['percentage']),
            dateCompleted:
                o['dateCompleted'] != null
                    ? String(o['dateCompleted'])
                    : o['date_completed'] != null
                      ? String(o['date_completed'])
                      : null,
            startedAt:
                o['startedAt'] != null ? String(o['startedAt']) : String(o['started_at'] ?? ''),
            finishedAt:
                o['finishedAt'] != null
                    ? String(o['finishedAt'])
                    : o['finished_at'] != null
                      ? String(o['finished_at'])
                      : null,
            answers
        };
    }

    private normalizeSessionAnswerDetail(raw: unknown): SessionAnswerDetailDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const img =
            o['questionImageUrl'] ??
            o['question_image_url'] ??
            o['imageUrl'] ??
            o['image_url'];
        const url = img != null && String(img).trim() !== '' ? String(img).trim() : null;
        return {
            questionId: Number(o['questionId'] ?? o['question_id'] ?? 0),
            prompt: String(o['prompt'] ?? ''),
            userAnswer: String(o['userAnswer'] ?? o['user_answer'] ?? ''),
            expectedAnswer: String(
                o['expectedAnswer'] ??
                    o['expected_answer'] ??
                    o['correctAnswer'] ??
                    o['correct_answer'] ??
                    ''
            ),
            correct: Boolean(o['correct']),
            imageUrl: url,
            questionImageUrl: url
        };
    }

    private numOrNull(v: unknown): number | null {
        if (v == null || v === '') {
            return null;
        }
        const n = Number(v);
        return Number.isNaN(n) ? null : n;
    }

    private normalizeGameSessionStartResponse(raw: unknown): GameSessionStartResponseDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const questionsRaw = o['questions'];
        const questions = Array.isArray(questionsRaw)
            ? questionsRaw.map((q: unknown) => this.normalizeQuestionPlayDto(q))
            : [];
        const cardsRaw = o['imageCards'] ?? o['image_cards'];
        const imageCards = Array.isArray(cardsRaw)
            ? cardsRaw.map((c: unknown) => this.normalizeImageCardPlay(c))
            : undefined;
        const gameType = this.normalizeApiGameType(o['gameType'] ?? o['game_type']);
        const fallbackTotal =
            questions.length > 0
                ? questions.length
                : imageCards && imageCards.length > 0
                  ? Math.floor(imageCards.length / 2)
                  : 0;
        return {
            sessionId: Number(o['sessionId'] ?? o['session_id'] ?? 0),
            activityId: Number(o['activityId'] ?? o['activity_id'] ?? 0),
            userId: Number(o['userId'] ?? o['user_id'] ?? 0),
            status: String(o['status'] ?? ''),
            totalQuestions: Number(
                o['totalQuestions'] ?? o['total_questions'] ?? fallbackTotal
            ),
            gameType,
            questions,
            imageCards
        };
    }

    private normalizeImageCardPlay(c: unknown): ImageCardPlayDto {
        const o = c && typeof c === 'object' ? (c as Record<string, unknown>) : {};
        const u = o['imageUrl'] ?? o['image_url'];
        return {
            id: Number(o['id'] ?? 0),
            imageUrl: u != null ? String(u) : '',
            backLabel: o['backLabel'] != null ? String(o['backLabel']) : o['back_label'] != null ? String(o['back_label']) : null
        };
    }

    private normalizeImageCardPublic(c: unknown): ImageCardPublicDto {
        const o = c && typeof c === 'object' ? (c as Record<string, unknown>) : {};
        const u = o['imageUrl'] ?? o['image_url'];
        return {
            id: Number(o['id'] ?? 0),
            imageUrl: u != null ? String(u) : '',
            label: o['label'] != null ? String(o['label']) : null
        };
    }

    private normalizeMemoryMoveResponse(raw: unknown): MemoryMoveResponseDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const sr = o['sessionResult'] ?? o['session_result'];
        return {
            match: Boolean(o['match']),
            movesCount: Number(o['movesCount'] ?? o['moves_count'] ?? 0),
            pairsFound: Number(o['pairsFound'] ?? o['pairs_found'] ?? 0),
            pairTotal: Number(o['pairTotal'] ?? o['pair_total'] ?? 0),
            gameCompleted: Boolean(o['gameCompleted'] ?? o['game_completed']),
            sessionResult: sr ? this.normalizeGameSessionResult(sr) : null
        };
    }

    private normalizeQuestionPlayDto(q: unknown): QuestionPlayDto {
        const o = q && typeof q === 'object' ? (q as Record<string, unknown>) : {};
        const rawUrl =
            o['questionImageUrl'] ?? o['question_image_url'] ?? o['imageUrl'] ?? o['image_url'];
        const imageUrl =
            rawUrl != null && String(rawUrl).trim() !== '' ? String(rawUrl).trim() : null;
        const opts = o['options'];
        const qoRaw = o['quizOptions'] ?? o['quiz_options'];
        let quizOptions: QuizOptionPlayDto[] | undefined;
        if (Array.isArray(qoRaw)) {
            quizOptions = qoRaw.map((x: unknown) => {
                const z = x && typeof x === 'object' ? (x as Record<string, unknown>) : {};
                const img = z['imageUrl'] ?? z['image_url'];
                return {
                    label: String(z['label'] ?? ''),
                    imageUrl:
                        img != null && String(img).trim() !== '' ? String(img).trim() : null
                };
            });
        }
        return {
            id: Number(o['id'] ?? 0),
            orderIndex: Number(o['orderIndex'] ?? o['order_index'] ?? 0),
            prompt: String(o['prompt'] ?? ''),
            imageUrl,
            questionImageUrl: imageUrl,
            options: Array.isArray(opts) ? (opts as string[]) : [],
            quizOptions
        };
    }

    private safeToEducationalActivity(dto: ActiviteEducativeResponseDto): EducationalActivity | null {
        try {
            return this.toEducationalActivity(dto);
        } catch (e) {
            console.warn('[ActivityService] skip malformed activity row', dto, e);
            return null;
        }
    }

    private toRequestDto(activity: Partial<EducationalActivity> & { name?: string; title?: string }): ActiviteEducativeRequestDto {
        const name = (activity as any)?.name ?? (activity as any)?.title ?? '';
        const rawDescription = activity?.description ?? '';
        const status = this.toDtoStatus(activity?.status as ActivityStatus | undefined);
        const type = activity?.type as ActivityType | undefined;
        const st = (activity as { scoreThreshold?: number | null })?.scoreThreshold;
        const videoUrl = activity.content?.videoUrl?.trim() ?? '';
        const durationMin = activity.content?.duration;
        const thumbnailUrl =
            (activity as { thumbnailUrl?: string | null }).thumbnailUrl?.trim() || undefined;

        if (type === 'quiz') {
            return {
                title: name,
                description: rawDescription,
                status,
                type: 'GAME',
                gameType: 'MEMORY_QUIZ',
                scoreThreshold: st ?? 60,
                iconKey: 'brain',
                thumbnailUrl
            };
        }
        if (type === 'cognitive_game') {
            return {
                title: name,
                description: rawDescription,
                status,
                type: 'GAME',
                gameType: 'IMAGE_RECOGNITION',
                scoreThreshold: st ?? 70,
                iconKey: 'image',
                thumbnailUrl
            };
        }
        if (type === 'image_game') {
            return {
                title: name,
                description: rawDescription,
                status,
                type: 'GAME',
                gameType: 'MEMORY_MATCH',
                scoreThreshold: st ?? 100,
                iconKey: 'image',
                thumbnailUrl
            };
        }
        if (type === 'puzzle_game') {
            return {
                title: name,
                description: rawDescription,
                status,
                type: 'GAME',
                gameType: 'PUZZLE',
                scoreThreshold: st ?? 100,
                iconKey: 'puzzle',
                thumbnailUrl
            };
        }
        if (type === 'video') {
            let desc = this.stripAllVideoMeta(rawDescription);
            if (videoUrl) {
                desc = `${desc}${VIDEO_URL_MARKER}${videoUrl}`;
            }
            if (durationMin != null && !Number.isNaN(Number(durationMin))) {
                desc = `${desc}${VIDEO_DURATION_MARKER}${Number(durationMin)}`;
            }
            return {
                title: name,
                description: desc,
                status,
                type: 'VIDEO',
                gameType: null,
                iconKey: 'video',
                thumbnailUrl
            };
        }
        if (type === 'content') {
            return {
                title: name,
                description: rawDescription,
                status,
                type: 'CONTENT',
                gameType: null,
                iconKey: 'book',
                thumbnailUrl
            };
        }
        return {
            title: name,
            description: rawDescription,
            status,
            type: 'CONTENT',
            gameType: null,
            iconKey: 'book',
            thumbnailUrl
        };
    }

    private normalizeQuestionAdmin(raw: unknown): QuestionAdminDto {
        const o = raw && typeof raw === 'object' ? (raw as Record<string, unknown>) : {};
        const opts = o['options'];
        const img =
            o['questionImageUrl'] ??
            o['question_image_url'] ??
            o['imageUrl'] ??
            o['image_url'];
        const url = img != null && String(img).trim() !== '' ? String(img).trim() : null;
        return {
            id: Number(o['id'] ?? 0),
            activityId:
                o['activityId'] != null || o['activity_id'] != null
                    ? Number(o['activityId'] ?? o['activity_id'])
                    : undefined,
            orderIndex: Number(o['orderIndex'] ?? o['order_index'] ?? 0),
            prompt: String(o['prompt'] ?? o['questionText'] ?? ''),
            imageUrl: url,
            questionImageUrl: url,
            options: Array.isArray(opts) ? (opts as string[]) : [],
            correctAnswer: String(o['correctAnswer'] ?? o['correct_answer'] ?? '')
        };
    }

    private stripDurationMarker(description: string): string {
        const i = description.lastIndexOf(VIDEO_DURATION_MARKER);
        if (i < 0) {
            return description.trimEnd();
        }
        return description.slice(0, i).trimEnd();
    }

    private stripVideoUrlMarker(description: string): string {
        const i = description.indexOf(VIDEO_URL_MARKER);
        if (i < 0) {
            return description.trimEnd();
        }
        return description.slice(0, i).trimEnd();
    }

    /** Retire URL + durée techniques en fin de description (édition vidéo). */
    private stripAllVideoMeta(description: string): string {
        return this.stripVideoUrlMarker(this.stripDurationMarker(description));
    }

    private splitVideoDescription(full: string): { text: string; videoUrl: string; duration?: number } {
        let rest = full.trimEnd();
        let duration: number | undefined;
        const di = rest.lastIndexOf(VIDEO_DURATION_MARKER);
        if (di >= 0) {
            const raw = rest.slice(di + VIDEO_DURATION_MARKER.length).trim();
            const n = parseInt(raw, 10);
            if (!Number.isNaN(n)) {
                duration = n;
            }
            rest = rest.slice(0, di).trimEnd();
        }
        const i = rest.indexOf(VIDEO_URL_MARKER);
        if (i < 0) {
            return { text: rest, videoUrl: '', duration };
        }
        return {
            text: rest.slice(0, i).trimEnd(),
            videoUrl: rest.slice(i + VIDEO_URL_MARKER.length).trim(),
            duration
        };
    }

    private toDtoStatus(status: ActivityStatus | undefined): 'ACTIVE' | 'INACTIVE' {
        return status === 'inactive' ? 'INACTIVE' : 'ACTIVE';
    }

    private toEducationalActivity(dto: ActiviteEducativeResponseDto): EducationalActivity {
        let created: Date;
        try {
            created = dto.createdAt ? new Date(dto.createdAt) : new Date();
            if (Number.isNaN(created.getTime())) {
                created = new Date();
            }
        } catch {
            created = new Date();
        }
        const apiType = String(dto.type ?? '').toUpperCase();
        let description = dto.description ?? '';
        let content: ActivityContent = {};
        if (apiType === 'VIDEO') {
            const split = this.splitVideoDescription(description);
            description = split.text;
            content = {
                videoUrl: split.videoUrl || undefined,
                duration: split.duration
            };
        }
        const gameType = this.normalizeApiGameType(dto.gameType);
        return {
            id: dto.id,
            name: dto.title ?? '',
            type: this.toFrontendType(dto, gameType),
            description,
            createdDate: created,
            status: this.toFrontendStatus(dto.status),
            content,
            gameType,
            iconKey: dto.iconKey ?? null,
            scoreThreshold: dto.scoreThreshold ?? null,
            latestScorePercent: dto.latestScorePercent ?? null,
            thumbnailUrl: dto.thumbnailUrl ?? null
        };
    }

    /** Enum / JSON parfois en casse ou format différent (gateway, anciennes versions). */
    private normalizeApiGameType(
        raw: unknown
    ): 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | 'PUZZLE' | null {
        if (raw == null || raw === '') {
            return null;
        }
        const u = String(raw).toUpperCase().trim().replace(/-/g, '_').replace(/\s+/g, '_');
        if (u === 'IMAGE_RECOGNITION') {
            return 'IMAGE_RECOGNITION';
        }
        if (u === 'MEMORY_QUIZ') {
            return 'MEMORY_QUIZ';
        }
        if (u === 'MEMORY_MATCH') {
            return 'MEMORY_MATCH';
        }
        if (u === 'PUZZLE') {
            return 'PUZZLE';
        }
        return null;
    }

    /**
     * Mappe les types API (dont QUIZ / VIDEO legacy) vers les types écran admin.
     */
    private toFrontendType(
        dto: ActiviteEducativeResponseDto,
        gameType: 'MEMORY_QUIZ' | 'IMAGE_RECOGNITION' | 'MEMORY_MATCH' | 'PUZZLE' | null
    ): ActivityType {
        const t = String(dto.type ?? '').toUpperCase();
        if (t === 'CONTENT') {
            return 'content';
        }
        if (t === 'VIDEO') {
            return 'video';
        }
        if (t === 'GAME' || t === 'QUIZ') {
            if (gameType === 'MEMORY_MATCH') {
                return 'image_game';
            }
            if (gameType === 'PUZZLE') {
                return 'puzzle_game';
            }
            if (gameType === 'IMAGE_RECOGNITION') {
                return 'cognitive_game';
            }
            return 'quiz';
        }
        return 'content';
    }

    private toFrontendStatus(status: ActiviteEducativeResponseDto['status']): ActivityStatus {
        return status === 'INACTIVE' ? 'inactive' : 'active';
    }
}
