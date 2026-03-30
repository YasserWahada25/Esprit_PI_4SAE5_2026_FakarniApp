import { Component, Inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ROLE_OPTIONS, Role, SignUpRequest } from './models/sign-up.model';
import { AuthService } from './services/auth.service';
import {
    nomValidators,
    prenomValidators,
    emailValidators,
    passwordValidators,
    numTelValidators,
    getControlErrorMessage,
} from './validators/form.validators';

@Component({
    selector: 'app-sign-up',
    standalone: true,
    imports: [ReactiveFormsModule, RouterLink],
    templateUrl: './sign-up.component.html',
    styleUrl: './sign-up.component.css',
})
export class SignUpComponent {
    signUpForm: FormGroup;
    roleOptions = ROLE_OPTIONS;
    errorMessage: string | null = null;
    loading = false;
    hidePassword = true;

    readonly getError = getControlErrorMessage;

    constructor(
        private fb: FormBuilder,
        private router: Router,
        @Inject(AuthService) private authService: AuthService
    ) {
        this.signUpForm = this.fb.group({
            nom: ['', nomValidators],
            prenom: ['', prenomValidators],
            email: ['', emailValidators],
            password: ['', passwordValidators],
            role: [Role.PATIENT_PROFILE, [Validators.required]],
            numTel: ['', numTelValidators],
            adresse: [''],
        });
    }

    togglePasswordVisibility(): void {
        this.hidePassword = !this.hidePassword;
    }

    onSubmit(): void {
        if (this.signUpForm.invalid || this.loading) {
            this.signUpForm.markAllAsTouched();
            return;
        }
        this.errorMessage = null;
        this.loading = true;
        const value = this.signUpForm.value as SignUpRequest;
        this.authService.register(value).subscribe({
            next: () => this.router.navigate(['/auth/signin']),
            error: (err) => {
                this.loading = false;
                this.errorMessage = err?.error?.message || 'Registration failed. Please try again.';
            }
        });
    }
}
