import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import {
    JoinStatus,
    ParticipantRole,
    SessionParticipantDTO,
    VideoSessionService
} from './video-session.service';
import { AuthService } from '../../auth/services/auth.service';
import { Role } from '../../auth/models/sign-up.model';

interface ChatMessage {
    senderId: string;
    senderLabel: string;
    text: string;
    time: string;
    isSelf: boolean;
}

interface MeetingMember {
    userId: string;
    displayName: string;
    role: ParticipantRole;
    joinStatus: JoinStatus;
    isOnline: boolean;
}

type SidebarResizeAction = 'decrease' | 'increase';

@Component({
    selector: 'app-meeting-sidebar',
    standalone: true,
    imports: [CommonModule, FormsModule],
    template: `
    <div class="sidebar-header">
      <div class="title-group">
        <h3>{{ getTitle() }}</h3>
        <p>{{ getSubtitle() }}</p>
      </div>
      <div class="header-actions">
        <button
          class="resize-btn"
          (click)="resizeSidebar.emit('decrease')"
          [disabled]="!canDecreaseSize"
          aria-label="Reduire la largeur">
          <i class="fa fa-angle-left"></i>
        </button>
        <button
          class="resize-btn"
          (click)="resizeSidebar.emit('increase')"
          [disabled]="!canIncreaseSize"
          aria-label="Agrandir la largeur">
          <i class="fa fa-angle-right"></i>
        </button>
        <button class="close-btn" (click)="close.emit()" aria-label="Fermer">
          <i class="fa fa-times"></i>
        </button>
      </div>
    </div>

    <div class="tab-bar" role="tablist" aria-label="Sections">
      <button type="button" class="tab-btn" [class.active]="activeTab === 'chat'" (click)="switchTab('chat')">
        <i class="fa fa-comment-alt"></i>
        <span>Chat</span>
      </button>
      <button type="button" class="tab-btn" [class.active]="activeTab === 'participants'" (click)="switchTab('participants')">
        <i class="fa fa-users"></i>
        <span>Participants</span>
      </button>
      <button type="button" class="tab-btn" [class.active]="activeTab === 'agenda'" (click)="switchTab('agenda')">
        <i class="fa fa-list"></i>
        <span>Agenda</span>
      </button>
    </div>

    <div class="sidebar-content" [ngSwitch]="activeTab">
      <div *ngSwitchCase="'chat'" class="chat-container">
        <div class="messages-list" #messagesList>
          <div class="empty-state" *ngIf="messages.length === 0">
            <i class="fa fa-comments"></i>
            <p>Aucun message pour le moment.</p>
          </div>

          <div class="message" *ngFor="let msg of messages" [class.self]="msg.isSelf">
            <div class="msg-header">
              <span class="sender">{{ msg.senderLabel }}</span>
              <span class="time">{{ msg.time }}</span>
            </div>
            <div class="msg-body">{{ msg.text }}</div>
          </div>
        </div>

        <div class="chat-input-area">
          <input
            type="text"
            [(ngModel)]="newMessage"
            (keyup.enter)="sendMessage()"
            [disabled]="!roomId"
            placeholder="Tapez un message..." />
          <button class="send-btn" (click)="sendMessage()" [disabled]="!roomId" aria-label="Envoyer">
            <i class="fa fa-paper-plane"></i>
          </button>
        </div>
      </div>

      <div *ngSwitchCase="'participants'" class="participants-pane">
        <ul class="participants-list" *ngIf="members.length > 0; else emptyParticipants">
          <li *ngFor="let member of members">
            <div class="member-main">
              <span class="member-name">
                {{ member.displayName }}{{ member.userId === currentUser ? ' (You)' : '' }}
              </span>
              <span class="member-role">{{ getParticipantRoleLabel(member.role) }}</span>
            </div>
            <div class="member-meta">
              <span class="member-join">{{ getJoinStatusLabel(member.joinStatus) }}</span>
              <span class="status" [class.online]="member.isOnline">
                {{ member.isOnline ? 'Online' : 'Offline' }}
              </span>
            </div>
          </li>
        </ul>

        <ng-template #emptyParticipants>
          <div class="empty-state participants-empty">
            <i class="fa fa-user-friends"></i>
            <p>Aucun participant charge.</p>
          </div>
        </ng-template>

        <div class="add-participant-box">
          <label for="newParticipantId">Ajouter un participant</label>
          <div class="add-row">
            <input
              id="newParticipantId"
              type="text"
              [(ngModel)]="newParticipantId"
              placeholder="userId du participant" />
            <button
              class="add-btn"
              (click)="addParticipant()"
              [disabled]="!canAddParticipant()">
              {{ isAddingParticipant ? '...' : 'Ajouter' }}
            </button>
          </div>
          <p class="participant-feedback" *ngIf="participantActionMessage">
            {{ participantActionMessage }}
          </p>
        </div>
      </div>

      <div *ngSwitchCase="'agenda'" class="agenda-container">
        <ul class="agenda-list">
          <li class="completed"><i class="fa fa-check-circle"></i> Introduction (5m)</li>
          <li class="active"><i class="fa fa-circle"></i> Cognitive Exercises (15m)</li>
          <li><i class="fa fa-circle-o"></i> Music Therapy (20m)</li>
          <li><i class="fa fa-circle-o"></i> Q&A (10m)</li>
        </ul>
      </div>
    </div>
  `,
    styles: [`
    :host {
      display: flex;
      flex-direction: column;
      height: 100%;
      min-height: 0;
      background: #f8fafc;
    }

    .sidebar-header {
      padding: 14px 16px 10px;
      border-bottom: 1px solid #e6ebf5;
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 10px;
      background: #ffffff;
    }

    .title-group {
      min-width: 0;
    }

    .sidebar-header h3 {
      margin: 0;
      font-size: 1.05rem;
      text-transform: capitalize;
      color: #25324d;
    }

    .title-group p {
      margin: 3px 0 0;
      font-size: 0.76rem;
      color: #64748b;
    }

    .close-btn {
      background: #f2f5fb;
      border: 1px solid #dce3f0;
      width: 30px;
      height: 30px;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.9rem;
      color: #475569;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .close-btn:hover {
      background: #e8eef8;
    }

    .header-actions {
      display: inline-flex;
      align-items: center;
      gap: 6px;
      flex-shrink: 0;
    }

    .resize-btn {
      background: #f8fbff;
      border: 1px solid #dce3f0;
      width: 30px;
      height: 30px;
      border-radius: 8px;
      cursor: pointer;
      font-size: 0.9rem;
      color: #475569;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0;
    }

    .resize-btn:hover:not(:disabled) {
      background: #edf3ff;
    }

    .resize-btn:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }

    .tab-bar {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 6px;
      padding: 8px 10px;
      border-bottom: 1px solid #e7edf8;
      background: #ffffff;
    }

    .tab-btn {
      border: 1px solid #e4eaf6;
      background: #f8faff;
      color: #475569;
      border-radius: 8px;
      padding: 7px 8px;
      font-size: 0.78rem;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      min-height: 34px;
    }

    .tab-btn.active {
      border-color: #c7d7ff;
      background: #eaf1ff;
      color: #1e3a8a;
    }

    .sidebar-content {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }

    .chat-container {
      flex: 1;
      min-height: 0;
      display: flex;
      flex-direction: column;
    }

    .messages-list {
      flex: 1;
      min-height: 0;
      padding: 12px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 10px;
      background: linear-gradient(180deg, #f8faff 0%, #f3f6fc 100%);
    }

    .empty-state {
      margin: auto;
      text-align: center;
      color: #64748b;
      font-size: 0.85rem;
      display: flex;
      flex-direction: column;
      gap: 8px;
      align-items: center;
      justify-content: center;
    }

    .empty-state i {
      font-size: 1.35rem;
      color: #94a3b8;
    }

    .message {
      background: #ffffff;
      padding: 9px 11px;
      border-radius: 10px;
      max-width: 92%;
      align-self: flex-start;
      border: 1px solid #e3e9f5;
      box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
    }

    .message.self {
      background: #eaf2ff;
      border-color: #cddfff;
      align-self: flex-end;
    }

    .msg-header {
      display: flex;
      justify-content: space-between;
      font-size: 0.72rem;
      color: #64748b;
      margin-bottom: 4px;
      gap: 8px;
    }

    .sender {
      font-weight: 700;
      color: #334155;
    }

    .msg-body {
      color: #1f2937;
      word-break: break-word;
      line-height: 1.35;
      font-size: 0.9rem;
    }

    .chat-input-area {
      padding: 10px;
      border-top: 1px solid #e6ebf5;
      display: flex;
      gap: 8px;
      background: #ffffff;
      box-shadow: 0 -4px 10px rgba(15, 23, 42, 0.04);
    }

    .chat-input-area input {
      flex: 1;
      min-width: 0;
      padding: 9px 12px;
      border: 1px solid #d6ddeb;
      border-radius: 20px;
      outline: none;
      background: #f9fbff;
      color: #1f2937;
    }

    .chat-input-area input:disabled {
      background: #eef2f7;
      color: #94a3b8;
      cursor: not-allowed;
    }

    .send-btn {
      width: 36px;
      height: 36px;
      border: none;
      border-radius: 999px;
      background: #2563eb;
      color: #ffffff;
      cursor: pointer;
      font-size: 0.9rem;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .send-btn:disabled {
      background: #94a3b8;
      cursor: not-allowed;
    }

    .participants-pane {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      padding: 12px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      background: #f8fafc;
    }

    .participants-list {
      list-style: none;
      padding: 0;
      margin: 0;
      border: 1px solid #e5ebf7;
      border-radius: 10px;
      overflow: hidden;
    }

    .participants-list li {
      padding: 10px 12px;
      border-bottom: 1px solid #edf0f8;
      display: flex;
      flex-direction: column;
      gap: 6px;
      background: #ffffff;
    }

    .participants-list li:last-child {
      border-bottom: none;
    }

    .participants-empty {
      border: 1px dashed #d5deed;
      border-radius: 10px;
      padding: 18px 10px;
      background: #ffffff;
    }

    .member-main {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
    }

    .member-name {
      font-weight: 600;
      color: #273049;
      word-break: break-word;
    }

    .member-role {
      font-size: 0.72rem;
      color: #475569;
      padding: 2px 8px;
      border-radius: 999px;
      background: #eef2ff;
    }

    .member-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;
      font-size: 0.78rem;
      color: #64748b;
    }

    .status {
      padding: 2px 8px;
      border-radius: 999px;
      background: #f1f5f9;
      color: #64748b;
    }

    .status.online {
      background: #dcfce7;
      color: #166534;
    }

    .add-participant-box {
      border: 1px solid #e4ebf7;
      border-radius: 10px;
      padding: 10px;
      background: #ffffff;
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .add-participant-box label {
      font-size: 0.84rem;
      font-weight: 600;
      color: #334155;
    }

    .add-row {
      display: grid;
      grid-template-columns: 1fr auto auto;
      gap: 8px;
      align-items: center;
    }

    .add-row input,
    .add-row select {
      border: 1px solid #d8deea;
      border-radius: 8px;
      padding: 7px 10px;
      background: #ffffff;
      min-width: 0;
    }

    .add-btn {
      border: none;
      border-radius: 8px;
      padding: 8px 12px;
      background: #4f46e5;
      color: #ffffff;
      cursor: pointer;
      font-weight: 600;
    }

    .add-btn:disabled {
      opacity: 0.55;
      cursor: not-allowed;
    }

    .participant-feedback {
      margin: 0;
      font-size: 0.78rem;
      color: #475569;
      word-break: break-word;
    }

    .agenda-container {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
      background: #f8fafc;
    }

    .agenda-list {
      list-style: none;
      padding: 12px;
      margin: 0;
    }

    .agenda-list li {
      padding: 10px 0;
      border-bottom: 1px solid #e6ebf5;
      display: flex;
      align-items: center;
      gap: 10px;
      color: #475569;
    }

    .agenda-list li.completed {
      color: #15803d;
      text-decoration: line-through;
    }

    .agenda-list li.active {
      color: #1d4ed8;
      font-weight: 700;
    }

    @media (max-width: 640px) {
      .resize-btn {
        display: none;
      }

      .tab-btn span {
        display: none;
      }

      .tab-btn {
        padding: 7px 6px;
      }

      .participants-pane {
        padding: 10px;
      }

      .add-row {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class MeetingSidebarComponent implements OnInit, OnChanges, OnDestroy {
    @Input() activeTab: string = 'chat';
    @Input() roomId: string = '';
    @Input() currentUser: string = 'patient';
    @Input() sessionId: number = 0;
    @Input() canDecreaseSize = true;
    @Input() canIncreaseSize = true;
    @Output() close = new EventEmitter<void>();
    @Output() tabChange = new EventEmitter<string>();
    @Output() resizeSidebar = new EventEmitter<SidebarResizeAction>();
    @ViewChild('messagesList') private messagesListRef?: ElementRef<HTMLDivElement>;

    messages: ChatMessage[] = [];
    members: MeetingMember[] = [];

    newMessage = '';
    newParticipantId = '';
    isAddingParticipant = false;
    participantActionMessage = '';

    private onlineUsers = new Set<string>();
    private subscriptions: Subscription[] = [];
    private loadedRoomId = '';
    private loadedSessionId = 0;
    private readonly userLabelCache = new Map<string, string>();
    private readonly labelLoading = new Set<string>();

    constructor(
        private readonly videoSessionService: VideoSessionService,
        private readonly authService: AuthService,
        private readonly cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        const current = this.authService.getCurrentUser();
        if (current) {
            this.userLabelCache.set(current.id, `${current.prenom} ${current.nom}`.trim());
        }

        this.subscriptions.push(
            this.videoSessionService.signal$.subscribe(signal => {
                if (signal.type === 'chat') {
                    this.consumeRealtimeChat(signal.payload, signal.fromUserId);
                } else if (signal.type === 'participant-added') {
                    this.consumeParticipantAdded(signal.payload);
                }
            })
        );

        this.subscriptions.push(
            this.videoSessionService.participants$.subscribe(ids => {
                this.onlineUsers = new Set(ids);
                this.ensureMembersFromOnlineUsers();
                this.syncOnlineFlags();
            })
        );

        this.loadChatHistory();
        this.loadSessionMembers();
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['roomId'] && this.roomId) {
            this.loadChatHistory();
        }
        if (changes['sessionId'] && this.sessionId) {
            this.loadSessionMembers();
        }
        if (changes['activeTab'] && this.activeTab === 'chat') {
            this.scrollMessagesToBottom();
        }
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
        this.subscriptions = [];
    }

    getTitle(): string {
        return this.activeTab.charAt(0).toUpperCase() + this.activeTab.slice(1);
    }

    getSubtitle(): string {
        if (this.activeTab === 'chat') {
            return this.roomId ? 'Discussion en direct' : 'Room indisponible';
        }
        if (this.activeTab === 'participants') {
            return `${this.members.length} participant(s)`;
        }
        return 'Programme de la session';
    }

    switchTab(tab: string): void {
        if (this.activeTab === tab) {
            return;
        }
        this.activeTab = tab;
        this.tabChange.emit(tab);
        if (tab === 'chat') {
            this.scrollMessagesToBottom();
        }
    }

    sendMessage(): void {
        const text = this.newMessage.trim();
        if (!text || !this.roomId) return;

        this.messages.push({
            senderId: this.currentUser,
            senderLabel: 'You',
            text,
            time: this.formatTime(new Date().toISOString()),
            isSelf: true
        });
        this.videoSessionService.sendChat(this.roomId, this.currentUser, text);
        this.newMessage = '';
        this.scrollMessagesToBottom();
    }

    canAddParticipant(): boolean {
        return !!this.roomId
            && !!this.newParticipantId.trim()
            && !this.isAddingParticipant;
    }

    addParticipant(): void {
        const userId = this.newParticipantId.trim();
        if (!userId || !this.roomId) return;

        this.isAddingParticipant = true;
        this.participantActionMessage = '';

        this.authService.getUserById(userId).subscribe({
            next: user => {
                const roomRole = this.mapUserRoleToParticipantRole(user.role);
                this.videoSessionService.addParticipantToRoom(
                    this.roomId,
                    this.currentUser,
                    userId,
                    roomRole,
                    'CONFIRMED'
                ).subscribe({
                    next: participants => {
                        this.hydrateMembers(participants);
                        this.newParticipantId = '';
                        this.participantActionMessage = `Participant ajoute: ${user.prenom} ${user.nom} (${this.getParticipantRoleLabel(roomRole)}).`;
                        this.isAddingParticipant = false;
                    },
                    error: err => {
                        this.participantActionMessage = err?.error?.message ?? 'Ajout impossible.';
                        this.isAddingParticipant = false;
                    }
                });
            },
            error: () => {
                this.participantActionMessage = "Utilisateur introuvable. Verifiez le userId saisi.";
                this.isAddingParticipant = false;
            }
        });
    }

    private loadChatHistory(): void {
        if (!this.roomId || !this.currentUser) return;
        if (this.loadedRoomId === this.roomId) return;
        this.loadedRoomId = this.roomId;
        this.videoSessionService.getRoomMessages(this.roomId).subscribe({
            next: history => {
                this.messages = history.map(msg => ({
                    senderId: msg.fromUserId,
                    senderLabel: msg.fromUserId === this.currentUser ? 'You' : this.getDisplayName(msg.fromUserId),
                    text: msg.text,
                    time: this.formatTime(msg.sentAt),
                    isSelf: msg.fromUserId === this.currentUser
                }));
                this.scrollMessagesToBottom();
            },
            error: err => {
                console.warn('Chat history not loaded', err);
            }
        });
    }

    private loadSessionMembers(): void {
        if (!this.sessionId) return;
        if (this.loadedSessionId === this.sessionId) return;
        this.loadedSessionId = this.sessionId;
        this.videoSessionService.getSessionParticipants(this.sessionId).subscribe({
            next: participants => this.hydrateMembers(participants),
            error: err => {
                console.warn('Participants not loaded', err);
                this.ensureMembersFromOnlineUsers();
            }
        });
    }

    private hydrateMembers(participants: SessionParticipantDTO[]): void {
        this.members = participants.map(p => ({
            userId: p.userId,
            displayName: this.getDisplayName(p.userId),
            role: p.role,
            joinStatus: p.joinStatus,
            isOnline: this.onlineUsers.has(p.userId)
        }));
        this.ensureMembersFromOnlineUsers();
    }

    private consumeRealtimeChat(payload: string, senderUserId: string): void {
        let parsed: { text?: string; time?: string; sentAt?: string } = {};
        try {
            parsed = JSON.parse(payload || '{}');
        } catch {
            parsed = {};
        }
        const text = (parsed.text ?? '').trim();
        if (!text) return;

        this.messages.push({
            senderId: senderUserId,
            senderLabel: senderUserId === this.currentUser ? 'You' : this.getDisplayName(senderUserId),
            text,
            time: this.formatTime(parsed.sentAt ?? parsed.time ?? new Date().toISOString()),
            isSelf: senderUserId === this.currentUser
        });
        this.scrollMessagesToBottom();
    }

    private consumeParticipantAdded(payload: string): void {
        let parsed: { userId?: string; role?: string; joinStatus?: string } = {};
        try {
            parsed = JSON.parse(payload || '{}');
        } catch {
            parsed = {};
        }
        const userId = (parsed.userId ?? '').trim();
        if (!userId) return;

        const existing = this.members.find(m => m.userId === userId);
        const role = (parsed.role as ParticipantRole) || 'PARTICIPANT';
        const joinStatus = (parsed.joinStatus as JoinStatus) || 'CONFIRMED';

        if (existing) {
            existing.role = role;
            existing.joinStatus = joinStatus;
            existing.isOnline = this.onlineUsers.has(existing.userId);
            existing.displayName = this.getDisplayName(existing.userId);
        } else {
            this.members.push({
                userId,
                displayName: this.getDisplayName(userId),
                role,
                joinStatus,
                isOnline: this.onlineUsers.has(userId)
            });
        }
    }

    private syncOnlineFlags(): void {
        this.members = this.members.map(member => ({
            ...member,
            displayName: this.getDisplayName(member.userId),
            isOnline: this.onlineUsers.has(member.userId)
        }));
    }

    private ensureMembersFromOnlineUsers(): void {
        for (const userId of this.onlineUsers) {
            if (this.members.some(m => m.userId === userId)) {
                continue;
            }
            this.members.push({
                userId,
                displayName: this.getDisplayName(userId),
                role: 'PARTICIPANT',
                joinStatus: 'ATTENDED',
                isOnline: true
            });
        }
    }

    private getDisplayName(userId: string): string {
        const cached = this.userLabelCache.get(userId);
        if (cached) {
            return cached;
        }
        this.resolveUserLabel(userId);
        return this.shortenUserId(userId);
    }

    private resolveUserLabel(userId: string): void {
        if (!userId || this.labelLoading.has(userId) || this.userLabelCache.has(userId)) {
            return;
        }
        this.labelLoading.add(userId);
        this.authService.getUserById(userId).subscribe({
            next: user => {
                this.userLabelCache.set(userId, this.formatUserDisplayName(user.prenom, user.nom, userId));
                this.labelLoading.delete(userId);
                this.syncOnlineFlags();
                this.messages = this.messages.map(m => ({
                    ...m,
                    senderLabel: m.isSelf ? 'You' : this.getDisplayName(m.senderId)
                }));
                this.cdr.markForCheck();
            },
            error: () => {
                this.userLabelCache.set(userId, this.shortenUserId(userId));
                this.labelLoading.delete(userId);
                this.cdr.markForCheck();
            }
        });
    }

    private shortenUserId(userId: string): string {
        if (!userId) return 'Utilisateur';
        return userId.length <= 12 ? userId : `${userId.slice(0, 5)}...${userId.slice(-4)}`;
    }

    private formatUserDisplayName(prenom?: string, nom?: string, fallbackUserId?: string): string {
        const first = (prenom ?? '').trim();
        const last = (nom ?? '').trim();
        if (first && last) {
            if (first.toLowerCase() === last.toLowerCase()) {
                return first;
            }
            return `${first} ${last}`;
        }
        if (first) return first;
        if (last) return last;
        return this.shortenUserId(fallbackUserId ?? '');
    }

    getParticipantRoleLabel(role: ParticipantRole): string {
        if (role === 'HOST') return 'Hote';
        if (role === 'ORGANIZER') return 'Organisateur';
        return 'Participant';
    }

    getJoinStatusLabel(status: JoinStatus): string {
        if (status === 'CONFIRMED') return 'Confirme';
        if (status === 'ATTENDED') return 'Present';
        return 'Invite';
    }

    private mapUserRoleToParticipantRole(role: Role): ParticipantRole {
        if (role === Role.DOCTOR_PROFILE || role === Role.ADMIN) {
            return 'ORGANIZER';
        }
        return 'PARTICIPANT';
    }

    private formatTime(isoOrTime: string): string {
        const asDate = new Date(isoOrTime);
        if (!Number.isNaN(asDate.getTime())) {
            return asDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        }
        return isoOrTime;
    }

    private scrollMessagesToBottom(): void {
        setTimeout(() => {
            const panel = this.messagesListRef?.nativeElement;
            if (!panel) {
                return;
            }
            panel.scrollTop = panel.scrollHeight;
        }, 0);
    }
}
