import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { API_URL } from '../config/api.config';

export const CABECERA_REQUEST_ID = 'X-Request-Id';

export const correlacionInterceptor: HttpInterceptorFn = (solicitud, siguiente) => {
  const esSolicitudApi = solicitud.url.startsWith(inject(API_URL));
  if (!esSolicitudApi || solicitud.headers.has(CABECERA_REQUEST_ID)) {
    return siguiente(solicitud);
  }
  return siguiente(solicitud.clone({ setHeaders: { [CABECERA_REQUEST_ID]: crypto.randomUUID() } }));
};
