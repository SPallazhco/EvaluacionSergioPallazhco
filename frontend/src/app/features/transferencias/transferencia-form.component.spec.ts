import {
  HttpInterceptorFn,
  HttpResponse,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatButtonHarness } from '@angular/material/button/testing';
import { MatInputHarness } from '@angular/material/input/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { of } from 'rxjs';
import { API_URL } from '../../core/config/api.config';
import { provideLocalizacion } from '../../core/config/localizacion';
import { erroresHttpInterceptor } from '../../core/http/errores-http.interceptor';
import { CuentaResponse, ResumenCuentaResponse } from '../cuentas/cuenta.model';
import { TransferenciaFormComponent } from './transferencia-form.component';
import { TransferenciaResponse } from './transferencia.model';

const URL_API = 'https://api.prueba/api';

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
    saldo: 300,
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-24T15:31:00Z',
  },
  {
    id: 3,
    numeroCuenta: '3333333333',
    titular: 'María León',
    tipo: 'AHORRO',
    saldo: 50,
    estado: 'INACTIVA',
    fechaCreacion: '2026-10-24T15:32:00Z',
  },
];

const RESUMEN: ResumenCuentaResponse = {
  cuentaId: 1,
  numeroCuenta: '1111111111',
  saldoActual: 1500,
  totalTransferidoHoy: 250,
  montoDisponibleHoy: 9750,
  cantidadTransferenciasHoy: 1,
  cantidadSospechosasHoy: 0,
  cantidadSospechosasTotal: 0,
};

const TRANSFERENCIA: TransferenciaResponse = {
  id: 10,
  numeroCuentaOrigen: '1111111111',
  numeroCuentaDestino: '2222222222',
  monto: 250.5,
  fecha: '2026-10-24T15:30:00Z',
  estado: 'COMPLETADA',
  motivo: null,
};

const resumenesSolicitados: string[] = [];

const resumenInmediato: HttpInterceptorFn = (solicitud, siguiente) => {
  if (!solicitud.url.endsWith('/resumen')) {
    return siguiente(solicitud);
  }
  resumenesSolicitados.push(solicitud.url);
  return of(new HttpResponse({ status: 200, body: RESUMEN }));
};

describe('TransferenciaFormComponent', () => {
  let fixture: ComponentFixture<TransferenciaFormComponent>;
  let elemento: HTMLElement;
  let cargador: HarnessLoader;
  let controlador: HttpTestingController;

  beforeEach(async () => {
    resumenesSolicitados.length = 0;
    TestBed.configureTestingModule({
      imports: [TransferenciaFormComponent],
      providers: [
        provideHttpClient(withInterceptors([resumenInmediato, erroresHttpInterceptor])),
        provideHttpClientTesting(),
        provideLocalizacion(),
        { provide: API_URL, useValue: URL_API },
      ],
    });
    fixture = TestBed.createComponent(TransferenciaFormComponent);
    elemento = fixture.nativeElement as HTMLElement;
    cargador = TestbedHarnessEnvironment.loader(fixture);
    controlador = TestBed.inject(HttpTestingController);
    TestBed.tick();
    responderCuentas();
    await fixture.whenStable();
  });

  afterEach(() => {
    controlador.verify();
  });

  function responderCuentas(): void {
    controlador
      .expectOne((solicitud) => solicitud.url === `${URL_API}/cuentas`)
      .flush({ contenido: CUENTAS, pagina: 0, tamanio: 100, totalElementos: 3, totalPaginas: 1 });
  }

  function selector(control: string): Promise<MatSelectHarness> {
    return cargador.getHarness(
      MatSelectHarness.with({ selector: `[formControlName="${control}"]` }),
    );
  }

  async function escribirMonto(valor: string): Promise<void> {
    const monto = await cargador.getHarness(MatInputHarness);
    await monto.setValue(valor);
    await monto.blur();
  }

  async function llenar(origen: RegExp, destino: RegExp, monto: string): Promise<void> {
    await (await selector('cuentaOrigen')).clickOptions({ text: origen });
    await (await selector('cuentaDestino')).clickOptions({ text: destino });
    await escribirMonto(monto);
  }

  async function enviar(): Promise<void> {
    await (
      await cargador.getHarness(MatButtonHarness.with({ selector: '[type="submit"]' }))
    ).click();
  }

  function errores(): string[] {
    return Array.from(elemento.querySelectorAll('mat-error')).map((error) =>
      error.textContent.trim(),
    );
  }

  function mensaje(): HTMLElement | null {
    return elemento.querySelector('app-mensaje');
  }

  describe('validaciones del cliente', () => {
    it('exige todos los campos y no llama al backend', async () => {
      await enviar();

      expect(errores()).toEqual([
        'Seleccione la cuenta origen',
        'Seleccione la cuenta destino',
        'Ingrese el monto',
      ]);
    });

    it('rechaza un monto no numérico', async () => {
      await escribirMonto('abc');

      expect(errores()).toEqual(['El monto debe ser numérico']);
    });

    it('rechaza un monto igual a cero', async () => {
      await escribirMonto('0');

      expect(errores()).toEqual(['El monto debe ser mayor a cero']);
    });

    it('rechaza que la cuenta destino sea igual a la origen y no llama al backend', async () => {
      await llenar(/1111111111/, /1111111111/, '100');
      await enviar();

      expect(errores()).toEqual(['La cuenta destino debe ser diferente a la cuenta origen']);
    });

    it('consulta y muestra el resumen del día de la cuenta origen', async () => {
      await (await selector('cuentaOrigen')).clickOptions({ text: /1111111111/ });

      expect(resumenesSolicitados).toEqual([`${URL_API}/cuentas/1/resumen`]);
      const resumen = elemento.querySelector('app-resumen-cuenta');
      expect(resumen?.textContent).toContain('$1.500,00');
      expect(resumen?.textContent).toContain('Disponible hoy');
      expect(resumen?.textContent).toContain('$9.750,00');
    });

    it('no muestra el resumen mientras no haya cuenta origen', () => {
      expect(elemento.querySelector('app-resumen-cuenta')).toBeNull();
    });

    it('deshabilita las cuentas inactivas en los selectores', async () => {
      const origen = await selector('cuentaOrigen');
      await origen.open();
      const opciones = await origen.getOptions();
      const [activa, , inactiva] = opciones;

      expect(opciones).toHaveLength(3);
      expect(await activa.isDisabled()).toBe(false);
      expect(await inactiva.getText()).toBe('3333333333 · María León (Inactiva)');
      expect(await inactiva.isDisabled()).toBe(true);
    });
  });

  it('vuelve a cargar las cuentas cuando cambia la actualización', async () => {
    fixture.componentRef.setInput('actualizacion', 1);
    TestBed.tick();
    responderCuentas();
    await fixture.whenStable();

    const origen = await selector('cuentaOrigen');
    await origen.open();
    expect(await origen.getOptions()).toHaveLength(3);
  });

  describe('envío al backend', () => {
    it('envía la transferencia con el monto convertido a número', async () => {
      await llenar(/1111111111/, /2222222222/, '250,50');
      await enviar();

      const solicitud = controlador.expectOne(`${URL_API}/transferencias`);
      expect(solicitud.request.body).toEqual({
        numeroCuentaOrigen: '1111111111',
        numeroCuentaDestino: '2222222222',
        monto: 250.5,
      });
      solicitud.flush(TRANSFERENCIA, { status: 201, statusText: 'Created' });
    });

    it('deshabilita el botón mientras espera la respuesta', async () => {
      await llenar(/1111111111/, /2222222222/, '100');
      await enviar();
      const boton = await cargador.getHarness(
        MatButtonHarness.with({ selector: '[type="submit"]' }),
      );

      expect(await boton.isDisabled()).toBe(true);
      expect(await boton.getText()).toBe('Enviando…');

      controlador
        .expectOne(`${URL_API}/transferencias`)
        .flush(TRANSFERENCIA, { status: 201, statusText: 'Created' });
      await fixture.whenStable();

      expect(await boton.isDisabled()).toBe(false);
    });

    it('muestra el éxito, limpia el formulario y avisa al padre', async () => {
      const emitidas: TransferenciaResponse[] = [];
      fixture.componentInstance.transferenciaRealizada.subscribe((transferencia) => {
        emitidas.push(transferencia);
      });
      await llenar(/1111111111/, /2222222222/, '250,50');
      await enviar();

      controlador
        .expectOne(`${URL_API}/transferencias`)
        .flush(TRANSFERENCIA, { status: 201, statusText: 'Created' });
      await fixture.whenStable();

      expect(mensaje()?.getAttribute('role')).toBe('status');
      expect(mensaje()?.textContent).toContain(
        'Transferencia realizada por $250,50 de la cuenta 1111111111 a la cuenta 2222222222.',
      );
      expect(emitidas).toEqual([TRANSFERENCIA]);
      expect(await (await selector('cuentaOrigen')).getValueText()).toBe('');
      expect(await (await cargador.getHarness(MatInputHarness)).getValue()).toBe('');
      expect(errores()).toEqual([]);
      expect(elemento.querySelector('app-resumen-cuenta')).toBeNull();
    });

    it('advierte cuando la transferencia queda en revisión', async () => {
      await llenar(/1111111111/, /2222222222/, '6000');
      await enviar();

      controlador.expectOne(`${URL_API}/transferencias`).flush(
        {
          ...TRANSFERENCIA,
          monto: 6000,
          estado: 'EN_REVISION',
          motivo: 'El monto supera 5000.00',
        },
        { status: 201, statusText: 'Created' },
      );
      await fixture.whenStable();

      expect(mensaje()?.classList).toContain('mensaje--advertencia');
      expect(mensaje()?.textContent).toContain('marcada para revisión');
      expect(mensaje()?.textContent).toContain('El monto supera 5000.00');
    });

    it('muestra el error del backend con el código de seguimiento y conserva los datos', async () => {
      const emitidas: TransferenciaResponse[] = [];
      fixture.componentInstance.transferenciaRealizada.subscribe((transferencia) => {
        emitidas.push(transferencia);
      });
      await llenar(/1111111111/, /2222222222/, '9000');
      await enviar();

      controlador.expectOne(`${URL_API}/transferencias`).flush(
        {
          type: 'about:blank',
          title: 'Unprocessable Entity',
          status: 422,
          detail:
            'La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 1000.00',
          codigo: 'LIMITE_DIARIO_EXCEDIDO',
          timestamp: '2026-10-24T15:30:00Z',
          detalles: { limiteDiario: 10000, montoDisponible: 1000 },
        },
        {
          status: 422,
          statusText: 'Unprocessable Entity',
          headers: { 'X-Request-Id': 'id-servidor' },
        },
      );
      await fixture.whenStable();

      expect(mensaje()?.getAttribute('role')).toBe('alert');
      expect(mensaje()?.textContent).toContain('Monto disponible hoy: 1000.00');
      expect(mensaje()?.textContent).toContain('Código de seguimiento: id-servidor');
      expect(await (await cargador.getHarness(MatInputHarness)).getValue()).toBe('9000');
      expect(emitidas).toEqual([]);
    });
  });
});
