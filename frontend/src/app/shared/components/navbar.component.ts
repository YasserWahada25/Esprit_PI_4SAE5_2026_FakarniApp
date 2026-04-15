import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../auth/services/auth.service';
import { Role } from '../../auth/models/sign-up.model';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [RouterModule, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  isMobileMenuOpen = false;
  activeDropdown: string | null = null;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  get isAdmin(): boolean {
    return this.authService.getCurrentUser()?.role === Role.ADMIN;
  }

  toggleMobileMenu(): void {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }

  toggleDropdown(menuName: string): void {
    if (window.innerWidth <= 992) {
      this.activeDropdown = this.activeDropdown === menuName ? null : menuName;
    }
  }

  onMouseEnter(menuName: string): void {
    if (window.innerWidth > 992) {
      this.activeDropdown = menuName;
    }
  }

  onMouseLeave(): void {
    if (window.innerWidth > 992) {
      this.activeDropdown = null;
    }
  }

  closeMenu(): void {
    this.isMobileMenuOpen = false;
    this.activeDropdown = null;
  }

  onLogout(): void {
    this.authService.logout();
    this.closeMenu();
    this.router.navigate(['/auth/signin']);
  }
}
