import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

declare global {
  interface Window {
    __fakarniGsiClientId?: string;
  }
}

/**
 * Initialise Google Identity une seule fois par client_id + page,
 * puis diffuse les JWT via observable (évite GSI_LOGGER "initialize() is called multiple times"
 * et garde des callbacks toujours valides).
 */
@Injectable({ providedIn: 'root' })
export class GoogleSignInService {
  private readonly credentials = new Subject<string>();
  readonly credentials$ = this.credentials.asObservable();

  ensureInitialized(clientId: string): void {
    if (!clientId || typeof window === 'undefined' || !window.google?.accounts?.id) {
      return;
    }
    const w = window;
    if (w.__fakarniGsiClientId === clientId) {
      return;
    }
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: (response) => {
        const credential = response?.credential;
        if (credential) {
          this.credentials.next(credential);
        }
      },
    });
    w.__fakarniGsiClientId = clientId;
  }
}
