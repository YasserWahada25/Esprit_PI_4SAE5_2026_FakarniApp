import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Route spécifique dynamique
  {
    path: 'alzheimer_meeting/meeting/:id',
    renderMode: RenderMode.Client
  },

  // Sections authentifiées / dynamiques en client-side
  { path: 'home', renderMode: RenderMode.Client },
  { path: 'profile/**', renderMode: RenderMode.Client },
  { path: 'alzheimer_meeting/**', renderMode: RenderMode.Client },
  { path: 'communication/**', renderMode: RenderMode.Client },
  { path: 'geofencing/**', renderMode: RenderMode.Client },
  { path: 'educational/**', renderMode: RenderMode.Client },
  { path: 'medical/**', renderMode: RenderMode.Client },
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: 'posts/**', renderMode: RenderMode.Client },

  // Fallback général
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
