import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, of, tap } from 'rxjs';
import { SignUpRequest } from '../models/sign-up.model';
import { AuthResponse, User, UserUpdateRequest } from '../models/user.model';

// Use relative URLs in frontend. Angular dev-server proxy forwards to Gateway (8090).
const API = '/api';
const AUTH = '/backend-auth';

const STORAGE_KEY = 'fakarni_user';

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
    this.restoreSession();
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
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private storeUser(user: User | null): void {
    if (user) {
      this.setSessionItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      this.removeSessionItem(STORAGE_KEY);
    }

    this.currentUserSubject.next(user);
  }

  // ============================================================
  // AUTH CLASSIQUE
  // ============================================================

  register(body: SignUpRequest): Observable<User> {
    return this.http.post<User>(`${API}/users`, body);
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${AUTH}/login`, body).pipe(
      tap((res) => this.storeUser(res.user))
    );
  }

  logout(): void {
    this.http.post<void>(`${AUTH}/logout`, {}).subscribe({
      next: () => undefined,
      error: () => undefined
    });
    this.storeUser(null);
  }

  resetPassword(token: string, newPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(
      `${AUTH}/reset-password`,
      { token, newPassword }
    );
  }

  forgotPassword(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${AUTH}/forgot-password`, { email });
  }

  // ============================================================
  // GOOGLE LOGIN — POST /auth/google { credential } (Google ID token JWT)
  // ============================================================

  loginWithGoogle(idToken: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(
      `${AUTH}/google`,
      { credential: idToken }
    ).pipe(
      tap((res) => this.storeUser(res.user))
    );
  }

  loginWithFacebook(accessToken: string): Observable<AuthResponse> {
    const body: FacebookLoginRequest = { accessToken };

    return this.http.post<AuthResponse>(`${AUTH}/facebook`, body).pipe(
      tap((res) => this.storeUser(res.user))
    );
  }

  // ============================================================
  // HELPERS
  // ============================================================

  getCurrentUser(): User | null {
    let user = this.currentUserSubject.value;

    if (!user) {
      user = this.loadStoredUser();
      if (user) {
        this.currentUserSubject.next(user);
      }
    }

    return user;
  }

  isLoggedIn(): boolean {
    return this.currentUserSubject.value !== null || this.getSessionItem(STORAGE_KEY) !== null;
  }

  getUserById(id: string): Observable<User> {
    return this.http.get<User>(`${API}/users/${id}`);
  }

  updateUser(id: string, body: UserUpdateRequest): Observable<User> {
    return this.http.put<User>(`${API}/users/${id}`, body).pipe(
      tap((updated) => {
        const current = this.getCurrentUser();
        if (current?.id === id) {
          this.storeUser(updated);
        }
      })
    );
  }

  private restoreSession(): void {
    if (!this.hasSessionStorage()) {
      return;
    }

    this.http.get<User>(`${AUTH}/me`).pipe(
      tap((user) => this.storeUser(user)),
      catchError(() => {
        this.storeUser(null);
        return of(null);
      })
    ).subscribe();
  }
}