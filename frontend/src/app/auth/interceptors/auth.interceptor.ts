import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const isBackendRequest = req.url.startsWith('/api') || req.url.startsWith('/backend-auth');

    if (isBackendRequest) {
        return next(req.clone({ withCredentials: true }));
    }

    return next(req);
};
