import { Component, Inject, OnInit, AfterViewInit, OnDestroy, PLATFORM_ID, ChangeDetectorRef } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MapsService } from '../../../../core/services/maps.service';
import { isPlatformBrowser } from '@angular/common';

@Component({
    selector: 'app-map-modal',
    standalone: true,
    imports: [CommonModule, MatDialogModule, MatButtonModule, MatProgressSpinnerModule, MatIconModule],
    templateUrl: './map-modal.component.html',
    styleUrls: ['./map-modal.component.scss']
})
export class MapModalComponent implements OnInit, AfterViewInit {
    loading = true;
    error = '';
    private map: L.Map | undefined;
    formattedAddress = '';

    private L: any; // Store leaflet instance
    private resizeObserver: ResizeObserver | undefined;

    constructor(
        public dialogRef: MatDialogRef<MapModalComponent>,
        @Inject(MAT_DIALOG_DATA) public data: { location: string, title?: string },
        private mapsService: MapsService,
        @Inject(PLATFORM_ID) private platformId: Object,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        if (!this.data.location) {
            this.error = 'Aucun lieu défini pour cet événement.';
            this.loading = false;
            this.cdr.detectChanges();
            return;
        }

        this.mapsService.geocode(this.data.location).subscribe({
            next: (res) => {
                this.loading = false;
                this.formattedAddress = res.formattedAddress;
                this.cdr.detectChanges();

                // L'astuce MAJEURE pour Leaflet + Material Dialog : 
                // On attend que la modale ait FINI son animation d'ouverture avant d'injecter la carte
                if (isPlatformBrowser(this.platformId)) {
                    this.dialogRef.afterOpened().subscribe(async () => {
                        this.L = await import('leaflet');
                        // Small extra tick for the DOM to paint the #event-map div
                        setTimeout(() => this.initMap(res.lat, res.lng), 50);
                    });
                }
            },
            error: (err) => {
                this.loading = false;
                console.error('Erreur geocoding', err);
                this.error = `Lieu "${this.data.location}" introuvable sur la carte.`;
                this.cdr.detectChanges();
            }
        });
    }

    ngAfterViewInit(): void {
        if (isPlatformBrowser(this.platformId) && this.L) {
            // Fix icônes Leaflet par défaut disparues à cause d'Angular/Webpack
            const iconRetinaUrl = 'assets/marker-icon-2x.png';
            const iconUrl = 'assets/marker-icon.png';
            const shadowUrl = 'assets/marker-shadow.png';
            this.L.Icon.Default.imagePath = '';

            // Un hack commun pour les icones Leaflet si les assets ne sont pas servis : 
            // on utilise un CDN public pour les markers
            this.L.Icon.Default.mergeOptions({
                iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
                iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
                shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
            });
        }
    }

    private initMap(lat: number, lng: number): void {
        if (!this.L) return;

        this.map = this.L.map('event-map', {
            center: [lat, lng],
            zoom: 14
        });

        this.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; OpenStreetMap contributors'
        }).addTo(this.map);

        this.L.marker([lat, lng])
            .addTo(this.map)
            .bindPopup(`<b>${this.data.title || 'Événement'}</b><br>${this.formattedAddress}`)
            .openPopup();

        // Use ResizeObserver to reliably call invalidateSize when the dialog animation finishes
        const mapContainer = document.getElementById('event-map');
        if (mapContainer && typeof ResizeObserver !== 'undefined') {
            this.resizeObserver = new ResizeObserver(() => {
                this.map?.invalidateSize();
            });
            this.resizeObserver.observe(mapContainer);
        }

        // Ultimate fallback: explicitly trigger a window resize event after Material Dialog animation (~200-400ms)
        setTimeout(() => {
            this.map?.invalidateSize();
            window.dispatchEvent(new Event('resize'));
        }, 400);
    }

    ngOnDestroy(): void {
        if (this.resizeObserver) {
            this.resizeObserver.disconnect();
        }
        if (this.map) {
            this.map.remove();
        }
    }
}
