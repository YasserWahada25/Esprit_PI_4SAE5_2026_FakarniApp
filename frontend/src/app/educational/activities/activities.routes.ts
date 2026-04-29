import { Routes } from '@angular/router';

const routes: Routes = [
    {
        path: '',
        loadComponent: () =>
            import('./activities-list.component').then(m => m.ActivitiesListComponent)
    },
    {
        path: ':id/play',
        loadComponent: () =>
            import('./activity-play.component').then(m => m.ActivityPlayComponent)
    }
];

export default routes;
