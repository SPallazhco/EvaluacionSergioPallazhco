import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { API_URL } from '../../core/config/api.config';
import { provideLocalizacion } from '../../core/config/localizacion';
import { TransferenciaFormComponent } from '../transferencias/transferencia-form.component';
import { DashboardComponent } from './dashboard.component';

const URL_CUENTAS = 'https://api.prueba/api/cuentas?pagina=0&tamanio=100';
const PAGINA_VACIA = { contenido: [], pagina: 0, tamanio: 100, totalElementos: 0, totalPaginas: 0 };

describe('DashboardComponent', () => {
  it('actualiza la tabla de cuentas cuando se realiza una transferencia', async () => {
    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideLocalizacion(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    const controlador = TestBed.inject(HttpTestingController);
    const fixture = TestBed.createComponent(DashboardComponent);
    TestBed.tick();
    controlador.match(URL_CUENTAS).forEach((solicitud) => {
      solicitud.flush(PAGINA_VACIA);
    });
    await fixture.whenStable();

    const formulario = fixture.debugElement.query(By.directive(TransferenciaFormComponent))
      .componentInstance as TransferenciaFormComponent;
    formulario.transferenciaRealizada.emit({
      id: 1,
      numeroCuentaOrigen: '1111111111',
      numeroCuentaDestino: '2222222222',
      monto: 10,
      fecha: '2026-10-24T15:30:00Z',
      estado: 'COMPLETADA',
      motivo: null,
    });
    TestBed.tick();

    const recargas = controlador.match(URL_CUENTAS);
    expect(recargas).toHaveLength(1);
    recargas.forEach((solicitud) => {
      solicitud.flush(PAGINA_VACIA);
    });
    controlador.verify();
  });
});
