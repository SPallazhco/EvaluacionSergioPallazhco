import { TestBed } from '@angular/core/testing';
import { provideLocalizacion } from '../../core/config/localizacion';
import { ResumenCuentaComponent } from './resumen-cuenta.component';

describe('ResumenCuentaComponent', () => {
  it('muestra el saldo y la actividad del día de la cuenta', async () => {
    TestBed.configureTestingModule({ providers: [provideLocalizacion()] });
    const fixture = TestBed.createComponent(ResumenCuentaComponent);
    fixture.componentRef.setInput('resumen', {
      cuentaId: 5,
      numeroCuenta: '3000000003',
      saldoActual: 17757.11,
      totalTransferidoHoy: 6000,
      montoDisponibleHoy: 4000,
      cantidadTransferenciasHoy: 1,
      cantidadSospechosasHoy: 1,
      cantidadSospechosasTotal: 3,
    });
    await fixture.whenStable();

    const elemento = fixture.nativeElement as HTMLElement;
    const datos = Array.from(elemento.querySelectorAll('.resumen__dato')).map((dato) => [
      dato.querySelector('dt')?.textContent.trim(),
      dato.querySelector('dd')?.textContent.trim(),
    ]);
    expect(datos).toEqual([
      ['Saldo actual', '$17.757,11'],
      ['Disponible hoy', '$4.000,00'],
      ['Transferido hoy', '$6.000,00'],
      ['Transferencias hoy', '1'],
      ['En revisión hoy', '1'],
    ]);
    expect(elemento.querySelector('dl')?.getAttribute('aria-label')).toBe(
      'Actividad de hoy de la cuenta 3000000003',
    );
  });
});
