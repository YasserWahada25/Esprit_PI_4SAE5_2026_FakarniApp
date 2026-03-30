import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Routes nécessitant sessionStorage/localStorage → rendu côté client uniquement
  { path: 'alzheimer_meeting/meeting/:id', renderMode: RenderMode.Client },
  { path: 'profile/edit', renderMode: RenderMode.Client },
  { path: 'home', renderMode: RenderMode.Client },
  { path: 'auth/signin', renderMode: RenderMode.Client },
  { path: 'auth/signup', renderMode: RenderMode.Client },
  { path: 'auth/forgot-password', renderMode: RenderMode.Client },
  { path: 'auth/reset-password', renderMode: RenderMode.Client },

  // Fallback
  { path: '**', renderMode: RenderMode.Prerender }
];
