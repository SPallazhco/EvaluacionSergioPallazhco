import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { API_URL } from '../../core/config/api.config';
import { PaginaResponse } from '../../shared/models/pagina.model';
import { CuentaResponse, ResumenCuentaResponse } from './cuenta.model';
import { CuentaService, TAMANIO_MAXIMO_PAGINA } from './cuenta.service';

const URL_CUENTAS = 'https://api.prueba/api/cuentas';

function crearCuenta(id: number): CuentaResponse {
  return {
    id,
    numeroCuenta: String(id).padStart(10, '0'),
    titular: `Titular ${String(id)}`,
    tipo: 'AHORRO',
    saldo: id * 100,
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-24T15:30:00Z',
  };
}

function crearPagina(
  pagina: number,
  totalPaginas: number,
  contenido: readonly CuentaResponse[],
): PaginaResponse<CuentaResponse> {
  return {
    contenido,
    pagina,
    tamanio: TAMANIO_MAXIMO_PAGINA,
    totalElementos: contenido.length,
    totalPaginas,
  };
}

describe('CuentaService', () => {
  let servicio: CuentaService;
  let controlador: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    servicio = TestBed.inject(CuentaService);
    controlador = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    controlador.verify();
  });

  function esperarPagina(pagina: number) {
    return controlador.expectOne(
      (solicitud) =>
        solicitud.url === URL_CUENTAS &&
        solicitud.params.get('pagina') === String(pagina) &&
        solicitud.params.get('tamanio') === String(TAMANIO_MAXIMO_PAGINA),
    );
  }

  it('listar consulta GET /cuentas con los parámetros de paginación', () => {
    let respuesta: PaginaResponse<CuentaResponse> | undefined;
    servicio.listar(2, 20).subscribe((pagina) => {
      respuesta = pagina;
    });

    const solicitud = controlador.expectOne(`${URL_CUENTAS}?pagina=2&tamanio=20`);
    expect(solicitud.request.method).toBe('GET');
    const pagina = crearPagina(2, 3, [crearCuenta(41)]);
    solicitud.flush(pagina);

    expect(respuesta).toEqual(pagina);
  });

  it('listarTodas hace una sola solicitud cuando hay una página', () => {
    let cuentas: readonly CuentaResponse[] | undefined;
    servicio.listarTodas().subscribe((resultado) => {
      cuentas = resultado;
    });

    esperarPagina(0).flush(crearPagina(0, 1, [crearCuenta(1), crearCuenta(2)]));

    expect(cuentas?.map((cuenta) => cuenta.id)).toEqual([1, 2]);
  });

  it('listarTodas devuelve una lista vacía cuando no hay cuentas', () => {
    let cuentas: readonly CuentaResponse[] | undefined;
    servicio.listarTodas().subscribe((resultado) => {
      cuentas = resultado;
    });

    esperarPagina(0).flush(crearPagina(0, 0, []));

    expect(cuentas).toEqual([]);
  });

  it('listarTodas pide las páginas restantes y las une en orden', () => {
    let cuentas: readonly CuentaResponse[] | undefined;
    servicio.listarTodas().subscribe((resultado) => {
      cuentas = resultado;
    });

    esperarPagina(0).flush(crearPagina(0, 3, [crearCuenta(1)]));
    const segunda = esperarPagina(1);
    const tercera = esperarPagina(2);
    tercera.flush(crearPagina(2, 3, [crearCuenta(3)]));
    segunda.flush(crearPagina(1, 3, [crearCuenta(2)]));

    expect(cuentas?.map((cuenta) => cuenta.id)).toEqual([1, 2, 3]);
  });

  it('listarTodas propaga el error si falla una de las páginas', () => {
    let error: unknown;
    let cuentas: readonly CuentaResponse[] | undefined;
    servicio.listarTodas().subscribe({
      next: (resultado) => {
        cuentas = resultado;
      },
      error: (causa: unknown) => {
        error = causa;
      },
    });

    esperarPagina(0).flush(crearPagina(0, 2, [crearCuenta(1)]));
    esperarPagina(1).flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(error).toBeDefined();
    expect(cuentas).toBeUndefined();
  });

  it('obtenerResumen consulta GET /cuentas/{id}/resumen', () => {
    const resumen: ResumenCuentaResponse = {
      cuentaId: 5,
      numeroCuenta: '3000000003',
      saldoActual: 17757.11,
      totalTransferidoHoy: 6000,
      montoDisponibleHoy: 4000,
      cantidadTransferenciasHoy: 1,
      cantidadSospechosasHoy: 1,
      cantidadSospechosasTotal: 1,
    };
    let respuesta: ResumenCuentaResponse | undefined;
    servicio.obtenerResumen(5).subscribe((resultado) => {
      respuesta = resultado;
    });

    const solicitud = controlador.expectOne(`${URL_CUENTAS}/5/resumen`);
    expect(solicitud.request.method).toBe('GET');
    solicitud.flush(resumen);

    expect(respuesta).toEqual(resumen);
  });
});
