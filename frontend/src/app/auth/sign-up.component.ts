import { Component } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ROLE_OPTIONS, Role, SignUpRequest } from './models/sign-up.model';
import { AuthService } from './services/auth.service';
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

  private readonly namePattern = /^[A-Za-zÀ-ÖØ-öø-ÿ\s'-]+$/;
  private readonly phonePattern = /^[0-9]{8}$/;
  private readonly passwordPattern = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{6,}$/;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.signUpForm = this.fb.group(
      {
        nom: ['', [Validators.required, Validators.pattern(this.namePattern)]],
        prenom: ['', [Validators.required, Validators.pattern(this.namePattern)]],
        email: ['', [Validators.required, Validators.email]],
        password: [
          '',
          [
            Validators.required,
            Validators.minLength(6),
            Validators.pattern(this.passwordPattern),
          ],
        ],
        role: [Role.PATIENT_PROFILE, Validators.required],
        numTel: ['', [Validators.pattern(this.phonePattern)]],
        adresse: ['', [Validators.required]],
      },
      { updateOn: 'change' }
    );
  }

  getControl(name: string) {
    return this.signUpForm.get(name);
  }

  // ✅ afficher erreur si invalide ET (touched OU dirty)
  shouldShowError(controlName: string): boolean {
    const c = this.getControl(controlName);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  onSubmit(): void {
    if (this.loading) return;

    // ✅ IMPORTANT: force l’affichage des erreurs même si l’utilisateur n’a rien touché
    this.signUpForm.markAllAsTouched();

    if (this.signUpForm.invalid) {
      this.errorMessage = 'Please fill in the required fields correctly.';
      return;
    }

    this.errorMessage = null;
    this.loading = true;

    const value = this.signUpForm.value as SignUpRequest;

    this.authService.register(value).subscribe({
      next: () => this.router.navigate(['/auth/signin']),
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message || 'Registration failed. Please try again.';
      },
    });
  }
}
