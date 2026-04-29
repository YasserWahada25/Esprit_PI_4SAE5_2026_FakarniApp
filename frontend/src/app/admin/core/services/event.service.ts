import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { EducationalEvent } from '../models/educational-event.model';

/**
 * @deprecated Use EducationalEventService instead.
 * This service is kept to avoid breaking other potential imports,
 * but now delegates to the correct Gateway URL via EducationalEvent model.
 */
@Injectable({
  providedIn: 'root'
})
export class EventService {
  private apiUrl = `${environment.apiBaseUrl}/api/events`;

  constructor(private http: HttpClient) { }

  addEvent(event: EducationalEvent): Observable<EducationalEvent> {
    return this.http.post<EducationalEvent>(this.apiUrl, event);
  }

  getEvents(): Observable<EducationalEvent[]> {
    return this.http.get<EducationalEvent[]>(this.apiUrl);
  }

  updateEvent(id: number, event: EducationalEvent): Observable<EducationalEvent> {
    return this.http.put<EducationalEvent>(`${this.apiUrl}/${id}`, event);
  }

  deleteEvent(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}