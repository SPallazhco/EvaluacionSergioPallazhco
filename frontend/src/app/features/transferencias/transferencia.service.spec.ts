import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../../core/config/api.config';
import { erroresHttpInterceptor } from '../../core/http/errores-http.interceptor';
import { ErrorApi } from '../../core/models/error-api.model';
import { CrearTransferenciaRequest, TransferenciaResponse } from './transferencia.model';
import { TransferenciaService } from './transferencia.service';

const URL_TRANSFERENCIAS = 'https://api.prueba/api/transferencias';

const SOLICITUD: CrearTransferenciaRequest = {
  numeroCuentaOrigen: '1111111111',
  numeroCuentaDestino: '2222222222',
  monto: 250.5,
};

describe('TransferenciaService', () => {
  let servicio: TransferenciaService;
  let controlador: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([erroresHttpInterceptor])),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    servicio = TestBed.inject(TransferenciaService);
    controlador = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controlador.verify();
  });

  it('envía POST /transferencias y devuelve la transferencia creada', () => {
    let respuesta: TransferenciaResponse | undefined;
    servicio.ejecutar(SOLICITUD).subscribe((transferencia) => {
      respuesta = transferencia;
    });

    const solicitud = controlador.expectOne(URL_TRANSFERENCIAS);
    expect(solicitud.request.method).toBe('POST');
    expect(solicitud.request.body).toEqual(SOLICITUD);
    const creada: TransferenciaResponse = {
      id: 10,
      ...SOLICITUD,
      fecha: '2026-10-24T15:30:00Z',
      estado: 'EN_REVISION',
      motivo: 'El monto supera 5000.00',
    };
    solicitud.flush(creada, { status: 201, statusText: 'Created' });

    expect(respuesta).toEqual(creada);
  });

  it('envía solo los campos del contrato aunque reciba propiedades adicionales', () => {
    const conExtras = { ...SOLICITUD, confirmado: true };

    servicio.ejecutar(conExtras).subscribe();

    const solicitud = controlador.expectOne(URL_TRANSFERENCIAS);
    expect(solicitud.request.body).toEqual(SOLICITUD);
    solicitud.flush({});
  });

  it('propaga el rechazo del backend como ErrorApi con el monto disponible', () => {
    let error: unknown;
    servicio.ejecutar(SOLICITUD).subscribe({
      error: (causa: unknown) => {
        error = causa;
      },
    });

    controlador.expectOne(URL_TRANSFERENCIAS).flush(
      {
        type: 'about:blank',
        title: 'Unprocessable Entity',
        status: 422,
        detail:
          'La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 100.00',
        codigo: 'LIMITE_DIARIO_EXCEDIDO',
        timestamp: '2026-10-24T15:30:00Z',
        detalles: { limiteDiario: 10000, montoDisponible: 100 },
      },
      { status: 422, statusText: 'Unprocessable Entity' },
    );

    expect(error).toBeInstanceOf(ErrorApi);
    expect(error).toMatchObject({
      codigo: 'LIMITE_DIARIO_EXCEDIDO',
      detalles: { montoDisponible: 100 },
    });
  });
});
