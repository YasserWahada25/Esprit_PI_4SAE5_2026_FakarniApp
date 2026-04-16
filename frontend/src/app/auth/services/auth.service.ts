import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SignUpRequest } from '../models/sign-up.model';
import { AuthResponse, User, UserUpdateRequest } from '../models/user.model';

// Use relative URLs in frontend. Angular dev-server proxy forwards to Gateway (8090).
const API = '/api';
const AUTH = '/backend-auth';

const STORAGE_KEY = 'fakarni_user';
const TOKEN_KEY = 'fakarni_token';
const REFRESH_TOKEN_KEY = 'fakarni_refresh';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface ResetPasswordRequest {
  token: string;
  newPassword: string;
}

export interface FacebookLoginRequest {
  accessToken: string;
}

export interface MessageResponse {
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(this.loadStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    console.log('✅ AuthService loaded');
    console.log('✅ AUTH =', AUTH);
  }

  private hasSessionStorage(): boolean {
    return typeof window !== 'undefined' && typeof window.sessionStorage !== 'undefined';
  }

  private getSessionItem(key: string): string | null {
    if (!this.hasSessionStorage()) return null;
    return window.sessionStorage.getItem(key);
  }

  private setSessionItem(key: string, value: string): void {
    if (!this.hasSessionStorage()) return;
    window.sessionStorage.setItem(key, value);
  }

  private removeSessionItem(key: string): void {
    if (!this.hasSessionStorage()) return;
    window.sessionStorage.removeItem(key);
  }

  private loadStoredUser(): User | null {
    try {
      const raw = this.getSessionItem(STORAGE_KEY);
      const user = raw ? JSON.parse(raw) : null;
      console.log('loadStoredUser() =>', user);
      return user;
    } catch (error) {
      console.error('loadStoredUser() error:', error);
      return null;
    }
  }

  private storeUser(user: User | null): void {
    console.log('storeUser() called with:', user);

    if (user) {
      this.setSessionItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      this.removeSessionItem(STORAGE_KEY);
    }

    this.currentUserSubject.next(user);
  }

  private storeAuthResponse(res: AuthResponse): void {
    console.log('storeAuthResponse() called with:', res);

    if (res?.accessToken) {
      this.setSessionItem(TOKEN_KEY, res.accessToken);
      console.log('✅ accessToken stored');
    }

    if (res?.refreshToken) {
      this.setSessionItem(REFRESH_TOKEN_KEY, res.refreshToken);
      console.log('✅ refreshToken stored');
    }

    if (res?.user) {
      this.storeUser(res.user);
    }
  }

  // ============================================================
  // AUTH CLASSIQUE
  // ============================================================

  register(body: SignUpRequest): Observable<User> {
    console.log('register() URL =', `${API}/users`);
    console.log('register() body =', body);

    return this.http.post<User>(`${API}/users`, body);
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    console.log('login() URL =', `${AUTH}/login`);
    console.log('login() body =', body);

    return this.http.post<AuthResponse>(`${AUTH}/login`, body).pipe(
      tap((res) => {
        console.log('login() response =', res);
        this.storeAuthResponse(res);
      })
    );
  }

  logout(): void {
    const refresh = this.getRefreshToken();
    console.log('logout() refresh token =', refresh);

    if (refresh) {
      console.log('logout() URL =', `${AUTH}/logout`);

      this.http.post(
        `${AUTH}/logout`,
        { refreshToken: refresh },
        { responseType: 'text' }
      ).subscribe({
        next: (res) => console.log('logout() success =', res),
        error: (err) => console.error('logout() error =', err)
      });
    }

    this.removeSessionItem(TOKEN_KEY);
    this.removeSessionItem(REFRESH_TOKEN_KEY);
    this.storeUser(null);
  }

  resetPassword(token: string, newPassword: string): Observable<MessageResponse> {
    console.log('resetPassword() URL =', `${AUTH}/reset-password`);
    console.log('resetPassword() body =', { token, newPassword });

    return this.http.post<MessageResponse>(
      `${AUTH}/reset-password`,
      { token, newPassword }
    );
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    console.log('forgotPassword() URL =', `${AUTH}/forgot-password`);
    console.log('forgotPassword() body =', { email });

    return this.http.post<MessageResponse>(`${AUTH}/forgot-password`, { email });
  }

  // ============================================================
  // GOOGLE LOGIN — POST /auth/google { credential } (Google ID token JWT)
  // ============================================================

  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    const url = `${AUTH}/google`;

    console.log('✅ loginWithGoogle() called');
    console.log('✅ loginWithGoogle() URL =', url);
    console.log('✅ loginWithGoogle() body =', { credential: '…' });
    console.log('✅ loginWithGoogle() token exists =', !!idToken);

    return this.http.post<AuthResponse>(
      url,
      { credential: idToken }
    ).pipe(
      tap({
        next: (res) => {
          console.log('✅ loginWithGoogle() response =', res);
          this.storeAuthResponse(res);
        },
        error: (err) => {
          console.error('❌ loginWithGoogle() error status =', err?.status);
          console.error('❌ loginWithGoogle() error body =', err?.error);
          console.error('❌ loginWithGoogle() full error =', err);
        }
      })
    );
  }

  loginWithFacebook(accessToken: string): Observable<AuthResponse> {
    const url = `${AUTH}/facebook`;
    const body: FacebookLoginRequest = { accessToken };

    console.log('loginWithFacebook() URL =', url);
    console.log('loginWithFacebook() token exists =', !!accessToken);

    return this.http.post<AuthResponse>(url, body).pipe(
      tap((res) => {
        console.log('loginWithFacebook() response =', res);
        this.storeAuthResponse(res);
      })
    );
  }

  // ============================================================
  // HELPERS
  // ============================================================

  getCurrentUser(): User | null {
    let user = this.currentUserSubject.value;
    console.log('getCurrentUser() subject value =', user);

    if (!user) {
      user = this.loadStoredUser();
      if (user) {
        this.currentUserSubject.next(user);
      }
    }

    console.log('getCurrentUser() return =', user);
    return user;
  }

  getAccessToken(): string | null {
    const token = this.getSessionItem(TOKEN_KEY);
    console.log('getAccessToken() =', token);
    return token;
  }

  getRefreshToken(): string | null {
    const refresh = this.getSessionItem(REFRESH_TOKEN_KEY);
    console.log('getRefreshToken() =', refresh);
    return refresh;
  }

  isLoggedIn(): boolean {
    const loggedIn = this.currentUserSubject.value !== null
      || this.getSessionItem(STORAGE_KEY) !== null;

    console.log('isLoggedIn() =', loggedIn);
    return loggedIn;
  }

  getUserById(id: string): Observable<User> {
    console.log('getUserById() URL =', `${API}/users/${id}`);
    console.log('getUserById() id =', id);

    return this.http.get<User>(`${API}/users/${id}`);
  }

  updateUser(id: string, body: UserUpdateRequest): Observable<User> {
    console.log('updateUser() URL =', `${API}/users/${id}`);
    console.log('updateUser() body =', body);

    return this.http.put<User>(`${API}/users/${id}`, body).pipe(
      tap((updated) => {
        console.log('updateUser() response =', updated);

        const current = this.getCurrentUser();
        if (current?.id === id) {
          this.storeUser(updated);
        }
      })
    );
  }
}