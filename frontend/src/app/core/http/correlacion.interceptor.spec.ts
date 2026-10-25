import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../config/api.config';
import { CABECERA_REQUEST_ID, correlacionInterceptor } from './correlacion.interceptor';

const URL_API = 'https://api.prueba/api';

describe('correlacionInterceptor', () => {
  let http: HttpClient;
  let controlador: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([correlacionInterceptor])),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: URL_API },
      ],
    });
    http = TestBed.inject(HttpClient);
    controlador = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controlador.verify();
  });

  it('agrega un X-Request-Id con formato UUID a las solicitudes de la API', () => {
    http.get(`${URL_API}/cuentas`).subscribe();

    const solicitud = controlador.expectOne(`${URL_API}/cuentas`);
    expect(solicitud.request.headers.get(CABECERA_REQUEST_ID)).toMatch(
      /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/,
    );
    solicitud.flush({});
  });

  it('genera un identificador distinto por solicitud', () => {
    http.get(`${URL_API}/cuentas`).subscribe();
    http.get(`${URL_API}/cuentas`).subscribe();

    const solicitudes = controlador.match(`${URL_API}/cuentas`);
    const identificadores = solicitudes.map((solicitud) =>
      solicitud.request.headers.get(CABECERA_REQUEST_ID),
    );
    expect(new Set(identificadores).size).toBe(2);
    solicitudes.forEach((solicitud) => {
      solicitud.flush({});
    });
  });

  it('respeta un X-Request-Id que ya viene en la solicitud', () => {
    http.get(`${URL_API}/cuentas`, { headers: { [CABECERA_REQUEST_ID]: 'id-propio' } }).subscribe();

    const solicitud = controlador.expectOne(`${URL_API}/cuentas`);
    expect(solicitud.request.headers.get(CABECERA_REQUEST_ID)).toBe('id-propio');
    solicitud.flush({});
  });

  it('no agrega la cabecera a solicitudes fuera de la API', () => {
    http.get('https://otro.dominio/recurso').subscribe();

    const solicitud = controlador.expectOne('https://otro.dominio/recurso');
    expect(solicitud.request.headers.has(CABECERA_REQUEST_ID)).toBe(false);
    solicitud.flush({});
  });
});
