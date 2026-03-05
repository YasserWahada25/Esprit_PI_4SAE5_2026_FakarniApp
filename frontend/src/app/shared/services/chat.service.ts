import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MessageRequest, MessageResponse, MockUser } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = '/api/messages';
  private mockUsersUrl = '/api/mock-users';

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

  getMockUsers(): Observable<MockUser[]> {
    return this.http.get<MockUser[]>(this.mockUsersUrl);
  }

  getMockUserById(userId: string): Observable<MockUser> {
    return this.http.get<MockUser>(`${this.mockUsersUrl}/${userId}`);
  }
}
