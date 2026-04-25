import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
    GeneratedMeetingReport,
    MeetingInsightsService,
    MeetingTranscription
} from './meeting-insights.service';


@Component({
    selector: 'app-reports-dashboard',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './reports-dashboard.component.html',
    styleUrl: './reports-dashboard.component.css'
})
export class ReportsDashboardComponent {
    reports: GeneratedMeetingReport[] = [];
    transcriptions: MeetingTranscription[] = [];

    selectedFile: File | null = null;
    meetingTitle = '';
    language = 'fr';
    transcriptionText = '';
    selectedTranscriptionId: string | null = null;
    selectedReport: GeneratedMeetingReport | null = null;

    loadingTranscription = false;
    loadingReport = false;
    loadingHistory = false;
    errorMessage = '';

    constructor(private meetingInsightsService: MeetingInsightsService) {}

    ngOnInit(): void {
        this.refreshHistory();
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        this.selectedFile = input.files?.item(0) ?? null;
        this.errorMessage = '';
    }

    runTranscription(): void {
        if (!this.selectedFile) {
            this.errorMessage = 'Veuillez choisir un fichier audio.';
            return;
        }

        this.loadingTranscription = true;
        this.errorMessage = '';
        this.meetingInsightsService.transcribeRecording(this.selectedFile, this.language).subscribe({
            next: (res) => {
                this.transcriptionText = res.transcription;
                this.selectedTranscriptionId = res.id;
                this.transcriptions = [res, ...this.transcriptions];
                this.loadingTranscription = false;
            },
            error: (err) => {
                this.loadingTranscription = false;
                this.errorMessage = err?.error?.detail || 'Echec de la transcription.';
            }
        });
    }

    generateSummaryReport(): void {
        if (!this.transcriptionText.trim()) {
            this.errorMessage = 'La transcription est vide.';
            return;
        }

        this.loadingReport = true;
        this.errorMessage = '';
        this.meetingInsightsService.generateReport({
            meeting_title: this.meetingTitle.trim() || null,
            language: this.language.trim() || null,
            transcribed_text: this.transcriptionText.trim(),
            transcription_id: this.selectedTranscriptionId
        }).subscribe({
            next: (report) => {
                this.selectedReport = report;
                this.reports = [report, ...this.reports];
                this.loadingReport = false;
            },
            error: (err) => {
                this.loadingReport = false;
                this.errorMessage = err?.error?.detail || 'Echec de la generation du rapport.';
            }
        });
    }

    refreshHistory(): void {
        this.loadingHistory = true;
        this.errorMessage = '';
        this.meetingInsightsService.getTranscriptions().subscribe({
            next: (items) => {
                this.transcriptions = items ?? [];
                this.loadingHistory = false;
            },
            error: () => {
                this.loadingHistory = false;
            }
        });

        this.meetingInsightsService.getReports().subscribe({
            next: (items) => {
                this.reports = items ?? [];
            },
            error: () => {
                // Keep quiet: service may be freshly deployed with empty state.
            }
        });
    }

    viewTranscription(transcription: MeetingTranscription): void {
        this.transcriptionText = transcription.transcription;
        this.selectedTranscriptionId = transcription.id;
    }

    previewReport(report: GeneratedMeetingReport): void {
        this.selectedReport = report;
    }
}
