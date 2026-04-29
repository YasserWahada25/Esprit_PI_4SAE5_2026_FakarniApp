export interface Session {
    id: number;
    title: string;
    date: Date;
    startTime: string;
    endTime: string;
    status: 'DRAFT' | 'SCHEDULED' | 'CANCELLED' | 'DONE';
    participantsCount: number;
    description?: string;
    visibility?: 'PRIVATE' | 'PUBLIC';
    sessionType?: 'PRIVATE' | 'GROUP';
    meetingMode?: 'ONLINE' | 'IN_PERSON';
    meetingUrl?: string;
    locationAddress?: string;
    locationLatitude?: number;
    locationLongitude?: number;
    createdBy?: string;
    createdAt?: string;
}
