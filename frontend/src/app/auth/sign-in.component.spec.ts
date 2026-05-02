import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideRouter, Router } from '@angular/router';
import { EMPTY, of, throwError } from 'rxjs';
import { SignInComponent } from './sign-in.component';
import { AuthService } from './services/auth.service';
import { GoogleSignInService } from './services/google-sign-in.service';
import { Role } from './models/sign-up.model';

describe('SignInComponent', () => {
  let component: SignInComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let navigateSpy: jasmine.Spy;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login']);

    await TestBed.configureTestingModule({
      imports: [SignInComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        {
          provide: GoogleSignInService,
          useValue: {
            ensureInitialized: (): void => {},
            credentials$: EMPTY,
          },
        },
      ],
    }).compileComponents();

    navigateSpy = spyOn(TestBed.inject(Router), 'navigate');

    component = TestBed.createComponent(SignInComponent).componentInstance;
  });

  it('should navigate to /home on successful patient login', () => {
    authServiceSpy.login.and.returnValue(
      of({
        accessToken: 'a',
        refreshToken: 'r',
        user: {
          id: 'u1',
          nom: 'Y',
          prenom: 'W',
          email: 'y@e.tn',
          role: Role.PATIENT_PROFILE,
          numTel: '12345678',
          adresse: 'Tunis',
        },
      })
    );

    component.loginForm.setValue({ email: 'y@e.tn', password: 'Pwd123!' });
    component.onSubmit();

    expect(authServiceSpy.login).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/home']);
    expect(component.errorMessage).toBeNull();
    expect(component.loading).toBeFalse();
  });

  it('should navigate to /admin on successful admin login', () => {
    authServiceSpy.login.and.returnValue(
      of({
        accessToken: 'a',
        refreshToken: 'r',
        user: {
          id: 'u1',
          nom: 'Admin',
          prenom: 'A',
          email: 'admin@e.tn',
          role: Role.ADMIN,
          numTel: '12345678',
          adresse: 'Tunis',
        },
      })
    );

    component.loginForm.setValue({ email: 'admin@e.tn', password: 'Pwd123!' });
    component.onSubmit();

    expect(navigateSpy).toHaveBeenCalledWith(['/admin']);
  });

  it('should show backend error message on failed login', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({ error: { message: 'Invalid credentials' } }))
    );

    component.loginForm.setValue({ email: 'bad@e.tn', password: 'bad' });
    component.onSubmit();

    expect(component.errorMessage).toBe('Invalid credentials');
    expect(component.loading).toBeFalse();
    expect(navigateSpy).not.toHaveBeenCalled();
  });
});
