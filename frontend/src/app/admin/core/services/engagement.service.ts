import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, forkJoin, map, Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { PatientEngagement, EngagementStatistics, ParticipationStatus } from '../models/patient-engagement.model';

@Injectable({
    providedIn: 'root'
})
export class EngagementService {
    private apiUrl = `${environment.apiBaseUrl}/api/engagement`;

    private readonly engagementsSubject = new BehaviorSubject<PatientEngagement[]>([]);
    readonly engagements$ = this.engagementsSubject.asObservable();

    private readonly statisticsSubject = new BehaviorSubject<EngagementStatistics | null>(null);
    readonly statistics$ = this.statisticsSubject.asObservable();

    constructor(private http: HttpClient) { }

    reloadEngagements(): Observable<PatientEngagement[]> {
        return this.http.get<any[]>(`${this.apiUrl}/patients`).pipe(
            map(rows => rows.map(r => this.toPatientEngagement(r))),
            tap(rows => this.engagementsSubject.next(rows))
        );
    }

    reloadStatistics(): Observable<EngagementStatistics> {
        return forkJoin({
            summary: this.http.get<any>(`${this.apiUrl}/summary`),
            byType: this.http.get<Record<string, number>>(`${this.apiUrl}/by-type`),
            distribution: this.http.get<any>(`${this.apiUrl}/distribution`)
        }).pipe(
            map(({ summary, byType, distribution }) => {
                const engagementByType: { [key: string]: number } = {};
                Object.entries(byType ?? {}).forEach(([k, v]) => {
                    engagementByType[this.toFrontendTypeKey(k)] = v as number;
                });

                const stats: EngagementStatistics = {
                    totalActivities: summary?.totalActivities ?? 0,
                    activePatients: summary?.activePatients ?? 0,
                    averageEngagement: Math.round(summary?.averageEngagement ?? 0),
                    engagementByType,
                    engagementOverTime: [],
                    participationDistribution: this.toParticipationDistribution(distribution)
                };
                return stats;
            }),
            tap(stats => this.statisticsSubject.next(stats))
        );
    }

    getEngagements(): Observable<PatientEngagement[]> {
        return this.engagements$;
    }

    getStatistics(): Observable<EngagementStatistics | null> {
        return this.statistics$;
    }

    getEngagementsByPatient(patientId: string): Observable<PatientEngagement[]> {
        return this.http.get<any[]>(`${this.apiUrl}/patients/${encodeURIComponent(patientId)}`).pipe(
            map(rows => rows.map(r => this.toPatientEngagement(r)))
        );
    }

    private toPatientEngagement(row: any): PatientEngagement {
        const participationStatus = this.toParticipationStatus(row?.status);
        const completedDate = row?.endDate ? new Date(row.endDate) : undefined;

        return {
            id: row?.id,
            patientId: row?.patientId != null ? String(row.patientId) : '',
            patientName: row?.patientName,
            activityId: row?.activityId,
            activityName: row?.activityTitle,
            participationStatus,
            score: row?.score ?? undefined,
            progressPercentage: row?.progression ?? undefined,
            completedDate,
            timeSpent: undefined
        };
    }

    private toParticipationStatus(status: string): ParticipationStatus {
        switch (status) {
            case 'COMPLETED':
                return 'completed';
            case 'IN_PROGRESS':
                return 'in_progress';
            case 'NOT_STARTED':
                return 'not_started';
            case 'ABANDONED':
                return 'abandoned';
            default:
                return 'not_started';
        }
    }

    private toFrontendTypeKey(type: string): string {
        switch (type) {
            case 'QUIZ':
                return 'quiz';
            case 'GAME':
                return 'cognitive_game';
            case 'VIDEO':
                return 'video';
            case 'EVENT':
                return 'event';
            default:
                return String(type).toLowerCase();
        }
    }

    private toParticipationDistribution(distribution: any): { status: ParticipationStatus; count: number }[] {
        return [
            { status: 'completed', count: distribution?.completed ?? 0 },
            { status: 'in_progress', count: distribution?.inProgress ?? 0 },
            { status: 'not_started', count: distribution?.notStarted ?? 0 },
            { status: 'abandoned', count: distribution?.abandoned ?? 0 }
        ];
    }
}
