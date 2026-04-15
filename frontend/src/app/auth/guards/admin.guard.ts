import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/sign-up.model';

export const adminGuard: CanActivateFn = (): boolean | UrlTree => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const user = auth.getCurrentUser();

  if (!auth.isLoggedIn()) {
    return router.createUrlTree(['/auth/signin']);
  }

  if (user && user.role === Role.ADMIN) {
    return true;
  }

  return router.createUrlTree(['/home']);
};
