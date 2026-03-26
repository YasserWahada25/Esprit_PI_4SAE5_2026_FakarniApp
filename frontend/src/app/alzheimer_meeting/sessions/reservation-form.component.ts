import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, Inject, Input, OnDestroy, OnInit, Output, PLATFORM_ID, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { Subscription } from 'rxjs';
import { PatientSession, SessionService } from './session.service';

type SessionMode = 'online' | 'presential';
type SessionType = 'PRIVATE' | 'GROUP';

function timeRangeValidator(group: AbstractControl): ValidationErrors | null {
    const startTime = group.get('startTime')?.value;
    const endTime = group.get('endTime')?.value;

    if (!startTime || !endTime) {
        return null;
    }

    return endTime > startTime ? null : { invalidTimeRange: true };
}

@Component({
    selector: 'app-reservation-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatDatepickerModule,
        MatNativeDateModule,
        MatButtonModule,
        MatRadioModule,
        MatIconModule,
        MatProgressSpinnerModule
    ],
    template: `
    <div class="reservation-form-container">
      <header class="form-header">
        <div>
          <h2><mat-icon>event_note</mat-icon> Nouvelle demande de seance</h2>
          <p>Envoyez votre demande. Le docteur l'acceptera ou la refusera.</p>
        </div>
        <button mat-icon-button type="button" (click)="onCancel()" aria-label="Fermer le formulaire">
          <mat-icon>close</mat-icon>
        </button>
      </header>

      <form [formGroup]="reservationForm" (ngSubmit)="onSubmit()" class="reservation-form">
        <section class="grid two">
          <mat-form-field appearance="outline">
            <mat-label>Titre de la seance</mat-label>
            <input matInput formControlName="title" placeholder="Ex: Suivi memoire hebdomadaire">
            <mat-error *ngIf="hasError('title', 'required')">Le titre est obligatoire.</mat-error>
            <mat-error *ngIf="hasError('title', 'minlength')">Minimum 3 caracteres.</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Type de seance</mat-label>
            <mat-select formControlName="sessionType">
              <mat-option value="PRIVATE">Privee (patient + docteur)</mat-option>
              <mat-option value="GROUP">Groupe</mat-option>
            </mat-select>
          </mat-form-field>
        </section>

        <mat-form-field appearance="outline">
          <mat-label>Description</mat-label>
          <textarea
            matInput
            rows="3"
            formControlName="description"
            placeholder="Precisez votre besoin ou contexte medical..."></textarea>
          <mat-error *ngIf="hasError('description', 'required')">La description est obligatoire.</mat-error>
          <mat-error *ngIf="hasError('description', 'minlength')">Minimum 10 caracteres.</mat-error>
        </mat-form-field>

        <section class="grid three">
          <mat-form-field appearance="outline">
            <mat-label>Date</mat-label>
            <input matInput [matDatepicker]="picker" formControlName="date">
            <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
            <mat-datepicker #picker></mat-datepicker>
            <mat-error *ngIf="hasError('date', 'required')">La date est obligatoire.</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Debut</mat-label>
            <input matInput type="time" formControlName="startTime">
            <mat-error *ngIf="hasError('startTime', 'required')">Heure de debut requise.</mat-error>
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Fin</mat-label>
            <input matInput type="time" formControlName="endTime">
            <mat-error *ngIf="hasError('endTime', 'required')">Heure de fin requise.</mat-error>
          </mat-form-field>
        </section>

        <div class="time-range-error" *ngIf="reservationForm.hasError('invalidTimeRange') && submittedOnce">
          <mat-icon>warning</mat-icon>
          <span>L'heure de fin doit etre apres l'heure de debut.</span>
        </div>

        <section class="mode-section">
          <label id="mode-label">Mode de seance</label>
          <mat-radio-group aria-labelledby="mode-label" formControlName="mode" class="radio-group">
            <mat-radio-button value="presential">Presentielle</mat-radio-button>
            <mat-radio-button value="online">En ligne</mat-radio-button>
          </mat-radio-group>
        </section>

        <div class="auto-link-info" *ngIf="isOnlineMode">
          <mat-icon>videocam</mat-icon>
          <span>Lien de reunion genere automatiquement lors de la creation de la reunion video.</span>
        </div>

        <section class="location-section" *ngIf="isPresentialMode">
          <div class="location-header">
            <strong>Localisation presentielle</strong>
            <button
              mat-stroked-button
              color="primary"
              type="button"
              (click)="useCurrentLocation()"
              [disabled]="isLocating || !isBrowser">
              <mat-icon>my_location</mat-icon>
              {{ isLocating ? 'Localisation...' : 'Utiliser ma position' }}
            </button>
          </div>

          <div #locationMap class="location-map"></div>
          <p class="location-hint">Cliquez sur la carte pour definir la localisation exacte de la seance.</p>

          <section class="grid three location-fields">
            <mat-form-field appearance="outline" class="address-field">
              <mat-label>Adresse</mat-label>
              <input matInput formControlName="locationAddress" placeholder="Ex: Centre Fakarni, salle 2">
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Latitude</mat-label>
              <input matInput type="number" formControlName="locationLatitude" step="0.000001">
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Longitude</mat-label>
              <input matInput type="number" formControlName="locationLongitude" step="0.000001">
            </mat-form-field>
          </section>

          <div class="time-range-error" *ngIf="hasLocationError()">
            <mat-icon>warning</mat-icon>
            <span>Selectionnez une latitude et une longitude valides pour une seance presentielle.</span>
          </div>
        </section>

        <div class="auto-visibility">
          <mat-icon>lock</mat-icon>
          <span>Visibilite appliquee automatiquement: <strong>{{ visibilityLabel }}</strong></span>
        </div>

        <div class="form-actions">
          <button mat-button type="button" (click)="onCancel()" [disabled]="isSubmitting">Annuler</button>
          <button
            mat-raised-button
            color="primary"
            type="submit"
            class="submit-button"
            [disabled]="reservationForm.invalid || isSubmitting">
            <ng-container *ngIf="!isSubmitting">
              <mat-icon>send</mat-icon>
              Envoyer la demande
            </ng-container>
            <ng-container *ngIf="isSubmitting">
              <mat-progress-spinner [diameter]="18" mode="indeterminate"></mat-progress-spinner>
              Envoi...
            </ng-container>
          </button>
        </div>

        <div class="submit-status error" *ngIf="submitError">
          <mat-icon>error</mat-icon>
          <span>{{ submitError }}</span>
        </div>
      </form>
    </div>
  `,
    styles: [`
    .reservation-form-container {
      padding: 24px;
      border-radius: 16px;
      background: #ffffff;
      box-shadow: 0 14px 32px rgba(16, 24, 40, 0.12);
    }

    .form-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 12px;
      margin-bottom: 18px;
    }

    .form-header h2 {
      margin: 0 0 6px;
      display: flex;
      align-items: center;
      gap: 8px;
      color: #1f2a44;
      font-size: 1.25rem;
      font-weight: 700;
    }

    .form-header p {
      margin: 0;
      color: #607089;
      font-size: 0.9rem;
    }

    .reservation-form {
      display: flex;
      flex-direction: column;
      gap: 10px;
    }

    .grid {
      display: grid;
      gap: 14px;
    }

    .grid.two {
      grid-template-columns: 1.2fr 1fr;
    }

    .grid.three {
      grid-template-columns: repeat(3, 1fr);
    }

    .mode-section {
      margin: 4px 0 2px;
    }

    .mode-section label {
      display: inline-block;
      margin-bottom: 8px;
      font-size: 0.86rem;
      font-weight: 600;
      color: #3c4e67;
    }

    .radio-group {
      display: flex;
      gap: 22px;
      padding: 10px 12px;
      background: #f7f9fc;
      border-radius: 10px;
    }

    .location-section {
      border: 1px solid #dbe6f5;
      border-radius: 12px;
      background: #f8fbff;
      padding: 12px;
    }

    .location-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 10px;
      margin-bottom: 10px;
      color: #1f2a44;
    }

    .location-map {
      width: 100%;
      height: 220px;
      border-radius: 10px;
      border: 1px solid #d6e3f7;
      overflow: hidden;
    }

    .location-hint {
      margin: 8px 0 0;
      font-size: 0.82rem;
      color: #4d5f7a;
    }

    .location-fields {
      margin-top: 12px;
    }

    .address-field {
      grid-column: span 3;
    }

    .auto-visibility {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #eef6ff;
      color: #114678;
      border: 1px solid #d4e8ff;
      border-radius: 10px;
      padding: 9px 12px;
      font-size: 0.84rem;
      margin-top: 2px;
    }

    .auto-link-info {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: #f3f6fb;
      border: 1px solid #dbe6f5;
      color: #334155;
      border-radius: 10px;
      padding: 9px 12px;
      font-size: 0.84rem;
    }

    .time-range-error {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      color: #b42318;
      background: #ffefee;
      border: 1px solid #ffd4d1;
      border-radius: 10px;
      padding: 8px 10px;
      font-size: 0.82rem;
    }

    .time-range-error mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 8px;
    }

    .submit-button {
      min-width: 160px;
      display: inline-flex;
      align-items: center;
      gap: 8px;
    }

    .submit-button mat-progress-spinner {
      --mdc-circular-progress-active-indicator-color: #fff;
    }

    .submit-status {
      margin-top: 4px;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      font-size: 0.84rem;
    }

    .submit-status.error {
      color: #b42318;
    }

    mat-form-field {
      width: 100%;
    }

    @media (max-width: 900px) {
      .grid.two,
      .grid.three {
        grid-template-columns: 1fr;
      }

      .address-field {
        grid-column: span 1;
      }

      .location-header {
        flex-direction: column;
        align-items: stretch;
      }
    }
  `]
})
export class ReservationFormComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild('locationMap') locationMapRef?: ElementRef<HTMLDivElement>;

    @Input() selectedDate: Date | null = null;
    @Output() submitted = new EventEmitter<PatientSession>();
    @Output() cancelled = new EventEmitter<void>();

    reservationForm: FormGroup;
    isSubmitting = false;
    isLocating = false;
    submittedOnce = false;
    submitError: string | null = null;
    readonly isBrowser: boolean;

    private readonly plannedStatuses = new Set(['DRAFT', 'SCHEDULED', 'ACCEPTED', 'PLANNED']);
    private readonly defaultMapCenter: [number, number] = [36.8065, 10.1815];
    private modeSubscription?: Subscription;
    private leaflet: any | null = null;
    private map: any | null = null;
    private locationMarker: any | null = null;

    constructor(
        private fb: FormBuilder,
        private sessionService: SessionService,
        @Inject(PLATFORM_ID) platformId: Object
    ) {
        this.isBrowser = isPlatformBrowser(platformId);
        this.reservationForm = this.fb.group(
            {
                title: ['', [Validators.required, Validators.minLength(3)]],
                description: ['', [Validators.required, Validators.minLength(10)]],
                date: [null, Validators.required],
                startTime: ['', Validators.required],
                endTime: ['', Validators.required],
                mode: ['presential', Validators.required],
                sessionType: ['PRIVATE', Validators.required],
                locationAddress: [''],
                locationLatitude: [null],
                locationLongitude: [null]
            },
            { validators: timeRangeValidator }
        );
    }

    ngOnInit(): void {
        if (this.selectedDate) {
            this.reservationForm.patchValue({ date: this.selectedDate });
        }

        this.updateLocationControls(this.reservationForm.get('mode')?.value as SessionMode);
        this.modeSubscription = this.reservationForm.get('mode')?.valueChanges.subscribe(mode => {
            this.updateLocationControls(mode as SessionMode);
        });
    }

    ngAfterViewInit(): void {
        if (this.isPresentialMode) {
            this.scheduleMapInitialization();
        }
    }

    ngOnDestroy(): void {
        this.modeSubscription?.unsubscribe();
        this.destroyMap();
    }

    get isOnlineMode(): boolean {
        return this.reservationForm.get('mode')?.value === 'online';
    }

    get isPresentialMode(): boolean {
        return this.reservationForm.get('mode')?.value === 'presential';
    }

    get visibilityLabel(): string {
        return this.reservationForm.get('sessionType')?.value === 'GROUP'
            ? 'Publique (seance de groupe)'
            : 'Privee (seance individuelle)';
    }

    onSubmit(): void {
        this.submittedOnce = true;
        this.reservationForm.markAllAsTouched();

        if (this.reservationForm.invalid) {
            return;
        }

        this.isSubmitting = true;
        this.submitError = null;

        const formValue = this.reservationForm.value;
        const sessionDate: Date = formValue.date;
        const startTime = this.combineDateAndTime(sessionDate, formValue.startTime);
        const endTime = this.combineDateAndTime(sessionDate, formValue.endTime);
        const sessionType = formValue.sessionType as SessionType;
        const mode = formValue.mode as SessionMode;
        const isPresential = mode === 'presential';

        const session: PatientSession = {
            title: formValue.title.trim(),
            description: formValue.description.trim(),
            startTime: startTime.toISOString(),
            endTime: endTime.toISOString(),
            visibility: sessionType === 'GROUP' ? 'PUBLIC' : 'PRIVATE',
            sessionType,
            status: 'DRAFT',
            type: mode,
            locationAddress: isPresential ? this.normalizeText(formValue.locationAddress) : undefined,
            locationLatitude: isPresential ? this.toCoordinate(formValue.locationLatitude) : undefined,
            locationLongitude: isPresential ? this.toCoordinate(formValue.locationLongitude) : undefined
        };

        this.sessionService.getSessions().subscribe({
            next: existingSessions => {
                const hasConflict = existingSessions.some(existing => {
                    if (!this.isPlannedStatus(existing.status)) {
                        return false;
                    }

                    const existingStart = new Date(existing.startTime);
                    const existingEnd = new Date(existing.endTime);
                    return this.isTimeRangeOverlapping(startTime, endTime, existingStart, existingEnd);
                });

                if (hasConflict) {
                    this.isSubmitting = false;
                    this.submitError = 'Une session existe deja dans cette plage horaire.';
                    if (typeof window !== 'undefined') {
                        window.alert(this.submitError);
                    }
                    return;
                }

                this.sessionService.createSession(session).subscribe({
                    next: createdSession => {
                        this.isSubmitting = false;
                        this.submitted.emit(createdSession);
                        this.reservationForm.reset({
                            title: '',
                            description: '',
                            date: this.selectedDate ?? null,
                            startTime: '',
                            endTime: '',
                            mode: 'presential',
                            sessionType: 'PRIVATE',
                            locationAddress: '',
                            locationLatitude: null,
                            locationLongitude: null
                        });
                        this.submittedOnce = false;
                        this.updateLocationControls('presential');
                    },
                    error: () => {
                        this.isSubmitting = false;
                        this.submitError = 'Impossible d envoyer la demande pour le moment. Veuillez reessayer.';
                    }
                });
            },
            error: () => {
                this.isSubmitting = false;
                this.submitError = 'Impossible de verifier les sessions existantes. Veuillez reessayer.';
            }
        });
    }

    onCancel(): void {
        this.cancelled.emit();
    }

    useCurrentLocation(): void {
        if (!this.isBrowser || !navigator.geolocation) {
            this.submitError = 'La geolocalisation est indisponible sur ce navigateur.';
            return;
        }

        this.isLocating = true;
        navigator.geolocation.getCurrentPosition(
            position => {
                this.isLocating = false;
                this.setLocation(position.coords.latitude, position.coords.longitude, true);
            },
            () => {
                this.isLocating = false;
                this.submitError = 'Impossible d obtenir votre position actuelle.';
            },
            { enableHighAccuracy: true, timeout: 10000 }
        );
    }

    hasError(controlName: string, errorCode: string): boolean {
        const control = this.reservationForm.get(controlName);
        return !!control && control.hasError(errorCode) && (control.touched || control.dirty || this.submittedOnce);
    }

    hasLocationError(): boolean {
        const hasInvalidLocation = !!this.reservationForm.get('locationLatitude')?.invalid
            || !!this.reservationForm.get('locationLongitude')?.invalid;
        const hasTouchedLocation = !!this.reservationForm.get('locationLatitude')?.touched
            || !!this.reservationForm.get('locationLongitude')?.touched
            || this.submittedOnce;
        return this.isPresentialMode && hasInvalidLocation && hasTouchedLocation;
    }

    private combineDateAndTime(date: Date, time: string): Date {
        const [hours, minutes] = time.split(':').map(Number);
        const value = new Date(date);
        value.setHours(hours, minutes, 0, 0);
        return value;
    }

    private isPlannedStatus(status: PatientSession['status']): boolean {
        return this.plannedStatuses.has((status || '').toUpperCase());
    }

    private isTimeRangeOverlapping(
        startA: Date,
        endA: Date,
        startB: Date,
        endB: Date
    ): boolean {
        return startA.getTime() < endB.getTime() && endA.getTime() > startB.getTime();
    }

    private updateLocationControls(mode: SessionMode): void {
        const latitudeControl = this.reservationForm.get('locationLatitude');
        const longitudeControl = this.reservationForm.get('locationLongitude');

        if (!latitudeControl || !longitudeControl) {
            return;
        }

        if (mode === 'presential') {
            latitudeControl.setValidators([Validators.required, Validators.min(-90), Validators.max(90)]);
            longitudeControl.setValidators([Validators.required, Validators.min(-180), Validators.max(180)]);
            latitudeControl.updateValueAndValidity({ emitEvent: false });
            longitudeControl.updateValueAndValidity({ emitEvent: false });
            this.scheduleMapInitialization();
            return;
        }

        latitudeControl.clearValidators();
        longitudeControl.clearValidators();
        this.reservationForm.patchValue(
            {
                locationAddress: '',
                locationLatitude: null,
                locationLongitude: null
            },
            { emitEvent: false }
        );
        latitudeControl.updateValueAndValidity({ emitEvent: false });
        longitudeControl.updateValueAndValidity({ emitEvent: false });
        this.destroyMap();
    }

    private scheduleMapInitialization(): void {
        if (!this.isBrowser) {
            return;
        }

        setTimeout(() => {
            void this.initializeMap();
        });
    }

    private async initializeMap(): Promise<void> {
        if (!this.isBrowser || !this.isPresentialMode) {
            return;
        }

        const mapElement = this.locationMapRef?.nativeElement;
        if (!mapElement) {
            return;
        }

        if (!this.leaflet) {
            this.leaflet = await import('leaflet');
        }

        if (!this.map) {
            this.map = this.leaflet.map(mapElement, { zoomControl: true }).setView(this.defaultMapCenter, 13);
            this.leaflet.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                attribution: '&copy; OpenStreetMap contributors'
            }).addTo(this.map);
            this.map.on('click', this.onMapClick);
        } else {
            this.map.invalidateSize();
        }

        const latitude = this.toCoordinate(this.reservationForm.get('locationLatitude')?.value);
        const longitude = this.toCoordinate(this.reservationForm.get('locationLongitude')?.value);
        if (latitude !== undefined && longitude !== undefined) {
            this.setLocation(latitude, longitude, true);
        } else {
            this.map.setView(this.defaultMapCenter, 13);
        }
    }

    private readonly onMapClick = (event: any): void => {
        this.setLocation(event.latlng.lat, event.latlng.lng, false);
    };

    private setLocation(latitude: number, longitude: number, centerMap: boolean): void {
        const roundedLatitude = this.roundCoordinate(latitude);
        const roundedLongitude = this.roundCoordinate(longitude);

        this.reservationForm.patchValue({
            locationLatitude: roundedLatitude,
            locationLongitude: roundedLongitude
        });
        this.reservationForm.get('locationLatitude')?.markAsTouched();
        this.reservationForm.get('locationLongitude')?.markAsTouched();

        if (!this.map || !this.leaflet) {
            return;
        }

        if (!this.locationMarker) {
            this.locationMarker = this.leaflet.circleMarker([roundedLatitude, roundedLongitude], {
                radius: 8,
                color: '#1f6feb',
                fillColor: '#1f6feb',
                fillOpacity: 0.85,
                weight: 2
            }).addTo(this.map);
        } else {
            this.locationMarker.setLatLng([roundedLatitude, roundedLongitude]);
        }

        if (centerMap) {
            this.map.setView([roundedLatitude, roundedLongitude], 15);
        }
    }

    private destroyMap(): void {
        if (!this.map) {
            return;
        }
        this.map.off('click', this.onMapClick);
        this.map.remove();
        this.map = null;
        this.locationMarker = null;
    }

    private toCoordinate(value: unknown): number | undefined {
        if (value === null || value === undefined || value === '') {
            return undefined;
        }
        const numericValue = typeof value === 'number' ? value : Number(value);
        return Number.isFinite(numericValue) ? numericValue : undefined;
    }

    private roundCoordinate(value: number): number {
        return Math.round(value * 1000000) / 1000000;
    }

    private normalizeText(value: unknown): string | undefined {
        if (typeof value !== 'string') {
            return undefined;
        }
        const trimmedValue = value.trim();
        return trimmedValue.length > 0 ? trimmedValue : undefined;
    }
}
