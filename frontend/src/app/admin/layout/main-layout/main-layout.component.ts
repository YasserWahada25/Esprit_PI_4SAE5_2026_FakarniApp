import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
    selector: 'app-main-layout',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        MatSidenavModule,
        MatListModule,
        MatToolbarModule,
        MatIconModule,
        MatButtonModule
    ],
    templateUrl: './main-layout.component.html',
    styleUrls: ['./main-layout.component.scss']
})
export class MainLayoutComponent {
    constructor(
        private readonly authService: AuthService,
        private readonly router: Router
    ) {}

    get adminDisplayName(): string {
        const user = this.authService.getCurrentUser();
        if (!user) return 'Administrateur';
        const fullName = `${user.prenom} ${user.nom}`.trim();
        return fullName || 'Administrateur';
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/auth/signin']);
    }
}
