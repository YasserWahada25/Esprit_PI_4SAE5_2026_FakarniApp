import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { GroupService } from '../services/group.service';
import { Group, GroupMember, MemberRole } from '../models/group.model';
import { timeout, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-group-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './group-details.component.html',
  styleUrl: './group-details.component.css'
})
export class GroupDetailsComponent implements OnInit {
  group: Group | null = null;
  members: GroupMember[] = [];
  loading = false;
  error: string | null = null;
  currentUserId = 1; // TODO: Récupérer depuis le service d'authentification
  groupId: number = 0; // ID du groupe actuel
  
  MemberRole = MemberRole;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private groupService: GroupService
  ) {}

  ngOnInit(): void {
    const groupId = Number(this.route.snapshot.paramMap.get('id'));
    if (groupId) {
      this.groupId = groupId;
      this.loadGroupDetails(groupId);
    }
  }

  loadGroupDetails(groupId: number): void {
    this.loading = true;
    this.error = null;

    console.log('🔍 Chargement du groupe ID:', groupId);
    console.log('📡 URL:', `/api/groups/${groupId}?includeMembers=true`);

    this.groupService.getGroupById(groupId, true).pipe(
      timeout(10000), // Timeout de 10 secondes
      catchError(err => {
        console.error('❌ Erreur complète:', err);
        throw err;
      })
    ).subscribe({
      next: (group) => {
        console.log('✅ Groupe chargé:', group);
        this.group = group;
        this.members = group.members || [];
        this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur de chargement:', err);
        
        if (err.name === 'TimeoutError') {
          this.error = 'Le serveur met trop de temps à répondre. Vérifiez que le backend est démarré.';
        } else if (err.status === 404) {
          this.error = 'Groupe introuvable';
        } else if (err.status === 503) {
          this.error = 'Service non disponible. Vérifiez que le Group Service est démarré sur le port 8097.';
        } else if (err.status === 0) {
          this.error = 'Impossible de contacter le serveur. Vérifiez que la Gateway est démarrée sur le port 8090.';
        } else {
          this.error = `Erreur ${err.status}: ${err.message || 'Erreur lors du chargement du groupe'}`;
        }
        
        this.loading = false;
      }
    });
  }

  isAdmin(): boolean {
    if (!this.group) return false;
    const member = this.members.find(m => m.userId === this.currentUserId);
    return member?.role === MemberRole.ADMIN;
  }

  isModerator(): boolean {
    if (!this.group) return false;
    const member = this.members.find(m => m.userId === this.currentUserId);
    return member?.role === MemberRole.MODERATOR || this.isAdmin();
  }

  isMember(): boolean {
    return this.members.some(m => m.userId === this.currentUserId);
  }

  removeMember(userId: number): void {
    if (!this.group || !confirm('Êtes-vous sûr de vouloir retirer ce membre ?')) {
      return;
    }

    this.groupService.removeMember(this.group.id, userId).subscribe({
      next: () => {
        this.members = this.members.filter(m => m.userId !== userId);
        if (this.group) {
          this.group.memberCount--;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression du membre';
        console.error(err);
      }
    });
  }

  promoteToModerator(userId: number): void {
    if (!this.group) return;

    this.groupService.updateMemberRole(this.group.id, userId, { role: MemberRole.MODERATOR }).subscribe({
      next: (updatedMember) => {
        const index = this.members.findIndex(m => m.userId === userId);
        if (index !== -1) {
          this.members[index] = updatedMember;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la mise à jour du rôle';
        console.error(err);
      }
    });
  }

  promoteToAdmin(userId: number): void {
    if (!this.group) return;

    this.groupService.updateMemberRole(this.group.id, userId, { role: MemberRole.ADMIN }).subscribe({
      next: (updatedMember) => {
        const index = this.members.findIndex(m => m.userId === userId);
        if (index !== -1) {
          this.members[index] = updatedMember;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la mise à jour du rôle';
        console.error(err);
      }
    });
  }

  demoteToMember(userId: number): void {
    if (!this.group) return;

    this.groupService.updateMemberRole(this.group.id, userId, { role: MemberRole.MEMBER }).subscribe({
      next: (updatedMember) => {
        const index = this.members.findIndex(m => m.userId === userId);
        if (index !== -1) {
          this.members[index] = updatedMember;
        }
      },
      error: (err) => {
        this.error = 'Erreur lors de la mise à jour du rôle';
        console.error(err);
      }
    });
  }

  leaveGroup(): void {
    if (!this.group || !confirm('Êtes-vous sûr de vouloir quitter ce groupe ?')) {
      return;
    }

    this.groupService.removeMember(this.group.id, this.currentUserId).subscribe({
      next: () => {
        this.router.navigate(['/communication/groups']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la sortie du groupe';
        console.error(err);
      }
    });
  }

  deleteGroup(): void {
    if (!this.group || !confirm('Êtes-vous sûr de vouloir supprimer ce groupe ? Cette action est irréversible.')) {
      return;
    }

    this.groupService.deleteGroup(this.group.id).subscribe({
      next: () => {
        this.router.navigate(['/communication/groups']);
      },
      error: (err) => {
        this.error = 'Erreur lors de la suppression du groupe';
        console.error(err);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/communication/groups']);
  }

  getRoleBadgeClass(role: MemberRole): string {
    switch (role) {
      case MemberRole.ADMIN:
        return 'badge-admin';
      case MemberRole.MODERATOR:
        return 'badge-moderator';
      default:
        return 'badge-member';
    }
  }

  getRoleIcon(role: MemberRole): string {
    switch (role) {
      case MemberRole.ADMIN:
        return 'fa-crown';
      case MemberRole.MODERATOR:
        return 'fa-shield-halved';
      default:
        return 'fa-user';
    }
  }
}
