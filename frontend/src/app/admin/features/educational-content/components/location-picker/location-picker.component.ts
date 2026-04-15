import {
    Component, OnInit, OnDestroy, AfterViewInit,
    Output, EventEmitter, Input, NgZone
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';

export interface LocationSelection {
    lat: number;
    lng: number;
    address: string;
}

@Component({
    selector: 'app-location-picker',
    standalone: true,
    imports: [CommonModule, MatIconModule, MatProgressSpinnerModule],
    templateUrl: './location-picker.component.html',
    styleUrls: ['./location-picker.component.scss']
})
export class LocationPickerComponent implements OnInit, AfterViewInit, OnDestroy {
    /** Initial coordinates — used when editing an event with known location */
    @Input() initialLat?: number;
    @Input() initialLng?: number;

    /** Emitted every time the user picks a location */
    @Output() locationSelected = new EventEmitter<LocationSelection>();

    selectedAddress: string | null = null;
    geocodingError: string | null = null;
    isLoading = false;

    private map: any;
    private marker: any;
    private L: any;

    constructor(private http: HttpClient, private zone: NgZone) { }

    ngOnInit(): void { }

    async ngAfterViewInit(): Promise<void> {
        // Dynamically import Leaflet to keep it out of the main bundle
        this.L = await import('leaflet');
        this.initMap();

        // Defer invalidateSize so the map calculates correct dimensions
        // after the Angular Material dialog's open animation completes (~300ms)
        setTimeout(() => {
            if (this.map) {
                this.map.invalidateSize();
            }
        }, 350);
    }

    private initMap(): void {
        const L = this.L;

        const defaultLat = this.initialLat ?? 36.8065;
        const defaultLng = this.initialLng ?? 10.1815;

        this.map = L.map('location-picker-map', { zoomControl: true }).setView(
            [defaultLat, defaultLng],
            this.initialLat ? 14 : 11
        );

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
            maxZoom: 19
        }).addTo(this.map);

        // If initial coordinates are provided (edit mode), place the marker
        if (this.initialLat && this.initialLng) {
            this.placeMarker(this.initialLat, this.initialLng);
            this.reverseGeocode(this.initialLat, this.initialLng);
        }

        // Click handler
        this.map.on('click', (e: any) => {
            this.zone.run(() => {
                const { lat, lng } = e.latlng;
                this.geocodingError = null;
                this.placeMarker(lat, lng);
                this.reverseGeocode(lat, lng);
            });
        });
    }

    private placeMarker(lat: number, lng: number): void {
        const L = this.L;

        // Fix default Leaflet marker icon path issue with Angular
        const icon = L.icon({
            iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
            iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
            shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41]
        });

        if (this.marker) {
            // Move existing marker instantly to the new coordinates
            this.marker.setLatLng([lat, lng]);
        } else {
            this.marker = L.marker([lat, lng], { icon, draggable: true }).addTo(this.map);

            // Allow dragging to update address
            this.marker.on('dragend', (e: any) => {
                this.zone.run(() => {
                    const pos = e.target.getLatLng();
                    this.geocodingError = null;
                    this.placeMarker(pos.lat, pos.lng);
                    this.reverseGeocode(pos.lat, pos.lng);
                });
            });
        }

        // Re-center the map on the selected point immediately (no page reload needed)
        this.map.panTo([lat, lng]);

        // Force Leaflet to recalculate tile layout (handles dialog animation sizing)
        this.map.invalidateSize();
    }

    private reverseGeocode(lat: number, lng: number): void {
        this.isLoading = true;
        this.selectedAddress = null;

        const url = `${environment.apiBaseUrl}/api/maps/reverse-geocode?lat=${lat}&lng=${lng}`;
        this.http.get<{ lat: number; lng: number; formattedAddress: string }>(url).subscribe({
            next: (result) => {
                this.isLoading = false;
                this.selectedAddress = result.formattedAddress;
                this.locationSelected.emit({ lat, lng, address: result.formattedAddress });
            },
            error: () => {
                this.isLoading = false;
                this.geocodingError = 'Impossible de récupérer l\'adresse pour cet endroit. Essayez une autre zone.';
                // Still emit coordinates even if reverse geocoding fails
                this.locationSelected.emit({ lat, lng, address: `${lat.toFixed(5)}, ${lng.toFixed(5)}` });
            }
        });
    }

    ngOnDestroy(): void {
        if (this.map) {
            this.map.remove();
        }
    }
}
