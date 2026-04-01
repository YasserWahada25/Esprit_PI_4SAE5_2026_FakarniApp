import { Component, Inject, OnInit, PLATFORM_ID, NgZone } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './services/auth.service';

// Declare the Google global so TypeScript doesn't complain
declare const google: any;

@Component({
    selector: 'app-sign-in',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        RouterLink
    ],
    templateUrl: './sign-in.component.html',
    styleUrl: './sign-in.component.css'
})
export class SignInComponent implements OnInit {

    loginForm: FormGroup;
    errorMessage: string | null = null;
    loading = false;
    hidePassword = true;
    googleLoading = false;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private ngZone: NgZone,
        @Inject(AuthService) private authService: AuthService,
        @Inject(PLATFORM_ID) private platformId: Object
    ) {
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        if (!isPlatformBrowser(this.platformId)) return;
        this.loadGoogleScript();
    }

    private loadGoogleScript(): void {
        // Avoid double-loading
        if (document.getElementById('google-gsi-script')) return;

        const script = document.createElement('script');
        script.id = 'google-gsi-script';
        script.src = 'https://accounts.google.com/gsi/client';
        script.async = true;
        script.defer = true;
        document.head.appendChild(script);
    }

    signInWithGoogle(): void {
        if (!isPlatformBrowser(this.platformId)) return;

        if (typeof google === 'undefined') {
            // Script may not be ready yet; wait a bit and retry
            console.warn('Google GSI script not yet loaded, retrying...');
            setTimeout(() => this.signInWithGoogle(), 500);
            return;
        }

        this.googleLoading = true;
        this.errorMessage = null;

        google.accounts.id.initialize({
            client_id: '968599520946-llp69cv61a73f9457lpedn7m4tflrr2t.apps.googleusercontent.com',
            callback: (response: any) => {
                // This runs outside Angular zone, so we need NgZone
                this.ngZone.run(() => {
                    if (!response?.credential) {
                        this.googleLoading = false;
                        this.errorMessage = 'Google sign-in was cancelled.';
                        return;
                    }
                    this.authService.loginWithGoogle(response.credential).subscribe({
                        next: () => {
                            this.googleLoading = false;
                            this.router.navigate(['/home']);
                        },
                        error: (err) => {
                            this.googleLoading = false;
                            this.errorMessage = err?.error?.message || 'Google sign-in failed.';
                        }
                    });
                });
            },
            auto_select: false,
            cancel_on_tap_outside: true
        });

        google.accounts.id.prompt(); // Shows One Tap popup
    }

    togglePasswordVisibility(): void {
        this.hidePassword = !this.hidePassword;
    }

    onSubmit(): void {

        if (this.loginForm.invalid || this.loading) {
            this.loginForm.markAllAsTouched();
            return;
        }

        this.errorMessage = null;
        this.loading = true;

        this.authService.login(this.loginForm.value).subscribe({

            next: () => this.router.navigate(['/home']),

            error: (err) => {
                this.loading = false;
                this.errorMessage =
                    err?.error?.message || 'Invalid email or password.';
            }

        });

    }

}