import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SignUpComponent } from './sign-up.component';
import { AuthService } from './services/auth.service';
import { Role } from './models/sign-up.model';

describe('SignUpComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['register']);

    await TestBed.configureTestingModule({
      imports: [SignUpComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  it('should show a validation message when the form is invalid', () => {
    const fixture = TestBed.createComponent(SignUpComponent);
    const component = fixture.componentInstance;

    component.onSubmit();

    expect(authService.register).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please fill in the required fields correctly.');
  });

  it('should register a user and redirect to sign in', () => {
    authService.register.and.returnValue(of({
      id: 'user-1',
      nom: 'Sara',
      prenom: 'Ben Ali',
      email: 'sara@example.com',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    }));

    const fixture = TestBed.createComponent(SignUpComponent);
    const component = fixture.componentInstance;
    component.signUpForm.setValue({
      nom: 'Sara',
      prenom: 'Ben Ali',
      email: 'sara@example.com',
      password: 'Password1!',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    });

    component.onSubmit();

    expect(authService.register).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/auth/signin']);
  });

  it('should surface the backend error when registration fails', () => {
    authService.register.and.returnValue(
      throwError(() => ({ error: { message: 'Email déjà utilisé' } }))
    );

    const fixture = TestBed.createComponent(SignUpComponent);
    const component = fixture.componentInstance;
    component.signUpForm.setValue({
      nom: 'Sara',
      prenom: 'Ben Ali',
      email: 'sara@example.com',
      password: 'Password1!',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    });

    component.onSubmit();

    expect(component.errorMessage).toBe('Email déjà utilisé');
    expect(component.loading).toBeFalse();
  });
});
