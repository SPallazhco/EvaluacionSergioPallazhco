import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { API_URL } from '../../core/config/api.config';
import { provideLocalizacion } from '../../core/config/localizacion';
import { TransferenciaFormComponent } from '../transferencias/transferencia-form.component';
import { DashboardComponent } from './dashboard.component';

const URL_CUENTAS = 'https://api.prueba/api/cuentas?pagina=0&tamanio=100';
const PAGINA_VACIA = { contenido: [], pagina: 0, tamanio: 100, totalElementos: 0, totalPaginas: 0 };

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let controlador: HttpTestingController;
  let consultasIniciales: number;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideLocalizacion(),
        { provide: API_URL, useValue: 'https://api.prueba/api' },
      ],
    });
    controlador = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(DashboardComponent);
    TestBed.tick();
    consultasIniciales = responderConsultasDeCuentas();
    await fixture.whenStable();
  });

  afterEach(() => {
    controlador.verify();
  });

  function responderConsultasDeCuentas(): number {
    const consultas = controlador.match(URL_CUENTAS);
    consultas.forEach((consulta) => {
      consulta.flush(PAGINA_VACIA);
    });
    return consultas.length;
  }

  it('carga las cuentas para la tabla y para el formulario', () => {
    expect(consultasIniciales).toBe(2);
  });

  it('recarga la tabla y el formulario al pulsar Actualizar', () => {
    const boton = fixture.debugElement.query(By.css('.cuentas__encabezado button'));
    (boton.nativeElement as HTMLButtonElement).click();
    TestBed.tick();

    expect(responderConsultasDeCuentas()).toBe(2);
  });

  it('recarga la tabla y el formulario cuando se realiza una transferencia', () => {
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

    expect(responderConsultasDeCuentas()).toBe(2);
  });
});
