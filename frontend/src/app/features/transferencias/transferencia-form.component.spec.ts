import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { MatInputHarness } from '@angular/material/input/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { HarnessLoader } from '@angular/cdk/testing';
import { API_URL } from '../../core/config/api.config';
import { provideLocalizacion } from '../../core/config/localizacion';
import { CuentaResponse } from '../cuentas/cuenta.model';
import { TransferenciaFormComponent } from './transferencia-form.component';

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

describe('TransferenciaFormComponent', () => {
  let fixture: ComponentFixture<TransferenciaFormComponent>;
  let elemento: HTMLElement;
  let cargador: HarnessLoader;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [TransferenciaFormComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideLocalizacion(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    fixture = TestBed.createComponent(TransferenciaFormComponent);
    elemento = fixture.nativeElement as HTMLElement;
    cargador = TestbedHarnessEnvironment.loader(fixture);
    TestBed.tick();
    TestBed.inject(HttpTestingController)
      .expectOne((solicitud) => solicitud.url === 'https://api.prueba/api/cuentas')
      .flush({ contenido: CUENTAS, pagina: 0, tamanio: 100, totalElementos: 3, totalPaginas: 1 });
    await fixture.whenStable();
  });

  function selector(etiqueta: string): Promise<MatSelectHarness> {
    return cargador.getHarness(
      MatSelectHarness.with({ selector: `[formControlName="${etiqueta}"]` }),
    );
  }

  async function enviar(): Promise<void> {
    elemento.querySelector<HTMLButtonElement>('button[type="submit"]')?.click();
    await fixture.whenStable();
  }

  function errores(): string[] {
    return Array.from(elemento.querySelectorAll('mat-error')).map((error) =>
      error.textContent.trim(),
    );
  }

  async function escribirMonto(valor: string): Promise<void> {
    const monto = await cargador.getHarness(MatInputHarness);
    await monto.setValue(valor);
    await monto.blur();
  }

  it('exige todos los campos al enviar', async () => {
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

  it('rechaza que la cuenta destino sea igual a la origen', async () => {
    await (await selector('cuentaOrigen')).clickOptions({ text: /1111111111/ });
    await (await selector('cuentaDestino')).clickOptions({ text: /1111111111/ });
    await escribirMonto('100');
    await enviar();

    expect(errores()).toEqual(['La cuenta destino debe ser diferente a la cuenta origen']);
  });

  it('no muestra errores con datos válidos', async () => {
    await (await selector('cuentaOrigen')).clickOptions({ text: /1111111111/ });
    await (await selector('cuentaDestino')).clickOptions({ text: /2222222222/ });
    await escribirMonto('250,50');
    await enviar();

    expect(errores()).toEqual([]);
  });

  it('muestra el saldo disponible de la cuenta origen', async () => {
    await (await selector('cuentaOrigen')).clickOptions({ text: /1111111111/ });

    expect(elemento.querySelector('mat-hint')?.textContent).toContain(
      'Saldo disponible: $1.500,00',
    );
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
