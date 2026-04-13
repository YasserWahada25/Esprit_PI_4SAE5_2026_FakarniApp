import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
    ActivityContent,
    ActivityType,
    EducationalActivity
} from '../../../../core/models/educational-activity.model';
import { ActivityService } from '../../../../core/services/activity.service';

@Component({
    selector: 'app-activity-form',
    standalone: false,
    templateUrl: './activity-form.component.html',
    styleUrls: ['./activity-form.component.scss']
})
export class ActivityFormComponent implements OnInit, OnDestroy {
    activityForm: FormGroup;
    isEditMode: boolean = false;
    private typeSub?: Subscription;

    private static readonly selectableTypes: ActivityType[] = ['quiz', 'image_game'];

    /** Fichier miniature choisi pour l’upload multipart. */
    thumbnailFile: File | null = null;
    /** Aperçu (data URL ou URL API existante). */
    thumbnailPreview: string | null = null;

    activityTypes: { value: ActivityType; label: string }[] = [
        { value: 'quiz', label: 'Quiz (QCM texte)' },
        { value: 'image_game', label: 'Memory — paires d’images' }
    ];

    constructor(
        private fb: FormBuilder,
        private activityService: ActivityService,
        public dialogRef: MatDialogRef<ActivityFormComponent>,
        @Inject(MAT_DIALOG_DATA) public data: EducationalActivity
    ) {
        this.isEditMode = !!data;
        this.activityForm = this.fb.group({
            name: ['', Validators.required],
            type: ['quiz', Validators.required],
            description: ['', Validators.required],
            status: ['active', Validators.required],
            videoUrl: [''],
            duration: [null as number | null],
            scoreThreshold: [null as number | null]
        });
    }

    ngOnInit(): void {
        if (this.isEditMode) {
            this.activityForm.patchValue({
                name: this.data.name,
                type: this.coerceSelectableType(this.data.type),
                description: this.data.description,
                status: this.data.status,
                videoUrl: this.data.content?.videoUrl || '',
                duration: this.data.content?.duration ?? null,
                scoreThreshold: this.data.scoreThreshold ?? null
            });
            const u = (this.data.thumbnailUrl || '').trim();
            this.thumbnailPreview = u.length > 0 ? u : null;
        } else {
            this.activityForm.patchValue({
                scoreThreshold: 60
            });
        }
        this.typeSub = this.activityForm.get('type')?.valueChanges.subscribe(t => {
            this.applyValidatorsForType(t as ActivityType);
        });
        this.applyValidatorsForType(this.activityForm.get('type')?.value as ActivityType);
    }

    ngOnDestroy(): void {
        this.typeSub?.unsubscribe();
    }

    private coerceSelectableType(t: ActivityType): ActivityType {
        return ActivityFormComponent.selectableTypes.includes(t) ? t : 'quiz';
    }

    private applyValidatorsForType(type: ActivityType): void {
        const videoUrl = this.activityForm.get('videoUrl');
        const score = this.activityForm.get('scoreThreshold');
        videoUrl?.clearValidators();
        score?.clearValidators();

        if (type === 'quiz' || type === 'image_game') {
            score?.setValidators([Validators.required, Validators.min(0), Validators.max(100)]);
        }

        if (type === 'quiz' && !this.isEditMode && (score?.value == null || score.value === '')) {
            score?.patchValue(60, { emitEvent: false });
        }
        if (type === 'image_game' && !this.isEditMode && (score?.value == null || score.value === '')) {
            score?.patchValue(100, { emitEvent: false });
        }

        videoUrl?.updateValueAndValidity({ emitEvent: false });
        score?.updateValueAndValidity({ emitEvent: false });
    }

    onThumbnailSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0] ?? null;
        this.thumbnailFile = file;
        if (!file) {
            return;
        }
        const reader = new FileReader();
        reader.onload = () => {
            this.thumbnailPreview = typeof reader.result === 'string' ? reader.result : null;
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    clearThumbnail(): void {
        this.thumbnailFile = null;
        const u = this.isEditMode ? (this.data.thumbnailUrl || '').trim() : '';
        this.thumbnailPreview = u.length > 0 ? u : null;
    }

    onSubmit(): void {
        if (this.activityForm.valid) {
            const formValue = this.activityForm.value;
            const base: Omit<EducationalActivity, 'id'> = {
                name: formValue.name,
                type: formValue.type,
                description: formValue.description,
                createdDate: this.isEditMode ? this.data.createdDate : new Date(),
                status: formValue.status,
                scoreThreshold:
                    formValue.type === 'quiz' || formValue.type === 'image_game'
                        ? Number(formValue.scoreThreshold)
                        : null,
                content: this.buildContent(formValue),
                thumbnailUrl:
                    this.thumbnailFile == null && this.isEditMode
                        ? this.data.thumbnailUrl ?? undefined
                        : undefined
            };

            if (this.isEditMode) {
                this.activityService
                    .updateActivity(this.data.id, base, this.thumbnailFile)
                    .subscribe(() => {
                        this.dialogRef.close(true);
                    });
            } else {
                this.activityService.createActivity(base, this.thumbnailFile).subscribe(() => {
                    this.dialogRef.close(true);
                });
            }
        }
    }

    private buildContent(formValue: {
        type: ActivityType;
        videoUrl?: string;
        duration?: number | null;
    }): ActivityContent {
        if (formValue.type === 'video') {
            return {
                videoUrl: formValue.videoUrl || '',
                duration: formValue.duration ?? undefined
            };
        }
        if (formValue.type === 'content') {
            return {};
        }
        return {};
    }

    close(): void {
        this.dialogRef.close();
    }
}
