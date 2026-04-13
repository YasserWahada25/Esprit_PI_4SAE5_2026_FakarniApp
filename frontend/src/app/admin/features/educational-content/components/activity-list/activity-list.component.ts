import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { EducationalActivity, ActivityType } from '../../../../core/models/educational-activity.model';
import { ActivityService } from '../../../../core/services/activity.service';
import { ActivityFormComponent } from '../activity-form/activity-form.component';
import { ActivityQuestionsDialogComponent } from '../activity-questions-dialog/activity-questions-dialog.component';

@Component({
    selector: 'app-activity-list',
    standalone: false,
    templateUrl: './activity-list.component.html',
    styleUrls: ['./activity-list.component.scss']
})
export class ActivityListComponent implements OnInit, AfterViewInit {
    activities: EducationalActivity[] = [];
    dataSource: MatTableDataSource<EducationalActivity>;
    displayedColumns: string[] = ['name', 'type', 'createdDate', 'status', 'actions'];
    loading = true;
    loadError: string | null = null;

    @ViewChild(MatPaginator) paginator!: MatPaginator;
    @ViewChild(MatSort) sort!: MatSort;

    constructor(
        private activityService: ActivityService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar
    ) {
        this.dataSource = new MatTableDataSource();
        this.dataSource.filterPredicate = (data: EducationalActivity, filter: string) => {
            const f = filter.trim().toLowerCase();
            if (!f) {
                return true;
            }
            const blob = [
                data.name,
                data.description ?? '',
                this.getActivityTypeLabel(data.type),
                data.gameType ?? '',
                data.type
            ]
                .join(' ')
                .toLowerCase();
            return blob.includes(f);
        };
    }

    ngOnInit(): void {
        this.loadActivities();
    }

    ngAfterViewInit(): void {
        this.connectPaginatorAndSort();
    }

    /** Nécessaire dès que le tableau / paginator sont dans le DOM (évite « 0 of 0 »). */
    private connectPaginatorAndSort(): void {
        if (this.paginator) {
            this.dataSource.paginator = this.paginator;
        }
        if (this.sort) {
            this.dataSource.sort = this.sort;
        }
    }

    loadActivities(): void {
        this.loading = true;
        this.loadError = null;
        this.activityService.getActivities().subscribe({
            next: activities => {
                const list = Array.isArray(activities) ? activities : [];
                this.activities = list;
                this.dataSource.data = list;
                this.loading = false;
                setTimeout(() => {
                    this.connectPaginatorAndSort();
                });
            },
            error: err => {
                console.error('[ActivityListComponent] loadActivities', err);
                this.loadError =
                    err?.error?.message ??
                    err?.message ??
                    'Impossible de charger les activités.';
                this.activities = [];
                this.dataSource.data = [];
                this.loading = false;
            }
        });
    }

    applyFilter(event: Event): void {
        const filterValue = (event.target as HTMLInputElement).value;
        this.dataSource.filter = filterValue.trim().toLowerCase();
    }

    getActivityTypeLabel(type: ActivityType): string {
        const labels: { [key in ActivityType]: string } = {
            quiz: 'Quiz (texte)',
            cognitive_game: 'Jeu cognitif (images)',
            image_game: 'Memory — paires',
            video: 'Vidéo',
            content: 'Contenu'
        };
        return labels[type] ?? String(type ?? '');
    }

    getActivityTypeClass(type: ActivityType): string {
        return (type ?? 'unknown').replace('_', '-');
    }

    openActivityDialog(activity?: EducationalActivity): void {
        const dialogRef = this.dialog.open(ActivityFormComponent, {
            width: '700px',
            data: activity
        });

        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.loadActivities();
                this.snackBar.open(
                    activity ? 'Activité modifiée avec succès' : 'Activité ajoutée avec succès',
                    'Fermer',
                    { duration: 3000 }
                );
            }
        });
    }

    isGameActivity(activity: EducationalActivity): boolean {
        const t = activity?.type;
        return t === 'quiz' || t === 'cognitive_game' || t === 'image_game';
    }

    openQuestionsDialog(activity: EducationalActivity): void {
        const ref = this.dialog.open(ActivityQuestionsDialogComponent, {
            width: '760px',
            maxHeight: '90vh',
            data: { activity }
        });
        ref.afterClosed().subscribe(() => this.loadActivities());
    }

    deleteActivity(activity: EducationalActivity): void {
        if (confirm(`Êtes-vous sûr de vouloir supprimer l'activité "${activity.name}" ?`)) {
            this.activityService.deleteActivity(activity.id).subscribe({
                next: () => {
                    this.loadActivities();
                    this.snackBar.open('Activité supprimée avec succès', 'Fermer', { duration: 3000 });
                },
                error: () => {
                    this.snackBar.open('Suppression impossible', 'Fermer', { duration: 4000 });
                }
            });
        }
    }
}
