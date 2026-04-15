import { Component, OnInit, OnDestroy, PLATFORM_ID, Inject, ChangeDetectorRef, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../shared/services/chat.service';
import { WebSocketService } from '../../shared/services/websocket.service';
import { MessageResponse, Contact, MockUser } from '../../shared/models/message.model';

@Component({
  selector: 'app-messaging',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './messaging.component.html',
  styleUrl: './messaging.component.css'
})
export class MessagingComponent implements OnInit, OnDestroy, AfterViewChecked {
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  activeTab = 'chat';
  currentUserId = 'user1';
  currentUser: MockUser | null = null;
  selectedContact: Contact | null = null;
  newMessage = '';
  loading = false;
  error: string | null = null;
  contactTyping: { [key: string]: boolean } = {};
  private typingTimeout: any;
  private isBrowser: boolean;
  private destroyed = false;
  private shouldScrollToBottom = false;

  availableUsers: MockUser[] = [];
  contacts: Contact[] = [];
  messages: MessageResponse[] = [];

  posts = [
    {
      author: 'Nutrition Specialist',
      title: 'Healthy Eating Tips',
      content: 'Focus on omega-3 rich foods...',
      date: '2 hours ago'
    },
    {
      author: 'Community Manager',
      title: 'New Yoga Session',
      content: 'Join us tomorrow at 10 AM...',
      date: '5 hours ago'
    }
  ];

  constructor(
    private chatService: ChatService,
    private webSocketService: WebSocketService,
    private cdr: ChangeDetectorRef,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit() {
    this.loadMockUsers();
  }

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  loadMockUsers() {
    this.chatService.getMockUsers().subscribe({
      next: (users) => {
        this.availableUsers = users;
        this.currentUser = users.find((u) => u.id === this.currentUserId) || users[0];
        this.currentUserId = this.currentUser.id;
        this.updateContactsList();
        // Activer WebSocket
        this.initializeWebSocket();
      },
      error: (err) => {
        console.error('Error loading mock users:', err);
        this.error = 'Erreur lors du chargement des utilisateurs';
      }
    });
  }

  initializeWebSocket() {
    console.log('🔌 Tentative de connexion WebSocket pour:', this.currentUserId);
    
    this.webSocketService.connect(this.currentUserId).then(() => {
      console.log('✅ WebSocket initialisé avec succès');
      this.error = null; // Effacer l'erreur si elle existait

      // Écouter les nouveaux messages
      this.webSocketService.getMessages().subscribe((message) => {
        this.handleIncomingMessage(message);
      });

      // Écouter les notifications de frappe
      this.webSocketService.getTypingNotifications().subscribe((notification) => {
        this.contactTyping[notification.senderId] = notification.typing;
        this.cdr.detectChanges();
        if (notification.typing) {
          setTimeout(() => {
            this.contactTyping[notification.senderId] = false;
            this.cdr.detectChanges();
          }, 3000);
        }
      });
    }).catch((error) => {
      console.error('❌ Erreur connexion WebSocket:', error);
      console.error('Détails:', error);
      
      // Message d'erreur plus informatif
      if (error?.message?.includes('NetworkError') || error?.message?.includes('Failed to fetch')) {
        this.error = 'Impossible de se connecter au serveur. Vérifiez que les services backend sont démarrés.';
      } else {
        this.error = 'Connexion temps réel non disponible. Le chat fonctionne en mode HTTP.';
      }
      
      // Le chat continue de fonctionner en HTTP même sans WebSocket
      console.log('ℹ️ Le chat fonctionne en mode HTTP (sans temps réel)');
    });
  }

  handleIncomingMessage(message: MessageResponse) {
    // Vérifier si le message appartient à la conversation actuelle
    if (this.selectedContact) {
      const isRelevant =
        (message.senderId === this.currentUserId && message.receiverId === this.selectedContact.id) ||
        (message.senderId === this.selectedContact.id && message.receiverId === this.currentUserId);

      if (isRelevant) {
        // Vérifier si le message n'existe pas déjà
        const exists = this.messages.some((m) => m.id === message.id);
        if (!exists) {
          this.messages.push(message);
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      }
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

  updateContactsList() {
    this.contacts = this.availableUsers
      .filter((user) => user.id !== this.currentUserId)
      .map((user) => ({
        id: user.id,
        name: user.name,
        status: 'online' as const,
        avatar: user.avatar
      }));

    if (this.contacts.length > 0 && !this.selectedContact) {
      this.selectContact(this.contacts[0]);
    } else if (this.selectedContact) {
      const updatedContact = this.contacts.find((c) => c.id === this.selectedContact!.id);
      if (updatedContact) {
        this.selectContact(updatedContact);
      } else if (this.contacts.length > 0) {
        this.selectContact(this.contacts[0]);
      }
    }
  }

  switchUser(userId: string) {
    // Déconnecter l'ancien WebSocket
    this.webSocketService.disconnect();

    this.currentUserId = userId;
    this.currentUser = this.availableUsers.find((u) => u.id === userId) || null;
    this.updateContactsList();
    
    // Reconnecter avec le nouvel utilisateur
    this.initializeWebSocket();
  }

  selectContact(contact: Contact) {
    this.selectedContact = contact;
    this.loadConversation();
  }

  loadConversation() {
    if (!this.selectedContact) return;

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
      }
    });
  }

  sendMessage() {
    if (!this.newMessage.trim() || !this.selectedContact) return;

    const content = this.newMessage;
    this.newMessage = '';

    const messageRequest = {
      senderId: this.currentUserId,
      receiverId: this.selectedContact.id,
      content: content
    };

    // Envoyer via HTTP (le backend enverra aussi via WebSocket)
    this.chatService.sendMessage(messageRequest).subscribe({
      next: (response) => {
        console.log('✅ Message envoyé:', response);
        
        // Ajouter le message immédiatement (optimistic update)
        // Si le WebSocket fonctionne, on vérifie qu'il n'existe pas déjà
        const exists = this.messages.some((m) => m.id === response.id);
        if (!exists) {
          this.messages.push(response);
          this.shouldScrollToBottom = true;
          this.cdr.detectChanges();
        }
      },
      error: (err) => {
        console.error('Error sending message:', err);
        this.error = "Erreur lors de l'envoi du message";
        this.newMessage = content;
      }
    });
  }

  onMessageInput() {
    if (!this.selectedContact) return;

    // Envoyer notification de frappe
    this.webSocketService.sendTypingNotification(
      this.currentUserId,
      this.selectedContact.id,
      true
    );

    // Arrêter la notification après 1 seconde d'inactivité
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
    }

    this.typingTimeout = setTimeout(() => {
      if (this.selectedContact) {
        this.webSocketService.sendTypingNotification(
          this.currentUserId,
          this.selectedContact.id,
          false
        );
      }
    }, 1000);
  }

  isContactTyping(): boolean {
    if (!this.selectedContact) return false;
    return this.contactTyping[this.selectedContact.id] || false;
  }

  ngOnDestroy() {
    this.destroyed = true;
    
    if (this.typingTimeout) {
      clearTimeout(this.typingTimeout);
    }
    
    // Déconnecter WebSocket
    this.webSocketService.disconnect();
  }

  deleteMessage(messageId: string) {
    if (!confirm('Voulez-vous vraiment supprimer ce message?')) return;

    this.chatService.deleteMessage(messageId).subscribe({
      next: () => {
        this.messages = this.messages.filter((m) => m.id !== messageId);
      },
      error: (err) => {
        console.error('Error deleting message:', err);
        this.error = 'Erreur lors de la suppression du message';
      }
    });
  }

  formatTime(timestamp: string): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  isMyMessage(message: MessageResponse): boolean {
    return message.senderId === this.currentUserId;
  }
}
