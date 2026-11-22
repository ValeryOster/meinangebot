import {ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection} from '@angular/core';
import {provideRouter, withHashLocation} from '@angular/router';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {routes} from './app.routes';
import {authInterceptor} from './service/security/helper/auth.interceptor';
import {MAT_TABS_CONFIG} from '@angular/material/tabs';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({eventCoalescing: true}),
    // Hash-Routing wie bisher (/#/login), damit der Server keine SPA-Weiterleitung braucht
    provideRouter(routes, withHashLocation()),
    provideHttpClient(withInterceptors([authInterceptor])),
    // Tabs wie in Angular Material 9: nicht über die ganze Breite gestreckt
    {provide: MAT_TABS_CONFIG, useValue: {stretchTabs: false, alignTabs: 'start'}}
  ]
};
