import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin, map } from 'rxjs';
import { Contact, MessageRequest, MessageResponse } from '../models/message.model';
import { User } from '../../auth/models/user.model';
import { Role, ROLE_OPTIONS } from '../../auth/models/sign-up.model';

/** Rôles proposés comme contacts (l'administrateur n'est pas un interlocuteur du chat). */
const CONTACT_ROLES: Role[] = [Role.DOCTOR_PROFILE, Role.CARE_OWNER, Role.PATIENT_PROFILE];

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = '/api/messages';
  private usersUrl = '/api/users/by-role';

  constructor(private http: HttpClient) {}

  sendMessage(message: MessageRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.apiUrl}/send`, message);
  }

  getConversation(user1: string, user2: string): Observable<MessageResponse[]> {
    const params = new HttpParams()
      .set('user1', user1)
      .set('user2', user2);
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/conversation`, { params });
  }

  getAllMessages(): Observable<MessageResponse[]> {
    return this.http.get<MessageResponse[]>(`${this.apiUrl}/all`);
  }

  updateMessage(messageId: string, newContent: string): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${this.apiUrl}/${messageId}`, newContent, {
      headers: { 'Content-Type': 'text/plain' }
    });
  }

  deleteMessage(messageId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${messageId}`);
  }

  deleteConversation(user1: string, user2: string): Observable<void> {
    const params = new HttpParams()
      .set('user1', user1)
      .set('user2', user2);
    return this.http.delete<void>(`${this.apiUrl}/conversation`, { params });
  }

  /**
   * Utilisateurs réels de la plateforme, triés par nom, sans l'utilisateur connecté.
   * `GET /api/users` est réservé aux administrateurs : la liste est construite rôle par rôle.
   */
  getContacts(excludeUserId?: string | null): Observable<Contact[]> {
    return forkJoin(
      CONTACT_ROLES.map((role) => this.http.get<User[]>(`${this.usersUrl}/${role}`))
    ).pipe(
      map((lists) =>
        lists
          .flat()
          .filter((user) => user.id !== excludeUserId)
          .map(toContact)
          .sort((a, b) => a.name.localeCompare(b.name, 'fr'))
      )
    );
  }
}

function toContact(user: User): Contact {
  const name = [user.prenom, user.nom].filter((part) => !!part?.trim()).join(' ') || user.email;
  const initials = name
    .split(' ')
    .filter((part) => part.length > 0)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('');
  const role = ROLE_OPTIONS.find((option) => option.value === user.role)?.label ?? '';
  return { id: user.id, name, role, initials };
}
