import { FormControl, FormGroup } from '@angular/forms';
import {
  convertirMonto,
  cuentasDistintas,
  CuentasIgualesErrorStateMatcher,
  mensajeErrorDestino,
  mensajeErrorMonto,
  montoConDosDecimales,
  montoMayorQueCero,
  montoNumerico,
} from './transferencia.validators';

function control(valor: string): FormControl<string> {
  return new FormControl(valor, { nonNullable: true });
}

function grupo(origen: string, destino: string): FormGroup {
  return new FormGroup(
    { cuentaOrigen: control(origen), cuentaDestino: control(destino) },
    { validators: cuentasDistintas },
  );
}

describe('validadores de transferencia', () => {
  describe('convertirMonto', () => {
    it.each([
      ['250', 250],
      ['250.5', 250.5],
      ['250,50', 250.5],
      [' 10 ', 10],
    ])('convierte "%s" en %d', (texto, esperado) => {
      expect(convertirMonto(texto)).toBe(esperado);
    });

    it.each(['', 'abc', '1.000,50', '-5', '1e3', '12,'])('devuelve null para "%s"', (texto) => {
      expect(convertirMonto(texto)).toBeNull();
    });
  });

  describe('montoNumerico', () => {
    it.each(['', '100', '100.25', '100,25'])('acepta "%s"', (valor) => {
      expect(montoNumerico(control(valor))).toBeNull();
    });

    it.each(['abc', '12a', '-10', '1.000,00', '1e3'])('rechaza "%s"', (valor) => {
      expect(montoNumerico(control(valor))).toEqual({ montoNoNumerico: true });
    });
  });

  describe('montoMayorQueCero', () => {
    it.each(['0', '0.00', '0,0'])('rechaza "%s"', (valor) => {
      expect(montoMayorQueCero(control(valor))).toEqual({ montoNoPositivo: true });
    });

    it.each(['0.01', '1', ''])('acepta "%s"', (valor) => {
      expect(montoMayorQueCero(control(valor))).toBeNull();
    });

    it('no se pronuncia sobre un valor no numérico', () => {
      expect(montoMayorQueCero(control('abc'))).toBeNull();
    });
  });

  describe('montoConDosDecimales', () => {
    it.each(['10', '10.5', '10,55'])('acepta "%s"', (valor) => {
      expect(montoConDosDecimales(control(valor))).toBeNull();
    });

    it('rechaza más de 2 decimales', () => {
      expect(montoConDosDecimales(control('10.555'))).toEqual({ montoDecimales: true });
    });

    it('no se pronuncia sobre un valor no numérico', () => {
      expect(montoConDosDecimales(control('abc'))).toBeNull();
    });
  });

  describe('cuentasDistintas', () => {
    it('marca el grupo cuando origen y destino son la misma cuenta', () => {
      expect(grupo('1111111111', '1111111111').errors).toEqual({ cuentasIguales: true });
    });

    it('acepta cuentas diferentes', () => {
      expect(grupo('1111111111', '2222222222').errors).toBeNull();
    });

    it('no marca el grupo mientras falte alguna cuenta', () => {
      expect(grupo('', '').errors).toBeNull();
    });
  });

  describe('mensajes de error', () => {
    it('muestra un solo mensaje del monto según la prioridad de los errores', () => {
      expect(mensajeErrorMonto({ required: true })).toBe('Ingrese el monto');
      expect(mensajeErrorMonto({ montoNoNumerico: true })).toBe('El monto debe ser numérico');
      expect(mensajeErrorMonto({ montoNoPositivo: true })).toBe('El monto debe ser mayor a cero');
      expect(mensajeErrorMonto({ montoDecimales: true })).toBe('El monto admite hasta 2 decimales');
      expect(mensajeErrorMonto(null)).toBe('');
    });

    it('distingue destino vacío de destino igual al origen', () => {
      expect(mensajeErrorDestino({ required: true })).toBe('Seleccione la cuenta destino');
      expect(mensajeErrorDestino(null)).toBe(
        'La cuenta destino debe ser diferente a la cuenta origen',
      );
    });
  });

  describe('CuentasIgualesErrorStateMatcher', () => {
    const matcher = new CuentasIgualesErrorStateMatcher();

    function destinoIgualAlOrigen(): FormControl<string> {
      const formulario = new FormGroup(
        { cuentaOrigen: control('1111111111'), cuentaDestino: control('1111111111') },
        { validators: cuentasDistintas },
      );
      return formulario.controls.cuentaDestino;
    }

    it('muestra el error del destino cuando es igual al origen y fue tocado', () => {
      const destino = destinoIgualAlOrigen();
      destino.markAsTouched();

      expect(matcher.isErrorState(destino, null)).toBe(true);
    });

    it('no muestra el error antes de que el usuario interactúe', () => {
      expect(matcher.isErrorState(destinoIgualAlOrigen(), null)).toBe(false);
    });
  });
});
