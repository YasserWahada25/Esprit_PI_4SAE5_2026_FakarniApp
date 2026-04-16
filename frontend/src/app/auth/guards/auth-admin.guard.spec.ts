import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { authGuard } from './auth.guard';
import { adminGuard } from './admin.guard';
import { Role } from '../models/sign-up.model';

describe('Auth and Admin guards', () => {
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(() => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['isLoggedIn', 'getCurrentUser']);
    routerSpy = jasmine.createSpyObj<Router>('Router', ['createUrlTree']);
    routerSpy.createUrlTree.and.callFake((commands: readonly any[]) => ({ commands } as unknown as UrlTree));

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });
  });

  it('authGuard should allow logged user', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('authGuard should redirect anonymous user to sign-in', () => {
    authServiceSpy.isLoggedIn.and.returnValue(false);

    const result = TestBed.runInInjectionContext(() => authGuard({} as any, {} as any));

    expect(result instanceof UrlTree).toBeTrue();
    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/auth/signin']);
  });

  it('adminGuard should allow admin user', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getCurrentUser.and.returnValue({
      id: '1',
      nom: 'A',
      prenom: 'B',
      email: 'a@b.tn',
      role: Role.ADMIN,
      numTel: '12345678',
      adresse: 'Tunis',
    });

    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(result).toBeTrue();
  });

  it('adminGuard should redirect authenticated non-admin to /home', () => {
    authServiceSpy.isLoggedIn.and.returnValue(true);
    authServiceSpy.getCurrentUser.and.returnValue({
      id: '1',
      nom: 'P',
      prenom: 'U',
      email: 'p@u.tn',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    });

    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(result instanceof UrlTree).toBeTrue();
    expect(routerSpy.createUrlTree).toHaveBeenCalledWith(['/home']);
  });
});
