import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { ActivityService } from '../../admin/core/services/activity.service';
import { EducationalActivity, ActivityType } from '../../admin/core/models/educational-activity.model';
import { catalogThumbForActivity, DEFAULT_GAME_THUMB } from './activity-catalog-assets';

@Component({
    selector: 'app-activities-list',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './activities-list.component.html',
    styleUrls: ['./activities-list.component.css', './activities-shared.css']
})
export class ActivitiesListComponent implements OnInit {
    readonly demoUserId = 1;
    readonly defaultGameThumb = DEFAULT_GAME_THUMB;

    activities: EducationalActivity[] = [];
    loading = true;
    loadError: string | null = null;

    constructor(
        private activityService: ActivityService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadActivities();
    }

    loadActivities(): void {
        this.loading = true;
        this.loadError = null;
        this.activityService
            .getActivities(this.demoUserId)
            .pipe(
                finalize(() => {
                    this.loading = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: list => {
                    this.activities = Array.isArray(list) ? list : [];
                },
                error: err => {
                    console.error('[ActivitiesList]', err);
                    const net =
                        err?.message === 'Failed to fetch' || err?.name === 'TypeError'
                            ? ' Réseau : vérifiez activite-educative-service (8084) et le proxy.'
                            : '';
                    this.loadError =
                        (err?.error?.message ?? err?.message ?? 'Impossible de charger les activités.') + net;
                }
            });
    }

    getTypeLabel(activity: EducationalActivity): string {
        const map: Partial<Record<ActivityType, string>> = {
            quiz: 'Quiz',
            cognitive_game: 'Quiz images (reconnaissance)',
            image_game: 'Memory — paires d’images',
            puzzle_game: 'Puzzle — image',
            video: 'Vidéo',
            content: 'Contenu'
        };
        const t = activity?.type;
        return (t && map[t]) || 'Activité';
    }

    getTypeBadgeClass(activity: EducationalActivity): string {
        switch (activity?.type) {
            case 'quiz':
                return 'badge-quiz';
            case 'cognitive_game':
                return 'badge-cognitive';
            case 'image_game':
                return 'badge-memory';
            case 'puzzle_game':
                return 'badge-memory';
            case 'video':
                return 'badge-video';
            case 'content':
                return 'badge-content';
            default:
                return 'badge-default';
        }
    }

    getIconClass(activity: EducationalActivity): string {
        const k = activity.iconKey || '';
        if (k === 'brain') {
            return 'fa-brain';
        }
        if (k === 'image') {
            return 'fa-image';
        }
        if (k === 'book') {
            return 'fa-book-open';
        }
        if (k === 'puzzle') {
            return 'fa-puzzle-piece';
        }
        if (k === 'video') {
            return 'fa-circle-play';
        }
        switch (activity?.type) {
            case 'quiz':
            case 'cognitive_game':
                return 'fa-brain';
            case 'image_game':
                return 'fa-clone';
            case 'puzzle_game':
                return 'fa-puzzle-piece';
            case 'content':
                return 'fa-book-open';
            case 'video':
                return 'fa-circle-play';
            default:
                return 'fa-shapes';
        }
    }

    getScore(activity: EducationalActivity): number | null {
        const s = activity.latestScorePercent;
        return s != null && !Number.isNaN(s) ? Math.round(s) : null;
    }

    thumbSrc(activity: EducationalActivity): string {
        const u = (activity.thumbnailUrl || '').trim();
        if (u) {
            return u;
        }
        return catalogThumbForActivity(activity.type);
    }

    onThumbError(ev: Event): void {
        const el = ev.target as HTMLImageElement | null;
        if (!el?.src) {
            return;
        }
        if (el.src.includes('assets/default/default-game')) {
            return;
        }
        el.src = this.defaultGameThumb;
    }

    start(activity: EducationalActivity, ev: Event): void {
        ev.stopPropagation();
        if (activity.type === 'content' || activity.type === 'video') {
            this.router.navigate(['/educational/activities', activity.id, 'play'], {
                queryParams: { sheet: '1' }
            });
            return;
        }
        if (
            activity.type === 'quiz' ||
            activity.type === 'cognitive_game' ||
            activity.type === 'image_game' ||
            activity.type === 'puzzle_game'
        ) {
            this.router.navigate(['/educational/activities', activity.id, 'play']);
        }
    }
}
