import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface MeetingTranscription {
  id: string;
  requested_by: string;
  filename: string;
  language?: string | null;
  transcription: string;
  created_at: string;
}

export interface ReportRequestPayload {
  meeting_title?: string | null;
  language?: string | null;
  transcribed_text: string;
  transcription_id?: string | null;
}

export interface GeneratedMeetingReport {
  id?: string | null;
  transcription_id?: string | null;
  meeting_title?: string | null;
  language?: string | null;
  generated_by?: string | null;
  summary: string;
  key_points: string[];
  action_items: string[];
  sentiment_analysis: {
    label: string;
    score: number;
  };
  entities: Record<string, string[]>;
  created_at?: string | null;
}

@Injectable({ providedIn: 'root' })
export class MeetingInsightsService {
  private readonly base = (environment.apiBaseUrl ?? '').trim().replace(/\/$/, '');
  private readonly api = this.base ? `${this.base}/api/meet` : '/api/meet';

  constructor(private http: HttpClient) {}

  transcribeRecording(file: File, language?: string): Observable<MeetingTranscription> {
    const fd = new FormData();
    fd.append('file', file, file.name);
    if (language?.trim()) {
      fd.append('language', language.trim());
    }
    return this.http.post<MeetingTranscription>(`${this.api}/recording`, fd);
  }

  generateReport(payload: ReportRequestPayload): Observable<GeneratedMeetingReport> {
    return this.http.post<GeneratedMeetingReport>(`${this.api}/report`, payload);
  }

  getReports(): Observable<GeneratedMeetingReport[]> {
    return this.http.get<GeneratedMeetingReport[]>(`${this.api}/reports`);
  }

  getTranscriptions(): Observable<MeetingTranscription[]> {
    return this.http.get<MeetingTranscription[]>(`${this.api}/transcriptions`);
  }
}

