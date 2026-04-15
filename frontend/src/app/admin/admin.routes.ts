import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { adminGuard } from '../auth/guards/admin.guard';

export const ADMIN_ROUTES: Routes = [
    {
        path: '',
        component: MainLayoutComponent,
        canActivate: [adminGuard],
        canActivateChild: [adminGuard],
        children: [
            { path: '', redirectTo: 'users', pathMatch: 'full' },
            {
                path: 'users',
                loadChildren: () => import('./features/user-management/user-management.module').then(m => m.UserManagementModule)
            },
            {
                path: 'sessions-events',
                loadChildren: () => import('./features/session-event/session-event.module').then(m => m.SessionEventModule)
            },
            {
                path: 'medical-monitoring',
                loadChildren: () => import('./features/medical-monitor/medical-monitor.module').then(m => m.MedicalMonitorModule)
            },
            {
                path: 'geolocation',
                loadChildren: () => import('./features/geolocation/geolocation.module').then(m => m.GeolocationModule)
            },
            {
                path: 'educational-content',
                loadChildren: () => import('./features/educational-content/educational-content.module').then(m => m.EducationalContentModule)
            }
        ]
    }
];
