import {
  AbstractControl,
  FormGroupDirective,
  NgForm,
  ValidationErrors,
  ValidatorFn,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';

const PATRON_NUMERICO = /^\d+([.,]\d+)?$/;
const PATRON_DOS_DECIMALES = /^\d+([.,]\d{1,2})?$/;

function textoMonto(control: AbstractControl): string {
  const valor: unknown = control.value;
  return typeof valor === 'string' ? valor.trim() : '';
}

export function convertirMonto(texto: string): number | null {
  const limpio = texto.trim();
  return PATRON_NUMERICO.test(limpio) ? Number(limpio.replace(',', '.')) : null;
}

export const montoNumerico: ValidatorFn = (control) => {
  const texto = textoMonto(control);
  return texto === '' || PATRON_NUMERICO.test(texto) ? null : { montoNoNumerico: true };
};

export const montoMayorQueCero: ValidatorFn = (control) => {
  const monto = convertirMonto(textoMonto(control));
  return monto === null || monto > 0 ? null : { montoNoPositivo: true };
};

export const montoConDosDecimales: ValidatorFn = (control) => {
  const texto = textoMonto(control);
  return !PATRON_NUMERICO.test(texto) || PATRON_DOS_DECIMALES.test(texto)
    ? null
    : { montoDecimales: true };
};

export const cuentasDistintas: ValidatorFn = (grupo): ValidationErrors | null => {
  const origen: unknown = grupo.get('cuentaOrigen')?.value;
  const destino: unknown = grupo.get('cuentaDestino')?.value;
  return origen && origen === destino ? { cuentasIguales: true } : null;
};

const MENSAJES_MONTO: readonly (readonly [string, string])[] = [
  ['required', 'Ingrese el monto'],
  ['montoNoNumerico', 'El monto debe ser numérico'],
  ['montoNoPositivo', 'El monto debe ser mayor a cero'],
  ['montoDecimales', 'El monto admite hasta 2 decimales'],
];

export function mensajeErrorMonto(errores: ValidationErrors | null): string {
  return MENSAJES_MONTO.find(([clave]) => errores?.[clave])?.[1] ?? '';
}

export function mensajeErrorDestino(errores: ValidationErrors | null): string {
  return errores?.['required']
    ? 'Seleccione la cuenta destino'
    : 'La cuenta destino debe ser diferente a la cuenta origen';
}

export class CuentasIgualesErrorStateMatcher implements ErrorStateMatcher {
  isErrorState(control: AbstractControl | null, formulario: FormGroupDirective | NgForm | null) {
    if (!control) {
      return false;
    }
    const mostrar = control.touched || (formulario?.submitted ?? false);
    const cuentasIguales = control.parent?.hasError('cuentasIguales') ?? false;
    return mostrar && (control.invalid || cuentasIguales);
  }
}
