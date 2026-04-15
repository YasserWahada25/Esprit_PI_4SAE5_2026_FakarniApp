import { RenderMode, ServerRoute } from '@angular/ssr';


export const serverRoutes: ServerRoute[] =
[
  // Keep authenticated app sections client-rendered to avoid 404 on deep-link reloads.
  { path: 'home', renderMode: RenderMode.Client },
  { path: 'profile/**', renderMode: RenderMode.Client },
  { path: 'alzheimer_meeting/**', renderMode: RenderMode.Client },
  { path: 'communication/**', renderMode: RenderMode.Client },
  { path: 'geofencing/**', renderMode: RenderMode.Client },
  { path: 'educational/**', renderMode: RenderMode.Client },
  { path: 'medical/**', renderMode: RenderMode.Client },
  { path: 'admin', renderMode: RenderMode.Client },
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Client }
];
