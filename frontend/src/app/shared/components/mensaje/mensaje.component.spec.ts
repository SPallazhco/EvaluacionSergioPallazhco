import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MensajeComponent } from './mensaje.component';

describe('MensajeComponent', () => {
  let fixture: ComponentFixture<MensajeComponent>;
  let elemento: HTMLElement;

  beforeEach(() => {
    fixture = TestBed.createComponent(MensajeComponent);
    elemento = fixture.nativeElement as HTMLElement;
    fixture.componentRef.setInput('texto', 'Transferencia realizada');
  });

  it('anuncia los errores como alerta', async () => {
    fixture.componentRef.setInput('tipo', 'error');
    await fixture.whenStable();

    expect(elemento.getAttribute('role')).toBe('alert');
    expect(elemento.classList).toContain('mensaje--error');
  });

  it('anuncia el éxito como estado y muestra el texto sin detalles', async () => {
    fixture.componentRef.setInput('tipo', 'exito');
    await fixture.whenStable();

    expect(elemento.getAttribute('role')).toBe('status');
    expect(elemento.querySelector('.mensaje__texto')?.textContent).toBe('Transferencia realizada');
    expect(elemento.querySelector('ul')).toBeNull();
  });

  it('lista los detalles', async () => {
    fixture.componentRef.setInput('tipo', 'advertencia');
    fixture.componentRef.setInput('detalles', ['Primero', 'Segundo']);
    await fixture.whenStable();

    const detalles = Array.from(elemento.querySelectorAll('li')).map((li) => li.textContent);
    expect(detalles).toEqual(['Primero', 'Segundo']);
  });
});
