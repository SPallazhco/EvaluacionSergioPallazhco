import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { correlacionInterceptor } from './core/http/correlacion.interceptor';
import { erroresHttpInterceptor } from './core/http/errores-http.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([correlacionInterceptor, erroresHttpInterceptor])),
  ],
};
