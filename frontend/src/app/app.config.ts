import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZonelessChangeDetection, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withFetch } from '@angular/common/http';
<<<<<<< HEAD
import { registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';

registerLocaleData(localeFr);
=======
>>>>>>> 67198709f82c9e9ce60df0115653964eec2a195b

import { routes } from './app.routes';
import { provideClientHydration, withEventReplay } from '@angular/platform-browser';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes),
    provideClientHydration(withEventReplay()),
    provideAnimations(),
<<<<<<< HEAD
    provideHttpClient(withFetch()),
    { provide: LOCALE_ID, useValue: 'fr-FR' }
=======
    provideHttpClient(withFetch())
>>>>>>> 67198709f82c9e9ce60df0115653964eec2a195b
  ]
};
