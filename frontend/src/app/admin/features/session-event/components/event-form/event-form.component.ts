import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { EventService } from '../../../../core/services/event.service';
import { EducationalEvent } from '../../../../core/models/educational-event.model';

@Component({
    selector: 'app-event-form',
    standalone: false,
    templateUrl: './event-form.component.html',
    styleUrls: ['./event-form.component.scss']
})
export class EventFormComponent {
    eventForm: FormGroup;
    isEditMode: boolean = false;
    reminderOptions: string[] = ['1 hour before', '1 day before', '1 week before'];

    constructor(
        private fb: FormBuilder,
        private eventService: EventService,
        public dialogRef: MatDialogRef<EventFormComponent>,
        @Inject(MAT_DIALOG_DATA) public data: EducationalEvent | null
    ) {
        this.isEditMode = !!data;
        this.eventForm = this.fb.group({
            title: [data?.title || '', Validators.required],
            date: [data?.date || new Date(), Validators.required],
            startTime: [data?.startTime || '', Validators.required],
            status: [data?.status || 'scheduled', Validators.required],
            participantsCount: [data?.participantsCount || 0, [Validators.required, Validators.min(0)]],
            description: [data?.description || ''],
            reminders: [data?.reminders || []]
        });
    }

    onSubmit(): void {
        if (this.eventForm.valid) {
            const formValue = this.eventForm.value;

            // Convert date + startTime en startDateTime ISO pour le backend
            let startDateTime: string | undefined;
            const date: Date = formValue.date;
            const time: string = formValue.startTime;

            if (date && time) {
                const [hoursStr, minutesStr] = time.split(':');
                const hours = Number(hoursStr);
                const minutes = Number(minutesStr || 0);
                const d = new Date(date);
                if (!isNaN(hours)) {
                    d.setHours(hours, minutes, 0, 0);
                }
                startDateTime = d.toISOString();
            }

            if (this.isEditMode && this.data) {
                const updatedEvent: EducationalEvent = {
                    ...this.data,
                    ...formValue,
                    startDateTime: startDateTime ?? this.data.startDateTime,
                    userId: this.data.userId ?? 1,
                    remindEnabled: this.data.remindEnabled ?? false
                };

                this.eventService.updateEvent(this.data.id, updatedEvent).subscribe(() => {
                    this.dialogRef.close(true);
                });
            } else {
                const newEvent: Omit<EducationalEvent, 'id'> = {
                    title: formValue.title,
                    description: formValue.description,
                    startDateTime: startDateTime ?? new Date().toISOString(),
                    location: undefined,
                    remindEnabled: false,
                    userId: 1,
                    date: formValue.date,
                    startTime: formValue.startTime,
                    status: formValue.status,
                    participantsCount: formValue.participantsCount,
                    reminders: formValue.reminders
                };

                this.eventService.addEvent(newEvent as EducationalEvent).subscribe(() => {
                    this.dialogRef.close(true);
                });
            }
        }
    }

    onCancel(): void {
        this.dialogRef.close();
    }
}
