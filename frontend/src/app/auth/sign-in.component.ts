import { Component, ElementRef, Inject, OnDestroy, OnInit, AfterViewInit, PLATFORM_ID, ViewChild } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthService } from './services/auth.service';
import { Role } from './models/sign-up.model';
import { environment } from '../../environments/environment';

declare global {
    interface Window {
        google?: {
            accounts: {
                id: {
                    initialize: (config: {
                        client_id: string;
                        callback: (response: { credential?: string }) => void;
                        auto_select?: boolean;
                    }) => void;
                    prompt: () => void;
                    renderButton: (element: HTMLElement, options: Record<string, unknown>) => void;
                };
            };
        };
        FB?: {
            init: (config: Record<string, unknown>) => void;
            login: (
                callback: (response: { authResponse?: { accessToken: string } }) => void,
                options?: { scope?: string }
            ) => void;
        };
        fbAsyncInit?: () => void;
    }
}

@Component({
    selector: 'app-sign-in',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './sign-in.component.html',
    styleUrl: './sign-in.component.css'
})
export class SignInComponent implements OnInit, AfterViewInit, OnDestroy {
    loginForm: FormGroup;
    errorMessage: string | null = null;
    loading = false;
    readonly showGoogleLogin = Boolean(environment.googleClientId);
    private readonly googleClientId = environment.googleClientId;
    private readonly facebookAppId = environment.facebookAppId;
    private readonly isBrowser: boolean;
    private destroyed = false;
    private googleIdentityInitialized = false;
    private googleScriptEl: HTMLScriptElement | null = null;
    private facebookScriptEl: HTMLScriptElement | null = null;
    @ViewChild('googleOverlay', { read: ElementRef })
    private googleOverlay?: ElementRef<HTMLElement>;

    constructor(
        @Inject(PLATFORM_ID) platformId: object,
        private fb: FormBuilder,
        private router: Router,
        private authService: AuthService
    ) {
        this.isBrowser = isPlatformBrowser(platformId);
        this.loginForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            password: ['', Validators.required]
        });
    }

    ngOnInit(): void {
        if (!this.isBrowser) {
            return;
        }
        this.initFacebookSdk();
    }

    ngAfterViewInit(): void {
        if (!this.isBrowser || this.destroyed) {
            return;
        }
        setTimeout(() => {
            if (!this.destroyed) {
                this.initGoogleSdk();
            }
        }, 0);
    }

    ngOnDestroy(): void {
        this.destroyed = true;
        if (!this.isBrowser) {
            return;
        }
        if (this.facebookScriptEl?.parentNode) {
            this.facebookScriptEl.parentNode.removeChild(this.facebookScriptEl);
        }
    }

    onSubmit(): void {
        if (this.loginForm.invalid || this.loading) return;
        this.errorMessage = null;
        this.loading = true;
        this.authService.login(this.loginForm.value).subscribe({
            next: (res) => {
                this.loading = false;
                const target = res?.user?.role === Role.ADMIN ? '/admin' : '/home';
                this.router.navigate([target]);
            },
            error: (err: HttpErrorResponse) => {
                this.loading = false;
                this.errorMessage = err?.error?.message || 'Invalid email or password.';
            }
        });
    }

    onFacebookLogin(): void {
        if (this.loading) return;
        if (!this.isBrowser) return;
        if (!window.FB) {
            this.errorMessage = 'Facebook SDK not ready. Check facebookAppId configuration.';
            return;
        }
        this.errorMessage = null;
        this.loading = true;
        window.FB.login((response) => {
            const token = response?.authResponse?.accessToken;
            if (!token) {
                this.loading = false;
                this.errorMessage = 'Facebook authentication was cancelled.';
                return;
            }
            this.authService.loginWithFacebook(token).subscribe({
                next: (res) => {
                    this.loading = false;
                    const target = res?.user?.role === Role.ADMIN ? '/admin' : '/home';
                    this.router.navigate([target]);
                },
                error: (err: HttpErrorResponse) => {
                    this.loading = false;
                    this.errorMessage = err?.error?.message || 'Facebook login failed.';
                }
            });
        }, { scope: 'public_profile,email' });
    }

    private initGoogleSdk(): void {
        if (!this.isBrowser || this.destroyed) {
            return;
        }
        if (!this.googleClientId) {
            return;
        }
        if (window.google?.accounts?.id) {
            this.setupGoogleIdentity();
            return;
        }
        const gsiUrl = 'https://accounts.google.com/gsi/client';
        const existing = document.querySelector<HTMLScriptElement>(`script[src="${gsiUrl}"]`);
        if (existing) {
            this.googleScriptEl = existing;
            const onReady = () => {
                if (!this.destroyed) {
                    this.setupGoogleIdentity();
                }
            };
            if (window.google?.accounts?.id) {
                onReady();
            } else {
                existing.addEventListener('load', onReady, { once: true });
            }
            return;
        }
        if (this.googleScriptEl) {
            return;
        }
        this.googleScriptEl = document.createElement('script');
        this.googleScriptEl.src = gsiUrl;
        this.googleScriptEl.async = true;
        this.googleScriptEl.defer = true;
        this.googleScriptEl.onload = () => {
            if (!this.destroyed) {
                this.setupGoogleIdentity();
            }
        };
        document.head.appendChild(this.googleScriptEl);
    }

    private setupGoogleIdentity(retry = 0): void {
        if (!this.isBrowser || this.destroyed) {
            return;
        }
        if (!window.google?.accounts?.id || !this.googleClientId) {
            return;
        }
        if (!this.googleIdentityInitialized) {
            window.google.accounts.id.initialize({
                client_id: this.googleClientId,
                callback: (response) => {
                    const credential = response?.credential;
                    if (!credential) {
                        this.errorMessage = 'Google authentication failed.';
                        return;
                    }
                    this.loading = true;
                    this.authService.loginWithGoogle(credential).subscribe({
                        next: (res) => {
                            this.loading = false;
                            const target = res?.user?.role === Role.ADMIN ? '/admin' : '/home';
                            this.router.navigate([target]);
                        },
                        error: (err: HttpErrorResponse) => {
                            this.loading = false;
                            this.errorMessage = err?.error?.message || 'Google login failed.';
                        }
                    });
                }
            });
            this.googleIdentityInitialized = true;
        }

        const googleButtonContainer = document.getElementById('google-signin-button');
        if (!googleButtonContainer) {
            return;
        }
        const host = this.googleOverlay?.nativeElement;
        const rawW = host ? host.getBoundingClientRect().width : 0;
        if (rawW < 8 && retry < 8) {
            requestAnimationFrame(() => this.setupGoogleIdentity(retry + 1));
            return;
        }
        const w = Math.min(400, Math.max(220, Math.floor(rawW > 0 ? rawW : 400)));

        googleButtonContainer.innerHTML = '';
        window.google.accounts.id.renderButton(googleButtonContainer, {
            type: 'standard',
            theme: 'outline',
            size: 'large',
            shape: 'rectangular',
            text: 'signin_with',
            width: w,
            logo_alignment: 'left',
            locale: 'en'
        });
    }

    private initFacebookSdk(): void {
        if (!this.isBrowser) {
            return;
        }
        if (!this.facebookAppId) {
            return;
        }
        if (window.FB) {
            return;
        }
        window.fbAsyncInit = () => {
            window.FB?.init({
                appId: this.facebookAppId,
                cookie: true,
                xfbml: false,
                version: 'v21.0'
            });
        };
        this.facebookScriptEl = document.createElement('script');
        this.facebookScriptEl.async = true;
        this.facebookScriptEl.defer = true;
        this.facebookScriptEl.crossOrigin = 'anonymous';
        this.facebookScriptEl.src = 'https://connect.facebook.net/en_US/sdk.js';
        document.head.appendChild(this.facebookScriptEl);
    }
}
