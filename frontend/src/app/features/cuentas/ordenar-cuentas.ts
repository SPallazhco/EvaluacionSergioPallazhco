import { SortDirection } from '@angular/material/sort';
import { CuentaResponse } from './cuenta.model';

export function ordenarPorSaldo(
  cuentas: readonly CuentaResponse[],
  direccion: SortDirection,
): readonly CuentaResponse[] {
  if (direccion === '') {
    return cuentas;
  }
  const factor = direccion === 'asc' ? 1 : -1;
  return [...cuentas].sort((a, b) => factor * (a.saldo - b.saldo));
}
