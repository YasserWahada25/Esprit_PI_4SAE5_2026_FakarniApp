import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: 'alzheimer_meeting/meeting/:id',
    renderMode: RenderMode.Client
  },
  {
    path: 'educational',
    renderMode: RenderMode.Client
  },
  {
    path: 'educational/activities',
    renderMode: RenderMode.Client
  },
  {
    path: 'educational/activities/:id/play',
    renderMode: RenderMode.Client
  },
  {
    path: 'educational/events',
    renderMode: RenderMode.Client
  },
  {
    path: 'educational/tracking',
    renderMode: RenderMode.Client
  },
  {
    path: 'admin/medical-monitoring/patients/:id',
    renderMode: RenderMode.Client
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender
  }
];
