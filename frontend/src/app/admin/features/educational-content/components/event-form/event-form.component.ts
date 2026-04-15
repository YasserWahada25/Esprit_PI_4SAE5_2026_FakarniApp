import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import { EducationalEventService } from '../../../../core/services/educational-event.service';
import { EducationalEvent } from '../../../../core/models/educational-event.model';
import { LocationPickerComponent, LocationSelection } from '../location-picker/location-picker.component';

@Component({
  selector: 'app-event-form',
  standalone: false,
  templateUrl: './event-form.component.html',
  styleUrls: ['./event-form.component.scss']
})
export class EventFormComponent implements OnInit {
  eventForm: FormGroup;
  isEditMode = false;

  constructor(
    private fb: FormBuilder,
    private eventService: EducationalEventService,
    private dialogRef: MatDialogRef<EventFormComponent>,
    private dialog: MatDialog,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public data: EducationalEvent | null
  ) {
    this.isEditMode = !!data;
    this.eventForm = this.fb.group({
      title: [data?.title ?? '', Validators.required],
      description: [data?.description ?? ''],
      startDateTime: [data?.startDateTime ?? '', Validators.required],
      location: [data?.location ?? ''],
      remindEnabled: [data?.remindEnabled ?? false],
      userId: [data?.userId ?? 1, Validators.required],
      lat: [data?.lat ?? null],
      lng: [data?.lng ?? null]
    });
  }

  ngOnInit(): void { }

  /** Called by LocationPickerComponent when the user clicks on the map */
  onLocationSelected(selection: LocationSelection): void {
    this.eventForm.patchValue({
      location: selection.address,
      lat: selection.lat,
      lng: selection.lng
    });
  }

  onSubmit(): void {
    if (this.eventForm.invalid) return;

    const payload = this.eventForm.value;

    // Fermer immédiatement toutes les popups pour l'utilisateur
    this.dialog.closeAll();

    if (this.isEditMode && this.data) {
      this.eventService.updateEvent(this.data.id, payload).subscribe({
        next: () => {
          // Après succès, on recharge la liste partagée puis on ferme le formulaire
          this.eventService.reloadEvents().subscribe(() => {
            this.router.navigate(['/admin/educational-content/events']);
          });
        },
        error: (err) => console.error('Erreur mise à jour', err)
      });
    } else {
      this.eventService.createEvent(payload).subscribe({
        next: () => {
          // Après succès, on recharge la liste partagée puis on ferme le formulaire
          this.eventService.reloadEvents().subscribe(() => {
            this.router.navigate(['/admin/educational-content/events']);
          });
        },
        error: (err) => console.error('Erreur création', err)
      });
    }
  }

  close(): void {
    this.dialogRef.close(false);
  }
}

