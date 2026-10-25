import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ErrorApi } from '../models/error-api.model';
import { ProblemaResponse } from '../models/problema.model';
import { CABECERA_REQUEST_ID } from './correlacion.interceptor';
import { erroresHttpInterceptor } from './errores-http.interceptor';

const URL = 'https://api.prueba/api/transferencias';

describe('erroresHttpInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([erroresHttpInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controlador.verify();
  });

  function capturarError(): () => unknown {
    let capturado: unknown;
    http.post(URL, {}).subscribe({
      error: (error: unknown) => {
        capturado = error;
      },
    });
    return () => capturado;
  }

  it('convierte un error RFC 7807 con detalles en ErrorApi', () => {
    const error = capturarError();
    const problema: ProblemaResponse = {
      type: 'about:blank',
      title: 'Unprocessable Entity',
      status: 422,
      detail: 'La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 1000.00',
      codigo: 'LIMITE_DIARIO_EXCEDIDO',
      timestamp: '2026-10-24T15:30:00Z',
      detalles: { limiteDiario: 10000, montoDisponible: 1000 },
    };

    controlador.expectOne(URL).flush(problema, {
      status: 422,
      statusText: 'Unprocessable Entity',
      headers: { [CABECERA_REQUEST_ID]: 'id-servidor' },
    });

    expect(error()).toBeInstanceOf(ErrorApi);
    expect(error()).toMatchObject({
      status: 422,
      codigo: 'LIMITE_DIARIO_EXCEDIDO',
      message: problema.detail,
      errores: [],
      detalles: { limiteDiario: 10000, montoDisponible: 1000 },
      requestId: 'id-servidor',
    });
  });

  it('conserva los errores por campo de una validación', () => {
    const error = capturarError();
    const errores = [{ campo: 'monto', mensaje: 'El monto debe ser mayor a cero' }];

    controlador.expectOne(URL).flush(
      {
        type: 'about:blank',
        title: 'Bad Request',
        status: 400,
        detail: 'La solicitud contiene datos inválidos',
        codigo: 'SOLICITUD_INVALIDA',
        timestamp: '2026-10-24T15:30:00Z',
        errores,
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(error()).toMatchObject({ codigo: 'SOLICITUD_INVALIDA', errores, detalles: {} });
  });

  it('usa el X-Request-Id de la solicitud si la respuesta no lo trae', () => {
    let capturado: unknown;
    http.get(URL, { headers: { [CABECERA_REQUEST_ID]: 'id-cliente' } }).subscribe({
      error: (error: unknown) => {
        capturado = error;
      },
    });

    controlador.expectOne(URL).error(new ProgressEvent('error'));

    expect(capturado).toMatchObject({ requestId: 'id-cliente' });
  });

  it('informa sin conexión cuando el servidor no responde', () => {
    const error = capturarError();

    controlador.expectOne(URL).error(new ProgressEvent('error'));

    expect(error()).toMatchObject({ status: 0, codigo: 'SIN_CONEXION', requestId: null });
    expect((error() as ErrorApi).message).toContain('No se pudo conectar');
  });

  it('informa una respuesta inesperada cuando el cuerpo no es RFC 7807', () => {
    const error = capturarError();

    controlador
      .expectOne(URL)
      .flush('<html>Bad Gateway</html>', { status: 502, statusText: 'Bad Gateway' });

    expect(error()).toMatchObject({
      status: 502,
      codigo: 'RESPUESTA_INESPERADA',
      message: 'Ocurrió un error inesperado (HTTP 502).',
    });
  });
});
