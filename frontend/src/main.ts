// ─── Polyfill pour sockjs-client ─────────────────────────────
// sockjs-client utilise `global` (Node.js) → on le map sur window
(window as any).global = window;

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
