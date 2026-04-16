import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SignInComponent } from './sign-in.component';
import { AuthService } from './services/auth.service';
import { Role } from './models/sign-up.model';

describe('SignInComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'login',
      'loginWithGoogle',
      'loginWithFacebook',
    ]);

    await TestBed.configureTestingModule({
      imports: [SignInComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  it('should not submit an invalid login form', () => {
    const fixture = TestBed.createComponent(SignInComponent);
    const component = fixture.componentInstance;

    component.onSubmit();

    expect(authService.login).not.toHaveBeenCalled();
    expect(component.loginForm.touched).toBeTrue();
  });

  it('should navigate admins to the admin area after login', () => {
    authService.login.and.returnValue(of({
      user: {
        id: 'user-1',
        nom: 'Sara',
        prenom: 'Ben Ali',
        email: 'sara@example.com',
        role: Role.ADMIN,
        numTel: '12345678',
        adresse: 'Tunis',
      },
    }));

    const fixture = TestBed.createComponent(SignInComponent);
    const component = fixture.componentInstance;
    component.loginForm.setValue({ email: 'sara@example.com', password: 'Password1!' });

    component.onSubmit();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'sara@example.com',
      password: 'Password1!',
    });
    expect(router.navigate).toHaveBeenCalledWith(['/admin']);
  });

  it('should show a backend error message when login fails', () => {
    authService.login.and.returnValue(
      throwError(() => ({ error: { message: 'Invalid email or password.' } }))
    );

    const fixture = TestBed.createComponent(SignInComponent);
    const component = fixture.componentInstance;
    component.loginForm.setValue({ email: 'sara@example.com', password: 'wrong-password' });

    component.onSubmit();

    expect(component.errorMessage).toBe('Invalid email or password.');
    expect(component.loading).toBeFalse();
  });
});
