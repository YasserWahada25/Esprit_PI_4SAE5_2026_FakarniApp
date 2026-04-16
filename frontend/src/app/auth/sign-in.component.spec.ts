import { TestBed } from '@angular/core/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { SignInComponent } from './sign-in.component';
import { AuthService } from './services/auth.service';
import { Role } from './models/sign-up.model';

describe('SignInComponent', () => {
  let component: SignInComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [SignInComponent],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    }).compileComponents();

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
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/home']);
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

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/admin']);
  });

  it('should show backend error message on failed login', () => {
    authServiceSpy.login.and.returnValue(
      throwError(() => ({ error: { message: 'Invalid credentials' } }))
    );

    component.loginForm.setValue({ email: 'bad@e.tn', password: 'bad' });
    component.onSubmit();

    expect(component.errorMessage).toBe('Invalid credentials');
    expect(component.loading).toBeFalse();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });
});
