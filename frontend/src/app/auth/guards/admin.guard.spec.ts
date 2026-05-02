import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { AuthService } from '../services/auth.service';
import { adminGuard } from './admin.guard';
import { Role } from '../models/sign-up.model';

describe('adminGuard', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(() => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', [
      'getCurrentUser',
      'isLoggedIn',
    ]);

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideRouter([]),
        { provide: AuthService, useValue: authService },
      ],
    });

    router = TestBed.inject(Router);
  });

  it('should allow admins to access protected routes', () => {
    authService.isLoggedIn.and.returnValue(true);
    authService.getCurrentUser.and.returnValue({
      id: 'user-1',
      nom: 'Sara',
      prenom: 'Ben Ali',
      email: 'sara@example.com',
      role: Role.ADMIN,
      numTel: '12345678',
      adresse: 'Tunis',
    });

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(result).toBeTrue();
  });

  it('should redirect unauthenticated users to the sign-in page', () => {
    authService.getCurrentUser.and.returnValue(null);
    authService.isLoggedIn.and.returnValue(false);
    spyOn(router, 'createUrlTree').and.callThrough();

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot)
    );

    expect(router.createUrlTree).toHaveBeenCalledWith(['/auth/signin']);
    expect(result).toEqual(router.createUrlTree(['/auth/signin']));
  });
});
