import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { Role, SignUpRequest } from '../models/sign-up.model';
import { AuthResponse, User } from '../models/user.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  const mockUser: User = {
    id: 'u1',
    nom: 'Yasser',
    prenom: 'Wahada',
    email: 'yasser@esprit.tn',
    role: Role.PATIENT_PROFILE,
    numTel: '12345678',
    adresse: 'Tunis',
  };

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  it('register should POST /api/users', () => {
    const body: SignUpRequest = {
      nom: 'Yasser',
      prenom: 'Wahada',
      email: 'yasser@esprit.tn',
      password: 'Abcdef1!',
      role: Role.PATIENT_PROFILE,
      numTel: '12345678',
      adresse: 'Tunis',
    };

    service.register(body).subscribe((res) => {
      expect(res.email).toBe('yasser@esprit.tn');
    });

    const req = httpMock.expectOne('http://localhost:8090/api/users');
    expect(req.request.method).toBe('POST');
    req.flush(mockUser);
  });

  it('login success should store tokens and user in sessionStorage', () => {
    const response: AuthResponse = {
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      user: mockUser,
    };

    service.login({ email: 'yasser@esprit.tn', password: 'Abcdef1!' }).subscribe((res) => {
      expect(res.user.id).toBe('u1');
    });

    const req = httpMock.expectOne('http://localhost:8090/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(response);

    expect(service.getAccessToken()).toBe('access-token');
    expect(service.getRefreshToken()).toBe('refresh-token');
    expect(service.getCurrentUser()?.email).toBe('yasser@esprit.tn');
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('login failure should not store session data', () => {
    service.login({ email: 'bad@esprit.tn', password: 'bad' }).subscribe({
      next: () => fail('Expected login to fail'),
      error: (err) => {
        expect(err.status).toBe(401);
      },
    });

    const req = httpMock.expectOne('http://localhost:8090/auth/login');
    req.flush({ message: 'Invalid credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(service.getAccessToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
  });

  it('logout should clear local session and call backend when refresh token exists', () => {
    sessionStorage.setItem('fakarni_refresh', 'refresh-token');
    sessionStorage.setItem('fakarni_token', 'access-token');
    sessionStorage.setItem('fakarni_user', JSON.stringify(mockUser));

    service.logout();

    const req = httpMock.expectOne('http://localhost:8090/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush('ok');

    expect(service.getAccessToken()).toBeNull();
    expect(service.getRefreshToken()).toBeNull();
    expect(service.getCurrentUser()).toBeNull();
    expect(service.isLoggedIn()).toBeFalse();
  });
});
