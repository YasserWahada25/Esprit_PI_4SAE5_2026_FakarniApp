import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EducationalEvent } from '../models/educational-event.model';

@Injectable({
    providedIn: 'root'
})
export class EducationalEventService {
    private apiUrl = `${environment.apiBaseUrl}/api/events`;

    // Flux partagé des événements pour que plusieurs composants puissent se synchroniser
    private readonly eventsSubject = new BehaviorSubject<EducationalEvent[]>([]);
    readonly events$ = this.eventsSubject.asObservable();

    constructor(private http: HttpClient) { }

    /**
     * Recharge la liste complète des événements depuis l'API
     * et met à jour le flux partagé.
     */
    reloadEvents(): Observable<EducationalEvent[]> {
        console.log('[EventService] Appel API :', this.apiUrl);
        return this.http.get<EducationalEvent[]>(this.apiUrl).pipe(
            tap(events => {
                console.log('[EventService] Réponse reçue :', events);
                this.eventsSubject.next(events);
            })
        );
    }

    /** Récupération simple (sans mettre à jour le cache partagé) si besoin ponctuel */
    getEvents(): Observable<EducationalEvent[]> {
        return this.http.get<EducationalEvent[]>(this.apiUrl);
    }

    getEventById(id: number): Observable<EducationalEvent> {
        return this.http.get<EducationalEvent>(`${this.apiUrl}/${id}`);
    }

    createEvent(event: Omit<EducationalEvent, 'id'>): Observable<EducationalEvent> {
        return this.http.post<EducationalEvent>(this.apiUrl, event).pipe(
            tap(created => {
                const current = this.eventsSubject.value;
                this.eventsSubject.next([...current, created]);
            })
        );
    }

    updateEvent(id: number, event: Partial<EducationalEvent>): Observable<EducationalEvent> {
        return this.http.put<EducationalEvent>(`${this.apiUrl}/${id}`, event).pipe(
            tap(updated => {
                const current = this.eventsSubject.value;
                const next = current.map(e => e.id === updated.id ? updated : e);
                this.eventsSubject.next(next);
            })
        );
    }

    deleteEvent(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`).pipe(
            tap(() => {
                const current = this.eventsSubject.value;
                this.eventsSubject.next(current.filter(e => e.id !== id));
            })
        );
    }
}
