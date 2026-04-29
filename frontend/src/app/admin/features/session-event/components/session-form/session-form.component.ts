import { isPlatformBrowser } from '@angular/common';
import { AfterViewInit, Component, ElementRef, Inject, OnDestroy, PLATFORM_ID, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { Session } from '../../../../core/models/session.model';
import { SessionService } from '../../../../core/services/session.service';

@Component({
    selector: 'app-session-form',
    standalone: false,
    templateUrl: './session-form.component.html',
    styleUrls: ['./session-form.component.scss']
})
export class SessionFormComponent implements AfterViewInit, OnDestroy {
    @ViewChild('locationMap') locationMapRef?: ElementRef<HTMLDivElement>;

    sessionForm: FormGroup;
    isEditMode: boolean = false;
    isSubmitting = false;
    isLocating = false;
    formError: string | null = null;
    private readonly plannedStatuses = new Set(['DRAFT', 'SCHEDULED', 'ACCEPTED', 'PLANNED']);
    private readonly defaultMapCenter: [number, number] = [36.8065, 10.1815];

    private leaflet: any | null = null;
    private map: any | null = null;
    private locationMarker: any | null = null;
    private modeSubscription?: Subscription;
    private mapInitTimeoutId: ReturnType<typeof setTimeout> | null = null;

    readonly isBrowser: boolean;

    statusOptions = [
        { value: 'SCHEDULED', label: 'Prevu' },
        { value: 'DRAFT', label: 'Brouillon' },
        { value: 'DONE', label: 'Termine' },
        { value: 'CANCELLED', label: 'Annule' }
    ];

    visibilityOptions = [
        { value: 'PUBLIC', label: 'Public' },
        { value: 'PRIVATE', label: 'Privee' }
    ];

    sessionTypeOptions = [
        { value: 'GROUP', label: 'Groupe' },
        { value: 'PRIVATE', label: 'Privee' }
    ];

    meetingModeOptions = [
        { value: 'ONLINE', label: 'En ligne' },
        { value: 'IN_PERSON', label: 'En presentiel' }
    ];

    constructor(
        private fb: FormBuilder,
        private sessionService: SessionService,
        public dialogRef: MatDialogRef<SessionFormComponent>,
        @Inject(MAT_DIALOG_DATA) public data: Session | null,
        @Inject(PLATFORM_ID) platformId: Object
    ) {
        this.isBrowser = isPlatformBrowser(platformId);
        this.isEditMode = !!data;
        this.sessionForm = this.fb.group({
            title: [data?.title || '', Validators.required],
            date: [data?.date || new Date(), Validators.required],
            startTime: [data?.startTime || '', Validators.required],
            endTime: [data?.endTime || '', Validators.required],
            status: [data?.status || 'SCHEDULED', Validators.required],
            visibility: [data?.visibility || 'PUBLIC', Validators.required],
            sessionType: [data?.sessionType || 'GROUP', Validators.required],
            meetingMode: [data?.meetingMode || 'ONLINE', Validators.required],
            participantsCount: [data?.participantsCount || 0, [Validators.required, Validators.min(0)]],
            locationAddress: [data?.locationAddress || ''],
            locationLatitude: [data?.locationLatitude ?? null],
            locationLongitude: [data?.locationLongitude ?? null],
            description: [data?.description || '']
        });

        this.updateLocationControls(this.sessionForm.get('meetingMode')?.value as Session['meetingMode']);
        this.modeSubscription = this.sessionForm.get('meetingMode')?.valueChanges.subscribe(mode => {
            this.updateLocationControls(mode as Session['meetingMode']);
        });
    }

    ngAfterViewInit(): void {
        if (this.isInPersonMode()) {
            this.scheduleMapInitialization();
        }
    }

    ngOnDestroy(): void {
        this.modeSubscription?.unsubscribe();
        if (this.mapInitTimeoutId) {
            clearTimeout(this.mapInitTimeoutId);
            this.mapInitTimeoutId = null;
        }
        this.destroyMap();
    }

    onSubmit(): void {
        this.sessionForm.markAllAsTouched();
        if (this.sessionForm.invalid) {
            return;
        }

        this.isSubmitting = true;
        this.formError = null;

        const formValue = this.sessionForm.value;
        const selectedDate = new Date(formValue.date);
        const requestedStart = this.combineDateAndTime(selectedDate, formValue.startTime);
        const requestedEnd = this.combineDateAndTime(selectedDate, formValue.endTime);

        if (requestedEnd.getTime() <= requestedStart.getTime()) {
            this.isSubmitting = false;
            this.formError = 'L heure de fin doit etre apres l heure de debut.';
            return;
        }

        const isInPerson = formValue.meetingMode === 'IN_PERSON';
        const locationLatitude = this.toCoordinate(formValue.locationLatitude);
        const locationLongitude = this.toCoordinate(formValue.locationLongitude);

        const session: Session = {
            id: this.data?.id || 0,
            title: formValue.title,
            date: formValue.date,
            startTime: formValue.startTime,
            endTime: formValue.endTime,
            status: formValue.status,
            visibility: formValue.visibility,
            sessionType: formValue.sessionType,
            meetingMode: formValue.meetingMode,
            participantsCount: Number(formValue.participantsCount) || 0,
            description: this.normalizeText(formValue.description),
            createdBy: this.data?.createdBy || 'admin',
            meetingUrl: this.data?.meetingUrl,
            locationAddress: isInPerson ? this.normalizeText(formValue.locationAddress) : undefined,
            locationLatitude: isInPerson ? locationLatitude : undefined,
            locationLongitude: isInPerson ? locationLongitude : undefined
        };

        this.sessionService.getSessionsByDate(new Date(formValue.date)).subscribe({
            next: sessionsOnDate => {
                const hasConflict = sessionsOnDate.some(existing => {
                    if (existing.id === session.id || !this.isPlannedStatus(existing.status)) {
                        return false;
                    }

                    const existingDate = existing.date instanceof Date ? existing.date : new Date(existing.date);
                    const existingStart = this.combineDateAndTime(existingDate, existing.startTime);
                    const existingEnd = this.combineDateAndTime(existingDate, existing.endTime);

                    return this.isTimeRangeOverlapping(requestedStart, requestedEnd, existingStart, existingEnd);
                });

                if (hasConflict) {
                    this.isSubmitting = false;
                    this.formError = 'Une session existe deja dans cette plage horaire.';
                    if (typeof window !== 'undefined') {
                        window.alert(this.formError);
                    }
                    return;
                }

                const request$ = this.isEditMode && this.data
                    ? this.sessionService.updateSession(session)
                    : this.sessionService.addSession(session);

                request$.subscribe({
                    next: () => {
                        this.isSubmitting = false;
                        this.dialogRef.close(true);
                    },
                    error: () => {
                        this.isSubmitting = false;
                        this.formError = 'Impossible de sauvegarder la session. Veuillez reessayer.';
                    }
                });
            },
            error: () => {
                this.isSubmitting = false;
                this.formError = 'Impossible de verifier les sessions existantes pour cette date.';
            }
        });
    }

    onCancel(): void {
        this.dialogRef.close();
    }

    useCurrentLocation(): void {
        if (!this.isBrowser || !navigator.geolocation) {
            this.formError = 'La geolocalisation est indisponible sur ce navigateur.';
            return;
        }

        this.isLocating = true;
        navigator.geolocation.getCurrentPosition(
            position => {
                this.isLocating = false;
                this.formError = null;
                this.setLocation(position.coords.latitude, position.coords.longitude, true);
            },
            () => {
                this.isLocating = false;
                this.formError = 'Impossible d obtenir votre position actuelle.';
            },
            { enableHighAccuracy: true, timeout: 10000 }
        );
    }

    hasLocationError(): boolean {
        const hasInvalidLocation = !!this.sessionForm.get('locationLatitude')?.invalid
            || !!this.sessionForm.get('locationLongitude')?.invalid;
        const hasTouchedLocation = !!this.sessionForm.get('locationLatitude')?.touched
            || !!this.sessionForm.get('locationLongitude')?.touched;
        return this.isInPersonMode() && hasInvalidLocation && hasTouchedLocation;
    }

    private isPlannedStatus(status: Session['status']): boolean {
        return this.plannedStatuses.has((status || '').toUpperCase());
    }

    private combineDateAndTime(date: Date, time: string): Date {
        const [hourPart = '0', minutePart = '0'] = (time || '').split(':');
        const hours = Number.parseInt(hourPart, 10);
        const minutes = Number.parseInt(minutePart, 10);

        return new Date(
            date.getFullYear(),
            date.getMonth(),
            date.getDate(),
            Number.isNaN(hours) ? 0 : hours,
            Number.isNaN(minutes) ? 0 : minutes,
            0,
            0
        );
    }

    private isTimeRangeOverlapping(
        startA: Date,
        endA: Date,
        startB: Date,
        endB: Date
    ): boolean {
        return startA.getTime() < endB.getTime() && endA.getTime() > startB.getTime();
    }

    private isInPersonMode(): boolean {
        return this.sessionForm.get('meetingMode')?.value === 'IN_PERSON';
    }

    private updateLocationControls(meetingMode: Session['meetingMode'] | undefined): void {
        const latitudeControl = this.sessionForm.get('locationLatitude');
        const longitudeControl = this.sessionForm.get('locationLongitude');

        if (!latitudeControl || !longitudeControl) {
            return;
        }

        if (meetingMode === 'IN_PERSON') {
            latitudeControl.setValidators([Validators.required, Validators.min(-90), Validators.max(90)]);
            longitudeControl.setValidators([Validators.required, Validators.min(-180), Validators.max(180)]);
            latitudeControl.updateValueAndValidity({ emitEvent: false });
            longitudeControl.updateValueAndValidity({ emitEvent: false });
            this.scheduleMapInitialization();
            return;
        }

        latitudeControl.clearValidators();
        longitudeControl.clearValidators();
        this.sessionForm.patchValue(
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

        if (this.mapInitTimeoutId) {
            clearTimeout(this.mapInitTimeoutId);
        }

        this.mapInitTimeoutId = setTimeout(() => {
            void this.initializeMap();
            this.mapInitTimeoutId = null;
        }, 200);
    }

    private async initializeMap(): Promise<void> {
        if (!this.isBrowser || !this.isInPersonMode()) {
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
        }

        this.map.invalidateSize();

        const latitude = this.toCoordinate(this.sessionForm.get('locationLatitude')?.value);
        const longitude = this.toCoordinate(this.sessionForm.get('locationLongitude')?.value);
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

        this.sessionForm.patchValue({
            locationLatitude: roundedLatitude,
            locationLongitude: roundedLongitude
        });
        this.sessionForm.get('locationLatitude')?.markAsTouched();
        this.sessionForm.get('locationLongitude')?.markAsTouched();

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
