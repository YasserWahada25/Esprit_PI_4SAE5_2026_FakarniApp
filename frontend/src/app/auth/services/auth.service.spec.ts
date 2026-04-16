import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { authInterceptor } from '../interceptors/auth.interceptor';
import { Role } from '../models/sign-up.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideZonelessChangeDetection(),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        AuthService,
      ],
    });

    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('should restore the current user from the backend session cookie', () => {
    const restoreRequest = httpMock.expectOne('/backend-auth/me');
    expect(restoreRequest.request.withCredentials).toBeTrue();
    restoreRequest.flush(buildUser());

    expect(service.getCurrentUser()?.email).toBe('sara@example.com');
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('should store the authenticated user after a successful login', () => {
    flushInitialSessionLookupWithUnauthorized();

    service.login({ email: 'sara@example.com', password: 'Password1!' }).subscribe((response) => {
      expect(response.user.role).toBe(Role.ADMIN);
    });

    const loginRequest = httpMock.expectOne('/backend-auth/login');
    expect(loginRequest.request.method).toBe('POST');
    expect(loginRequest.request.withCredentials).toBeTrue();
    loginRequest.flush({ user: buildUser(), sessionId: 'session-123' });

    expect(service.getCurrentUser()?.role).toBe(Role.ADMIN);
  });

  it('should keep the user logged out after a failed login', () => {
    flushInitialSessionLookupWithUnauthorized();

    service.login({ email: 'sara@example.com', password: 'wrong-password' }).subscribe({
      next: fail,
      error: (error) => expect(error.status).toBe(401),
    });

    const loginRequest = httpMock.expectOne('/backend-auth/login');
    loginRequest.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.getCurrentUser()).toBeNull();
  });

  it('should call logout and clear the cached user', () => {
    const restoreRequest = httpMock.expectOne('/backend-auth/me');
    restoreRequest.flush(buildUser());

    service.logout();

    const logoutRequest = httpMock.expectOne('/backend-auth/logout');
    expect(logoutRequest.request.method).toBe('POST');
    expect(logoutRequest.request.withCredentials).toBeTrue();
    logoutRequest.flush({});

    expect(service.getCurrentUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });

  function flushInitialSessionLookupWithUnauthorized(): void {
    const restoreRequest = httpMock.expectOne('/backend-auth/me');
    restoreRequest.flush({}, { status: 401, statusText: 'Unauthorized' });
  }

  function buildUser() {
    return {
      id: 'user-1',
      nom: 'Sara',
      prenom: 'Ben Ali',
      email: 'sara@example.com',
      role: Role.ADMIN,
      numTel: '12345678',
      adresse: 'Tunis',
    };
  }
});
