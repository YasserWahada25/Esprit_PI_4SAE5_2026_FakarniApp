import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from './services/auth.service';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('newPassword');
    const confirm = control.get('confirmPassword');
    if (password && confirm && password.value !== confirm.value) {
        confirm.setErrors({ mismatch: true });
        return { mismatch: true };
    }
    if (confirm?.hasError('mismatch') && password?.value === confirm?.value) {
        confirm.setErrors(null);
    }
    return null;
}

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './reset-password.component.html',
    styleUrl: './sign-in.component.css'
})
export class ResetPasswordComponent {
    resetForm: FormGroup;
    loading = false;
    successMessage: string | null = null;
    errorMessage: string | null = null;
    hidePassword = true;
    hideConfirm = true;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        private authService: AuthService
    ) {
        this.resetForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]],
            code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern(/^\d{6}$/)]],
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required]
        }, { validators: passwordMatchValidator });
    }

    togglePasswordVisibility(): void {
        this.hidePassword = !this.hidePassword;
    }

    toggleConfirmVisibility(): void {
        this.hideConfirm = !this.hideConfirm;
    }

    onSubmit(): void {
        if (this.resetForm.invalid || this.loading) {
            this.resetForm.markAllAsTouched();
            return;
        }
        this.errorMessage = null;
        this.successMessage = null;
        this.loading = true;

        const { email, code, newPassword } = this.resetForm.value;

        this.authService.resetPassword(email, code, newPassword).subscribe({
            next: (res: { message?: string }) => {
                this.loading = false;
                this.successMessage = res?.message || 'Votre mot de passe a été réinitialisé avec succès !';
                setTimeout(() => this.router.navigate(['/auth/signin']), 3000);
            },
            error: (err: HttpErrorResponse) => {
                this.loading = false;
                this.errorMessage = err?.error?.message || 'Une erreur est survenue. Veuillez réessayer.';
            }
        });
    }
}
