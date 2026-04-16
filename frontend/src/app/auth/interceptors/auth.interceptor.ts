import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Ne pas envoyer de Bearer sur les routes publiques : un JWT expiré/invalide encore présent
 * dans sessionStorage ferait rejeter la requête (401) avant les règles permitAll côté API.
 */
function shouldSendAuthHeader(req: { url: string; method: string }): boolean {
  const path = req.url.split('?')[0].toLowerCase();
  const m = req.method.toUpperCase();

  if (m === 'POST' && path.endsWith('/api/users')) {
    return false;
  }

  if (
    m === 'POST' &&
    /\/auth\/(login|google|facebook|forgot-password|reset-password)\/?$/i.test(path)
  ) {
    return false;
  }

  return true;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();

<<<<<<< HEAD
  if (!token || !shouldSendAuthHeader(req)) {
=======
  console.log('🔐 Auth Interceptor:', {
    url: req.url,
    hasToken: !!token,
    token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN'
  });

  if (!token) {
    console.warn('⚠️ No token found - request will be sent without Authorization header');
>>>>>>> 34a87b605fecf5b2caf4446596de1916397f7f44
    return next(req);
  }

  const clonedReq = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });

  console.log('✅ Token added to request:', clonedReq.headers.get('Authorization')?.substring(0, 30) + '...');

  return next(clonedReq);
};
