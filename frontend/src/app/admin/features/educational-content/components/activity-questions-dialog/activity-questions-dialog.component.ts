import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { finalize } from 'rxjs/operators';
import { EducationalActivity } from '../../../../core/models/educational-activity.model';
import {
    ActivityService,
    QuestionAdminDto,
    QuestionAdminWriteDto
} from '../../../../core/services/activity.service';

@Component({
    selector: 'app-activity-questions-dialog',
    standalone: false,
    templateUrl: './activity-questions-dialog.component.html',
    styleUrls: ['./activity-questions-dialog.component.scss']
})
export class ActivityQuestionsDialogComponent implements OnInit {
    readonly displayedColumns = ['orderIndex', 'prompt', 'preview', 'actions'];
    dataSource = new MatTableDataSource<QuestionAdminDto>([]);
    loading = false;
    saving = false;
    loadError: string | null = null;

    form: FormGroup;
    newImageFile: File | null = null;
    newImagePreview: string | null = null;

    constructor(
        private fb: FormBuilder,
        private activityService: ActivityService,
        private snackBar: MatSnackBar,
        public dialogRef: MatDialogRef<ActivityQuestionsDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { activity: EducationalActivity }
    ) {
        this.form = this.fb.group({
            prompt: ['', [Validators.required, Validators.maxLength(2000)]],
            optionsText: ['', Validators.required],
            correctAnswer: ['', [Validators.required, Validators.maxLength(500)]]
        });
    }

    ngOnInit(): void {
        this.reload();
    }

    get requiresQuestionImage(): boolean {
        const t = this.data.activity.type;
        return t === 'cognitive_game' || t === 'image_game' || t === 'puzzle_game';
    }

    get activityTitle(): string {
        return this.data.activity.name;
    }

    reload(): void {
        this.loading = true;
        this.loadError = null;
        this.activityService
            .getQuestions(this.data.activity.id)
            .pipe(finalize(() => (this.loading = false)))
            .subscribe({
                next: rows => {
                    this.dataSource.data = Array.isArray(rows) ? rows : [];
                },
                error: err => {
                    this.loadError =
                        err?.error?.message ?? err?.message ?? 'Impossible de charger les questions.';
                }
            });
    }

    onNewImageSelected(ev: Event): void {
        const input = ev.target as HTMLInputElement;
        const file = input.files?.[0];
        this.newImageFile = file ?? null;
        this.newImagePreview = null;
        if (!file) {
            return;
        }
        const reader = new FileReader();
        reader.onload = () => {
            this.newImagePreview = typeof reader.result === 'string' ? reader.result : null;
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    clearNewImage(): void {
        this.newImageFile = null;
        this.newImagePreview = null;
    }

    private nextOrderIndex(): number {
        const rows = this.dataSource.data;
        if (!rows.length) {
            return 1;
        }
        return Math.max(...rows.map(r => r.orderIndex)) + 1;
    }

    private parseOptions(text: string): string[] {
        return text
            .split('\n')
            .map(s => s.trim())
            .filter(s => s.length > 0);
    }

    addQuestion(): void {
        if (this.form.invalid || this.saving) {
            return;
        }
        const options = this.parseOptions(this.form.value.optionsText as string);
        if (options.length < 2) {
            this.snackBar.open('Au moins deux réponses (une par ligne) sont requises.', 'Fermer', {
                duration: 4000
            });
            return;
        }
        if (this.requiresQuestionImage && !this.newImageFile) {
            this.snackBar.open('Une image est obligatoire pour ce type de jeu.', 'Fermer', { duration: 4000 });
            return;
        }
        const correct = String(this.form.value.correctAnswer).trim();
        const ok = options.some(o => o.toLowerCase() === correct.toLowerCase());
        if (!ok) {
            this.snackBar.open('La bonne réponse doit correspondre exactement à une des lignes.', 'Fermer', {
                duration: 5000
            });
            return;
        }

        const body: QuestionAdminWriteDto = {
            orderIndex: this.nextOrderIndex(),
            prompt: String(this.form.value.prompt).trim(),
            imageUrl: null,
            options,
            correctAnswer: correct
        };

        this.saving = true;
        this.activityService
            .createQuestionMultipart(this.data.activity.id, body, this.newImageFile)
            .pipe(finalize(() => (this.saving = false)))
            .subscribe({
                next: () => {
                    this.snackBar.open('Question ajoutée', 'Fermer', { duration: 2500 });
                    this.form.reset({ prompt: '', optionsText: '', correctAnswer: '' });
                    this.clearNewImage();
                    this.reload();
                },
                error: err => {
                    const msg = err?.error?.message ?? err?.message ?? 'Erreur à la création.';
                    this.snackBar.open(msg, 'Fermer', { duration: 5000 });
                }
            });
    }

    deleteQuestion(row: QuestionAdminDto): void {
        const short =
            row.prompt.length > 52 ? row.prompt.slice(0, 52) + '…' : row.prompt;
        if (!confirm(`Supprimer la question « ${short} » ?`)) {
            return;
        }
        this.activityService.deleteQuestion(this.data.activity.id, row.id).subscribe({
            next: () => {
                this.snackBar.open('Question supprimée', 'Fermer', { duration: 2500 });
                this.reload();
            },
            error: () => this.snackBar.open('Suppression impossible', 'Fermer', { duration: 4000 })
        });
    }

    close(): void {
        this.dialogRef.close();
    }
}
