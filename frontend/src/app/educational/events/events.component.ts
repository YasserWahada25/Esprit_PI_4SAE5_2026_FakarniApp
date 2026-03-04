import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { EducationalEventService } from '../../admin/core/services/educational-event.service';
import { EducationalEvent } from '../../admin/core/models/educational-event.model';
import { MapModalComponent } from '../../admin/features/educational-content/components/map-modal/map-modal.component';

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, MatDialogModule],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class EventsComponent implements OnInit {
  events: EducationalEvent[] = [];
  loading = false;
  error = '';

  constructor(
    private eventService: EducationalEventService,
    private cdr: ChangeDetectorRef,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loading = true;
    console.log('[EventsComponent] Initialisation — chargement des événements...');

    // On s'abonne au flux partagé
    this.eventService.events$.subscribe({
      next: (data) => {
        console.log('[EventsComponent] Données reçues via events$ :', data);
        this.events = data;
        this.loading = false;
        this.cdr.detectChanges();
      }
    });

    // Premier chargement depuis l'API
    this.eventService.reloadEvents().subscribe({
      error: (err) => {
        console.error('[EventsComponent] Erreur lors du chargement des événements :', err);
        this.error = 'Impossible de charger les événements. Vérifiez que le serveur est démarré.';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  openMap(event: EducationalEvent): void {
    console.log(`[EventsComponent] Clic sur "Localiser" pour l'événement ID: ${event.id}, Titre: "${event.title}", Lieu brut: "${event.location}"`);

    if (!event.location || event.location.trim() === '') {
      console.warn(`[EventsComponent] Impossible d'ouvrir la carte : Le lieu est vide pour l'événement "${event.title}".`);
      return;
    }

    console.log(`[EventsComponent] Ouverture du MapModalComponent pour le lieu: "${event.location}"`);
    try {
      const dialogRef = this.dialog.open(MapModalComponent, {
        width: '600px',
        data: { location: event.location, title: event.title }
      });
      console.log('[EventsComponent] Dialog ouvert avec succès', dialogRef);
    } catch (e) {
      console.error('[EventsComponent] ERREUR LORS DE L\'OUVERTURE DU DIALOG:', e);
    }

    // Forcer la détection de changements pour Angular Zoneless
    this.cdr.detectChanges();
  }
}
