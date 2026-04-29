import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
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
export class ResetPasswordComponent implements OnInit {
    resetForm: FormGroup;
    loading = false;
    successMessage: string | null = null;
    errorMessage: string | null = null;
    token: string | null = null;
    hidePassword = true;
    hideConfirm = true;

    constructor(
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private authService: AuthService
    ) {
        this.resetForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', Validators.required]
        }, { validators: passwordMatchValidator });
    }

    ngOnInit(): void {
        this.token = this.route.snapshot.queryParamMap.get('token');
        if (!this.token) {
            this.errorMessage = 'Invalid or missing reset token. Please request a new password reset.';
        }
    }

    togglePasswordVisibility(): void {
        this.hidePassword = !this.hidePassword;
    }

    toggleConfirmVisibility(): void {
        this.hideConfirm = !this.hideConfirm;
    }

    onSubmit(): void {
        if (this.resetForm.invalid || this.loading || !this.token) {
            this.resetForm.markAllAsTouched();
            return;
        }
        this.errorMessage = null;
        this.successMessage = null;
        this.loading = true;

        this.authService.resetPassword(this.token, this.resetForm.value.newPassword).subscribe({
            next: (res: { message?: string }) => {
                this.loading = false;
                this.successMessage = res?.message || 'Your password has been reset successfully!';
                setTimeout(() => this.router.navigate(['/auth/signin']), 3000);
            },
            error: (err: HttpErrorResponse) => {
                this.loading = false;
                this.errorMessage = err?.error?.message || 'An error occurred. Please try again.';
            }
        });
    }
}
