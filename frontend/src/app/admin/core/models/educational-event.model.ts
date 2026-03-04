export interface EducationalEvent {
    // ── Champs backend (EventResponse) ──────────────────
    id: number;
    title: string;
    startDateTime: string;   // ISO 8601 ex: "2026-03-01T10:00:00"
    location?: string;
    remindEnabled: boolean;
    userId: number;
    createdAt?: string;

    // ── Champs présents dans le DTO create/update ────────
    description?: string;

    // ── Champs UI uniquement (non stockés backend) ───────
    date?: Date;
    startTime?: string;
    endTime?: string;
    status?: EventStatus;
    participantsCount?: number;
    maxParticipants?: number;
    reminders?: ReminderConfig[];
}

export type EventStatus = 'scheduled' | 'ongoing' | 'completed' | 'cancelled';

export interface ReminderConfig {
    type: 'email' | 'sms' | 'push';
    timeBefore: number;
    enabled: boolean;
}
