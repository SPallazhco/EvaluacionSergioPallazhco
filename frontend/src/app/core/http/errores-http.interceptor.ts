import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { ErrorApi } from '../models/error-api.model';
import { ProblemaResponse } from '../models/problema.model';
import { CABECERA_REQUEST_ID } from './correlacion.interceptor';

const MENSAJE_SIN_CONEXION =
  'No se pudo conectar con el servidor. Verifique que la API esté disponible.';

export const erroresHttpInterceptor: HttpInterceptorFn = (solicitud, siguiente) =>
  siguiente(solicitud).pipe(
    catchError((error: unknown) =>
      throwError(() => (error instanceof HttpErrorResponse ? aErrorApi(error, solicitud) : error)),
    ),
  );

function aErrorApi(respuesta: HttpErrorResponse, solicitud: HttpRequest<unknown>): ErrorApi {
  const requestId =
    respuesta.headers.get(CABECERA_REQUEST_ID) ?? solicitud.headers.get(CABECERA_REQUEST_ID);
  const cuerpo: unknown = respuesta.error;

  if (esProblema(cuerpo)) {
    return new ErrorApi({
      status: respuesta.status,
      codigo: cuerpo.codigo,
      mensaje: cuerpo.detail,
      errores: cuerpo.errores ?? [],
      detalles: cuerpo.detalles ?? {},
      requestId,
    });
  }

  const sinConexion = respuesta.status === 0;
  return new ErrorApi({
    status: respuesta.status,
    codigo: sinConexion ? 'SIN_CONEXION' : 'RESPUESTA_INESPERADA',
    mensaje: sinConexion
      ? MENSAJE_SIN_CONEXION
      : `Ocurrió un error inesperado (HTTP ${String(respuesta.status)}).`,
    errores: [],
    detalles: {},
    requestId,
  });
}

function esProblema(cuerpo: unknown): cuerpo is ProblemaResponse {
  return (
    typeof cuerpo === 'object' &&
    cuerpo !== null &&
    'codigo' in cuerpo &&
    typeof cuerpo.codigo === 'string' &&
    'detail' in cuerpo &&
    typeof cuerpo.detail === 'string'
  );
}
