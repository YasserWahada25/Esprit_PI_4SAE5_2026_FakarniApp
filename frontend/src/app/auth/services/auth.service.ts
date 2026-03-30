import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { SignUpRequest } from '../models/sign-up.model';
import { AuthResponse, User, UserUpdateRequest } from '../models/user.model';

const API_BASE = 'http://localhost:8090';
const API = `${API_BASE}/api`;
const AUTH = `${API_BASE}/auth`;

const STORAGE_KEY = 'fakarni_user';
const TOKEN_KEY = 'fakarni_token';
const REFRESH_TOKEN_KEY = 'fakarni_refresh';

export interface LoginRequest {
  email: string;
  password: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private currentUserSubject = new BehaviorSubject<User | null>(this.loadStoredUser());
  currentUser$ = this.currentUserSubject.asObservable();

  // ✅ Injecter PLATFORM_ID pour détecter browser vs serveur
  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  // ✅ Helper : est-ce qu'on est dans le navigateur ?
  private isBrowser(): boolean {
    return isPlatformBrowser(this.platformId);
  }

  private loadStoredUser(): User | null {
    // ✅ Guard SSR
    if (typeof window === 'undefined') return null;
    try {
      const raw = sessionStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  private storeUser(user: User | null): void {
    if (!this.isBrowser()) return; // ✅ Guard SSR
    if (user) {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(user));
    } else {
      sessionStorage.removeItem(STORAGE_KEY);
    }
    this.currentUserSubject.next(user);
  }

  register(body: SignUpRequest): Observable<User> {
    return this.http.post<User>(`${API}/users`, body);
  }

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${AUTH}/login`, body).pipe(
      tap((res) => {
        if (!this.isBrowser()) return; // ✅ Guard SSR
        if (res?.accessToken) {
          sessionStorage.setItem(TOKEN_KEY, res.accessToken);
        }
        if (res?.refreshToken) {
          sessionStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
        }
        if (res?.user) {
          this.storeUser(res.user);
        }
      })
    );
  }

  logout(): void {
    const refresh = this.getRefreshToken();
    if (refresh) {
      this.http.post(`${AUTH}/logout`, { refreshToken: refresh }, { responseType: 'text' }).subscribe({
        next: () => {},
        error: () => {}
      });
    }
    if (!this.isBrowser()) return; // ✅ Guard SSR
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    this.storeUser(null);
  }

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

  getAccessToken(): string | null {
    if (!this.isBrowser()) return null; // ✅ Guard SSR
    return sessionStorage.getItem(TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    if (!this.isBrowser()) return null; // ✅ Guard SSR
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    if (!this.isBrowser()) return false; // ✅ Guard SSR
    return this.currentUserSubject.value !== null
      || sessionStorage.getItem(STORAGE_KEY) !== null;
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

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${AUTH}/forgot-password`, { email });
  }

  resetPassword(token: string, newPassword: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${AUTH}/reset-password`, { token, newPassword });
  }
}