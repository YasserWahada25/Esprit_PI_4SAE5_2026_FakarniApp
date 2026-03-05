import { Routes } from '@angular/router';

import { MainLayoutComponent } from './shared/components/main-layout.component';
import { HomeComponent } from './home/home.component';
import { SignInComponent } from './auth/sign-in.component';
import { SignUpComponent } from './auth/sign-up.component';
import { ProfileEditComponent } from './profile/profile-edit.component';

import { ForgotPasswordComponent } from './auth/forgot-password.component';
import { ResetPasswordComponent } from './auth/reset-password.component';

export const routes: Routes = [

    // Default redirect
    { path: '', redirectTo: 'auth/signin', pathMatch: 'full' },

    // Auth routes
    { path: 'auth/signin', component: SignInComponent },
    { path: 'auth/signup', component: SignUpComponent },
    { path: 'auth/forgot-password', component: ForgotPasswordComponent },
    { path: 'auth/reset-password', component: ResetPasswordComponent },

    // Main layout routes
    {
        path: '',
        component: MainLayoutComponent,
        children: [
            { path: 'home', component: HomeComponent },
            { path: 'profile/edit', component: ProfileEditComponent },

            {
                path: 'alzheimer_meeting',
                loadChildren: () =>
                    import('./alzheimer_meeting/alzheimer-meeting.routes')
                        .then(m => m.ALZHEIMER_ROUTES)
            },
            {
                path: 'geofencing',
                loadChildren: () =>
                    import('./geofencing/geofencing.routes')
                        .then(m => m.GEOFENCING_ROUTES)
            },
            {
                path: 'educational',
                loadChildren: () =>
                    import('./educational/educational.routes')
                        .then(m => m.EDUCATIONAL_ROUTES)
            },
            {
                path: 'communication',
                loadChildren: () =>
                    import('./communication/communication.routes')
                        .then(m => m.COMMUNICATION_ROUTES)
            },
            {
                path: 'medical',
                loadChildren: () =>
                    import('./medical/medical.routes')
                        .then(m => m.MEDICAL_ROUTES)
            }
        ]
    },

    // Admin
    {
        path: 'admin',
        loadChildren: () =>
            import('./admin/admin.routes')
                .then(m => m.ADMIN_ROUTES)
    },

    // Fallback
    { path: '**', redirectTo: 'auth/signin' }
];