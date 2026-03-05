import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject, Observable } from 'rxjs';
import { MessageResponse } from '../models/message.model';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private client: Client | null = null;
  private messageSubject = new Subject<MessageResponse>();
  private typingSubject = new Subject<{ senderId: string; typing: boolean }>();
  private connected = false;
  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  connect(userId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      // Ne pas connecter si on est côté serveur
      if (!this.isBrowser) {
        resolve();
        return;
      }

      if (this.connected && this.client) {
        resolve();
        return;
      }

      try {
        console.log('🔌 Connexion WebSocket via proxy');
        
        // Import dynamique de SockJS
        import('sockjs-client').then((SockJSModule) => {
          const SockJS = SockJSModule.default;
          // Utiliser une URL relative pour passer par le proxy Angular
          const socket = new SockJS('/ws');
          
          this.client = new Client({
            webSocketFactory: () => socket,
            debug: (str) => {
              console.log('STOMP: ' + str);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
              console.log('✅ WebSocket connecté pour user:', userId);
              this.connected = true;

              // S'abonner aux messages pour cet utilisateur
              this.client!.subscribe(`/queue/messages/${userId}`, (message: IMessage) => {
                const msg = JSON.parse(message.body) as MessageResponse;
                console.log('📨 Message reçu via WebSocket:', msg);
                this.messageSubject.next(msg);
              });

              // S'abonner aux notifications de frappe
              this.client!.subscribe(`/queue/typing/${userId}`, (message: IMessage) => {
                const notification = JSON.parse(message.body);
                console.log('⌨️ Notification de frappe:', notification);
                this.typingSubject.next({
                  senderId: notification.senderId,
                  typing: notification.typing
                });
              });

              resolve();
            },
            onStompError: (frame) => {
              console.error('❌ Erreur STOMP:', frame);
              console.error('Frame headers:', frame.headers);
              console.error('Frame body:', frame.body);
              this.connected = false;
              const errorMsg = frame.headers?.['message'] || 'Unknown error';
              reject(new Error(`STOMP Error: ${errorMsg}`));
            },
            onWebSocketError: (error) => {
              console.error('❌ Erreur WebSocket:', error);
              this.connected = false;
              reject(new Error('WebSocket connection failed. Is the backend running?'));
            },
            onWebSocketClose: (event) => {
              console.warn('⚠️ WebSocket fermé:', event);
              this.connected = false;
            }
          });

          this.client.activate();
        }).catch((error) => {
          console.error('❌ Erreur lors du chargement de SockJS:', error);
          reject(error);
        });
      } catch (error) {
        console.error('❌ Exception lors de la création du WebSocket:', error);
        reject(error);
      }
    });
  }

  sendMessage(senderId: string, receiverId: string, content: string): void {
    if (!this.isBrowser || !this.client || !this.connected) {
      console.error('WebSocket non connecté');
      return;
    }

    const message = {
      senderId,
      receiverId,
      content
    };

    this.client.publish({
      destination: '/app/chat.send',
      body: JSON.stringify(message)
    });
  }

  sendTypingNotification(senderId: string, receiverId: string, typing: boolean): void {
    if (!this.isBrowser || !this.client || !this.connected) {
      return;
    }

    const notification = {
      senderId,
      receiverId,
      typing
    };

    this.client.publish({
      destination: '/app/chat.typing',
      body: JSON.stringify(notification)
    });
  }

  getMessages(): Observable<MessageResponse> {
    return this.messageSubject.asObservable();
  }

  getTypingNotifications(): Observable<{ senderId: string; typing: boolean }> {
    return this.typingSubject.asObservable();
  }

  disconnect(): void {
    if (this.isBrowser && this.client) {
      this.client.deactivate();
      this.connected = false;
      console.log('🔌 WebSocket déconnecté');
    }
  }

  isConnected(): boolean {
    return this.connected;
  }
}
