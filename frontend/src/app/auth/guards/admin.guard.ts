import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/sign-up.model';

export const adminGuard: CanMatchFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const user = authService.getCurrentUser();

  if (user?.role === Role.ADMIN) {
    return true;
  }

  return router.parseUrl('/auth/signin');
};
