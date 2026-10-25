import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { API_URL } from '../../core/config/api.config';
import { provideLocalizacion } from '../../core/config/localizacion';
import { erroresHttpInterceptor } from '../../core/http/errores-http.interceptor';
import { CuentaResponse } from './cuenta.model';
import { CuentasComponent } from './cuentas.component';

const URL_CUENTAS = 'https://api.prueba/api/cuentas';

const CUENTAS: readonly CuentaResponse[] = [
  {
    id: 1,
    numeroCuenta: '1111111111',
    titular: 'Ana Pérez',
    tipo: 'AHORRO',
    saldo: 1500,
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-24T15:30:00Z',
  },
  {
    id: 2,
    numeroCuenta: '2222222222',
    titular: 'Luis Torres',
    tipo: 'CORRIENTE',
    saldo: 20000.5,
    estado: 'INACTIVA',
    fechaCreacion: '2026-10-24T15:31:00Z',
  },
  {
    id: 3,
    numeroCuenta: '3333333333',
    titular: 'María León',
    tipo: 'AHORRO',
    saldo: 0,
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-24T15:32:00Z',
  },
];

describe('CuentasComponent', () => {
  let fixture: ComponentFixture<CuentasComponent>;
  let controlador: HttpTestingController;
  let elemento: HTMLElement;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [CuentasComponent],
      providers: [
        provideHttpClient(withInterceptors([erroresHttpInterceptor])),
        provideHttpClientTesting(),
        provideLocalizacion(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    controlador = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(CuentasComponent);
    elemento = fixture.nativeElement as HTMLElement;
    TestBed.tick();
  });

  afterEach(() => {
    controlador.verify();
  });

  async function responderCuentas(cuentas: readonly CuentaResponse[]): Promise<void> {
    controlador
      .expectOne((solicitud) => solicitud.url === URL_CUENTAS)
      .flush({
        contenido: cuentas,
        pagina: 0,
        tamanio: 100,
        totalElementos: cuentas.length,
        totalPaginas: 1,
      });
    await fixture.whenStable();
  }

  function filas(): string[][] {
    return Array.from(elemento.querySelectorAll('tr.mat-mdc-row')).map((fila) =>
      Array.from(fila.querySelectorAll('td')).map((celda) => celda.textContent.trim()),
    );
  }

  function titulares(): string[] {
    return filas().map((fila) => fila[1] ?? '');
  }

  async function ordenarPorSaldo(): Promise<void> {
    const encabezado = elemento.querySelector<HTMLElement>('th.mat-sort-header');
    encabezado?.click();
    await fixture.whenStable();
  }

  it('muestra número, titular, tipo, saldo y estado de cada cuenta', async () => {
    await responderCuentas(CUENTAS);

    expect(filas()).toEqual([
      ['1111111111', 'Ana Pérez', 'Ahorro', '$1.500,00', 'Activa'],
      ['2222222222', 'Luis Torres', 'Corriente', '$20.000,50', 'Inactiva'],
      ['3333333333', 'María León', 'Ahorro', '$0,00', 'Activa'],
    ]);
  });

  it('ordena por saldo ascendente y luego descendente', async () => {
    await responderCuentas(CUENTAS);

    await ordenarPorSaldo();
    expect(titulares()).toEqual(['María León', 'Ana Pérez', 'Luis Torres']);

    await ordenarPorSaldo();
    expect(titulares()).toEqual(['Luis Torres', 'Ana Pérez', 'María León']);
  });

  it('indica el orden actual en el encabezado para lectores de pantalla', async () => {
    await responderCuentas(CUENTAS);
    const encabezado = elemento.querySelector('th.mat-sort-header');

    await ordenarPorSaldo();

    expect(encabezado?.getAttribute('aria-sort')).toBe('ascending');
  });

  it('muestra la paginación en español', async () => {
    await responderCuentas(CUENTAS);

    const paginador = elemento.querySelector('mat-paginator');
    expect(paginador?.textContent).toContain('Elementos por página');
    expect(paginador?.textContent).toContain('1 – 3 de 3');
  });

  it('muestra un mensaje cuando no hay cuentas', async () => {
    await responderCuentas([]);

    expect(elemento.querySelector('.cuentas__vacio')?.textContent).toContain(
      'No hay cuentas registradas.',
    );
  });

  it('muestra el error del backend y permite reintentar', async () => {
    controlador
      .expectOne((solicitud) => solicitud.url === URL_CUENTAS)
      .flush(
        {
          type: 'about:blank',
          title: 'Internal Server Error',
          status: 500,
          detail: 'Ocurrió un error inesperado',
          codigo: 'ERROR_INTERNO',
          timestamp: '2026-10-24T15:30:00Z',
        },
        { status: 500, statusText: 'Internal Server Error' },
      );
    await fixture.whenStable();

    const alerta = elemento.querySelector('[role="alert"]');
    expect(alerta?.textContent).toContain('Ocurrió un error inesperado');

    alerta?.querySelector('button')?.click();
    TestBed.tick();
    await responderCuentas(CUENTAS);

    expect(elemento.querySelector('[role="alert"]')).toBeNull();
    expect(filas()).toHaveLength(3);
  });
});
