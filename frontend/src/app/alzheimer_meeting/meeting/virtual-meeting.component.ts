import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { VideoGridComponent } from './video-grid.component';
import { MeetingControlsComponent } from './meeting-controls.component';
import { MeetingSidebarComponent } from './meeting-sidebar.component';
import { AlzheimerService } from '../shared/alzheimer.service';
import { VideoSessionService } from './video-session.service';
import { WebrtcPeerService } from './webrtc-peer.service';
import { AuthService } from '../../auth/services/auth.service';
import { Subscription } from 'rxjs';

@Component({
    selector: 'app-virtual-meeting',
    standalone: true,
    imports: [CommonModule, RouterModule, VideoGridComponent, MeetingControlsComponent, MeetingSidebarComponent],
    templateUrl: './virtual-meeting.component.html',
    styleUrl: './virtual-meeting.component.css'
})
export class VirtualMeetingComponent implements OnInit, OnDestroy {
    sessionId: number = 0;
    sessionTitle: string = 'Virtual Meeting';
    isSidebarOpen: boolean = true;
    activeSidebarTab: string = 'chat'; // chat, participants, agenda, docs
    sidebarWidth = 360;

    // Variables WebRTC
    roomId: string = '';
    currentUser: string = '';
    isMuted = false;
    isVideoOff = false;
    participantCount = 0;

    private subscriptions: Subscription[] = [];
    private readonly sidebarMinWidth = 300;
    private readonly sidebarMaxWidth = 560;
    private readonly sidebarStep = 40;
    private readonly sidebarStorageKey = 'alzheimer_meeting_sidebar_width';

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private alzheimerService: AlzheimerService,
        private videoSessionService: VideoSessionService,
        private webrtcService: WebrtcPeerService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.currentUser = this.resolveCurrentUserId();
        this.sidebarWidth = this.restoreSidebarWidth();

        this.subscriptions.push(this.route.params.subscribe(params => {
            this.sessionId = +params['id'];
            this.loadSessionDetails();
        }));

        this.subscriptions.push(this.route.queryParams.subscribe(params => {
            this.roomId = params['roomId'];
            if (this.roomId) {
                // Se connecter au broker STOMP pour la signalisation
                this.videoSessionService.connectAndSubscribe(this.roomId, this.currentUser);
            } else {
                console.error('Aucun roomId fourni ! URL doit contenir ?roomId=UUID');
                this.router.navigate(['/alzheimer_meeting/consultation']);
            }
        }));

        this.subscriptions.push(this.videoSessionService.participants$.subscribe(ids => {
            this.participantCount = ids.length;
        }));
    }

    loadSessionDetails(): void {
        // In a real app, fetch from API. ensuring we have a title.
        this.sessionTitle = `Session #${this.sessionId}`;
        this.alzheimerService.getSessions().subscribe(sessions => {
            const session = sessions.find(s => s.id === this.sessionId);
            if (session) {
                this.sessionTitle = session.title;
            }
        });
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(sub => sub.unsubscribe());
        this.subscriptions = [];
        if (this.roomId) {
            this.videoSessionService.disconnect(this.roomId, this.currentUser);
        }
    }

    private resolveCurrentUserId(): string {
        return this.authService.getCurrentUser()?.id?.trim() || 'patient';
    }

    toggleSidebar(tab?: string): void {
        if (tab) {
            if (this.activeSidebarTab === tab && this.isSidebarOpen) {
                this.isSidebarOpen = false;
            } else {
                this.activeSidebarTab = tab;
                this.isSidebarOpen = true;
            }
        } else {
            this.isSidebarOpen = !this.isSidebarOpen;
        }
    }

    onLeaveMeeting(): void {
        if (this.roomId) {
            this.videoSessionService.disconnect(this.roomId, this.currentUser);
        }
        console.log('Leaving meeting...');
        this.router.navigate(['/alzheimer_meeting/consultation']);
    }

    onToggleMute(): void {
        this.isMuted = this.webrtcService.toggleLocalAudio();
    }

    onToggleVideo(): void {
        this.isVideoOff = this.webrtcService.toggleLocalVideo();
    }

    async onShareScreen(): Promise<void> {
        try {
            await this.webrtcService.startScreenShare();
        } catch (err) {
            console.error('Partage d\'écran impossible', err);
        }
    }

    canDecreaseSidebar(): boolean {
        return this.sidebarWidth > this.sidebarMinWidth;
    }

    canIncreaseSidebar(): boolean {
        return this.sidebarWidth < this.sidebarMaxWidth;
    }

    onResizeSidebar(action: 'decrease' | 'increase'): void {
        const nextWidth = action === 'increase'
            ? this.sidebarWidth + this.sidebarStep
            : this.sidebarWidth - this.sidebarStep;
        this.sidebarWidth = this.clampSidebarWidth(nextWidth);
        this.persistSidebarWidth(this.sidebarWidth);
    }

    private clampSidebarWidth(width: number): number {
        return Math.max(this.sidebarMinWidth, Math.min(this.sidebarMaxWidth, width));
    }

    private restoreSidebarWidth(): number {
        if (typeof window === 'undefined') {
            return 360;
        }
        const raw = window.localStorage.getItem(this.sidebarStorageKey);
        const value = raw ? Number.parseInt(raw, 10) : Number.NaN;
        if (Number.isNaN(value)) {
            return 360;
        }
        return this.clampSidebarWidth(value);
    }

    private persistSidebarWidth(width: number): void {
        if (typeof window === 'undefined') {
            return;
        }
        window.localStorage.setItem(this.sidebarStorageKey, String(width));
    }
}
