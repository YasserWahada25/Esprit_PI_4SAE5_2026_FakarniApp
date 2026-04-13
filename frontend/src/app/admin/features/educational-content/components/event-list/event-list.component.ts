import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EducationalEvent, EventStatus } from '../../../../core/models/educational-event.model';
import { EducationalEventService } from '../../../../core/services/educational-event.service';
import { EventFormComponent } from '../event-form/event-form.component';
import { MapModalComponent } from '../map-modal/map-modal.component';

@Component({
    selector: 'app-event-list',
    standalone: false,
    templateUrl: './event-list.component.html',
    styleUrls: ['./event-list.component.scss']
})
export class EventListComponent implements OnInit {
    events: EducationalEvent[] = [];
    dataSource: MatTableDataSource<EducationalEvent>;
    displayedColumns: string[] = ['title', 'date', 'time', 'status', 'participants', 'map', 'actions'];

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(
        private eventService: EducationalEventService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
        private cdr: ChangeDetectorRef
    ) {
        this.dataSource = new MatTableDataSource<EducationalEvent>();
    }

    ngOnInit(): void {
        // On s'abonne au flux partagé des événements
        this.eventService.events$.subscribe(rawEvents => {
            const now = new Date();
            this.events = rawEvents.map(event => {
                const eventDate = new Date(event.startDateTime);
                return {
                    ...event,
                    status: eventDate < now ? 'completed' : 'scheduled',
                    participantsCount: event.participantsCount ?? null
                } as EducationalEvent;
            });

            this.dataSource.data = this.events;
            this.dataSource.paginator = this.paginator;
            this.dataSource.sort = this.sort;
            // Avec le mode zoneless, on force le rafraîchissement de la vue
            this.cdr.detectChanges();
        });

        // Premier chargement depuis l'API
        this.eventService.reloadEvents().subscribe();
    }

    private reloadFromApi(): void {
        this.eventService.reloadEvents().subscribe();
    }

    applyFilter(event: Event): void {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    getStatusLabel(status: EventStatus): string {
        const labels: { [key in EventStatus]: string } = {
            'scheduled': 'Programmé',
            'ongoing': 'En cours',
            'completed': 'Terminé',
            'cancelled': 'Annulé'
        };
        return labels[status];
    }

    openEventDialog(event?: EducationalEvent): void {
        const dialogRef = this.dialog.open(EventFormComponent, {
            width: '700px',
            data: event
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.snackBar.open(
                    event ? 'Événement modifié avec succès' : 'Événement ajouté avec succès',
                    'Fermer',
                    { duration: 3000 }
                );
            }
        });
    }

    openMap(event: EducationalEvent): void {
        this.dialog.open(MapModalComponent, {
            width: '600px',
            data: { location: event.location, title: event.title }
        });
    }

    
    deleteEvent(event: EducationalEvent): void {
        if (confirm(`Êtes-vous sûr de vouloir supprimer l'événement "${event.title}" ?`)) {
            this.eventService.deleteEvent(event.id).subscribe({
                next: () => {
                    // Mise à jour instantanée de la liste au niveau du composant
                    this.events = this.events.filter(e => e.id !== event.id);
                    this.dataSource.data = [...this.events];
                    this.snackBar.open('Événement supprimé avec succès', 'Fermer', { duration: 3000 });
                },
                error: () => {
                    this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 3000 });
                }
            });
        }
    }

    sendTestEmail(): void {
        this.eventService.sendTestEmail().subscribe({
            next: () => {
                this.snackBar.open('Email de test envoyé avec succès à testuser@example.com !', 'Fermer', {
                    duration: 4000
                });
            },
            error: (err) => {
                console.error('Erreur lors de l\'envoi de l\'email de test', err);
                this.snackBar.open('Erreur : Impossible d\'envoyer l\'email de test.', 'Fermer', {
                    duration: 5000
                });
            }
        });
    }
}
