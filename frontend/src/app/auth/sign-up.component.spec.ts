import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { SignUpComponent } from './sign-up.component';
import { AuthService } from './services/auth.service';
import { Role } from './models/sign-up.model';

describe('SignUpComponent', () => {
  let component: SignUpComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['register']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [SignUpComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

    component = TestBed.createComponent(SignUpComponent).componentInstance;
  });

  function fillValidForm() {
    component.signUpForm.setValue({
      nom: 'Yasser',
      prenom: 'Wahada',
      email: 'yasser.new@esprit.tn',
      password: 'Strong1!',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    });
  }

  it('should register and redirect to sign-in on success', () => {
    authServiceSpy.register.and.returnValue(
      of({
        id: 'u1',
        nom: 'Yasser',
        prenom: 'Wahada',
        email: 'yasser.new@esprit.tn',
        role: Role.PATIENT_PROFILE,
        numTel: '12345678',
        adresse: 'Tunis',
      })
    );

    fillValidForm();
    component.onSubmit();

    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/signin']);
  });

  it('should display specific message on 409 conflict', () => {
    authServiceSpy.register.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 409,
            error: { message: 'This email is already registered.' },
          })
      )
    );

    fillValidForm();
    component.onSubmit();

    expect(component.errorMessage).toBe('This email is already registered.');
    expect(component.loading).toBeFalse();
  });

  it('should stop and show validation message for invalid form', () => {
    component.signUpForm.patchValue({ email: 'invalid-mail' });

    component.onSubmit();

    expect(authServiceSpy.register).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('Please fill in the required fields correctly.');
  });
});
