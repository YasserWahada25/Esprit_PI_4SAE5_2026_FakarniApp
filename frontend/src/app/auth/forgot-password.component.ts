import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from './services/auth.service';

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './forgot-password.component.html',
    styleUrl: './sign-in.component.css'
})
export class ForgotPasswordComponent {
    forgotForm: FormGroup;
    loading = false;
    successMessage: string | null = null;
    errorMessage: string | null = null;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService
    ) {
        this.forgotForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]]
        });
    }

    onSubmit(): void {
        if (this.forgotForm.invalid || this.loading) {
            this.forgotForm.markAllAsTouched();
            return;
        }
        this.errorMessage = null;
        this.successMessage = null;
        this.loading = true;

        this.authService.forgotPassword(this.forgotForm.value.email).subscribe({
            next: (res) => {
                this.loading = false;
                this.successMessage = res?.message || 'A password reset link has been sent to your email.';
            },
            error: (err) => {
                this.loading = false;
                this.errorMessage = err?.error?.message || 'An error occurred. Please try again.';
            }
        });
    }
}
