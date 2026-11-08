import { Component, OnInit, OnDestroy, PLATFORM_ID, Inject, ChangeDetectorRef, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ChatService } from '../../shared/services/chat.service';
import { WebSocketService } from '../../shared/services/websocket.service';
import { MessageResponse, Contact } from '../../shared/models/message.model';
import { AuthService } from '../../auth/services/auth.service';

/**
 * Messagerie entre utilisateurs réels : l'expéditeur est l'utilisateur connecté,
 * les contacts viennent de user-service. Les nouveaux messages arrivent par
 * WebSocket ; tant que celui-ci n'est pas connecté, la conversation est
 * rechargée périodiquement.
 */
@Component({
  selector: 'app-messaging',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './messaging.component.html',
  styleUrl: './messaging.component.css'
})
export class MessagingComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;

  private static readonly POLL_INTERVAL_MS = 4000;

  activeTab = 'chat';
  currentUserId: string | null = null;
  currentUserName = '';
  selectedContact: Contact | null = null;
  newMessage = '';
  loading = false;
  loadingContacts = false;
  error: string | null = null;
  contactTyping: { [key: string]: boolean } = {};
  private typingTimeout: any;
  private pollTimer: any;
  private isBrowser: boolean;
  private shouldScrollToBottom = false;
  private subscriptions: Subscription[] = [];

  contacts: Contact[] = [];
  messages: MessageResponse[] = [];

  constructor(
    private chatService: ChatService,
    private webSocketService: WebSocketService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit() {
    if (!this.isBrowser) return;

    const user = this.authService.getCurrentUser();
    this.currentUserId = this.authService.getCurrentUserId();
    this.currentUserName = user ? `${user.prenom} ${user.nom}`.trim() : '';
    if (!this.currentUserId) {
      this.error = 'Connectez-vous pour accéder à la messagerie.';
      return;
    }

    this.loadContacts();
    this.initializeWebSocket();
    this.pollTimer = setInterval(() => {
      if (!this.webSocketService.isConnected()) this.refreshConversation();
    }, MessagingComponent.POLL_INTERVAL_MS);
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  loadContacts() {
    this.loadingContacts = true;
    this.chatService.getContacts(this.currentUserId).subscribe({
      next: (contacts) => {
        this.contacts = contacts;
        this.loadingContacts = false;
        if (contacts.length > 0 && !this.selectedContact) {
          this.selectContact(contacts[0]);
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading contacts:', err);
        this.loadingContacts = false;
        this.error = 'Erreur lors du chargement des contacts';
        this.cdr.detectChanges();
      }
    });
  }

  initializeWebSocket() {
    const userId = this.currentUserId!;
    this.webSocketService.connect(userId).then(() => {
      this.subscriptions.push(
        this.webSocketService.getMessages().subscribe((message) => this.handleIncomingMessage(message)),
        this.webSocketService.getTypingNotifications().subscribe((notification) => {
          this.contactTyping[notification.senderId] = notification.typing;
          this.cdr.detectChanges();
          if (notification.typing) {
            setTimeout(() => {
              this.contactTyping[notification.senderId] = false;
              this.cdr.detectChanges();
            }, 3000);
          }
        })
      );
    }).catch((error) => {
      // Le chat reste utilisable : la conversation est rechargée périodiquement.
      console.warn('WebSocket du chat indisponible, rechargement périodique activé:', error);
    });
  }

  handleIncomingMessage(message: MessageResponse) {
    if (!this.selectedContact || !this.currentUserId) return;
    const isRelevant =
      (message.senderId === this.currentUserId && message.receiverId === this.selectedContact.id) ||
      (message.senderId === this.selectedContact.id && message.receiverId === this.currentUserId);
    if (isRelevant && !this.messages.some((m) => m.id === message.id)) {
      this.messages.push(message);
      this.shouldScrollToBottom = true;
      this.cdr.detectChanges();
    }
  }

  scrollToBottom() {
    if (!this.isBrowser || !this.messagesContainer) return;
    try {
      const element = this.messagesContainer.nativeElement;
      element.scrollTop = element.scrollHeight;
    } catch (err) {
      console.error('Erreur lors du scroll:', err);
    }
  }

  selectContact(contact: Contact) {
    this.selectedContact = contact;
    this.loadConversation();
  }

  loadConversation() {
    if (!this.selectedContact || !this.currentUserId) return;

    this.loading = true;
    this.error = null;

    this.chatService.getConversation(this.currentUserId, this.selectedContact.id).subscribe({
      next: (messages) => {
        this.messages = messages;
        this.loading = false;
        this.shouldScrollToBottom = true;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error loading conversation:', err);
        this.error = 'Erreur lors du chargement des messages';
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  /** Rechargement silencieux (sans WebSocket) : ne remplace la liste que si elle a changé. */
  private refreshConversation() {
    if (!this.selectedContact || !this.currentUserId || this.loading) return;
    const contactId = this.selectedContact.id;
    this.chatService.getConversation(this.currentUserId, contactId).subscribe({
      next: (messages) => {
        if (this.selectedContact?.id !== contactId) return;
        const last = (list: MessageResponse[]) => list[list.length - 1]?.id;
        if (messages.length !== this.messages.length || last(messages) !== last(this.messages)) {
          this.messages = messages;
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      },
      error: () => {
        // Nouvel essai au prochain intervalle.
      }
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.selectedContact || !this.currentUserId) return;

    const content = this.newMessage;
    this.newMessage = '';

    const messageRequest = {
      senderId: this.currentUserId,
      receiverId: this.selectedContact.id,
      content: content
    };

    // Envoi HTTP ; le backend relaie aussi le message par WebSocket.
    this.chatService.sendMessage(messageRequest).subscribe({
      next: (response) => {
        if (!this.messages.some((m) => m.id === response.id)) {
          this.messages.push(response);
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error sending message:', err);
        this.error = "Erreur lors de l'envoi du message";
        this.newMessage = content;
        this.cdr.detectChanges();
      }
    });
  }

  onMessageInput() {
    if (!this.selectedContact || !this.currentUserId) return;

    this.webSocketService.sendTypingNotification(this.currentUserId, this.selectedContact.id, true);

    // Arrêter la notification après 1 seconde d'inactivité
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
    }
    this.typingTimeout = setTimeout(() => {
      if (this.selectedContact && this.currentUserId) {
        this.webSocketService.sendTypingNotification(this.currentUserId, this.selectedContact.id, false);
      }
    }, 1000);
  }

  isContactTyping(): boolean {
    if (!this.selectedContact) return false;
    return this.contactTyping[this.selectedContact.id] || false;
  }

  ngOnDestroy() {
    if (this.typingTimeout) clearTimeout(this.typingTimeout);
    if (this.pollTimer) clearInterval(this.pollTimer);
    this.subscriptions.forEach((s) => s.unsubscribe());
    this.webSocketService.disconnect();
  }

  deleteMessage(messageId: string) {
    if (!confirm('Voulez-vous vraiment supprimer ce message?')) return;

    this.chatService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter((m) => m.id !== messageId);
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Error deleting message:', err);
        this.error = 'Erreur lors de la suppression du message';
        this.cdr.detectChanges();
      }
    });
  }

  formatTime(timestamp: string): string {
    // Le service renvoie l'heure UTC sans fuseau ("2026-11-08T09:02:53") : la lire comme UTC.
    const hasZone = /(Z|[+-]\d{2}:?\d{2})$/.test(timestamp);
    const date = new Date(hasZone ? timestamp : `${timestamp}Z`);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  isMyMessage(message: MessageResponse): boolean {
    return message.senderId === this.currentUserId;
  }
}
