import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();

  console.log('🔐 Auth Interceptor:', {
    url: req.url,
    hasToken: !!token,
    token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN'
  });

  if (!token) {
    console.warn('⚠️ No token found - request will be sent without Authorization header');
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
