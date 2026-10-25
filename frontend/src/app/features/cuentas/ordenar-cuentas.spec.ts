import { CuentaResponse } from './cuenta.model';
import { ordenarPorSaldo } from './ordenar-cuentas';

function cuenta(id: number, saldo: number): CuentaResponse {
  return {
    id,
    numeroCuenta: String(id).padStart(10, '0'),
    titular: `Titular ${String(id)}`,
    tipo: 'AHORRO',
    saldo,
    estado: 'ACTIVA',
    fechaCreacion: '2026-10-24T15:30:00Z',
  };
}

const cuentas = [cuenta(1, 500), cuenta(2, 20000.5), cuenta(3, 0), cuenta(4, 1500)];

function ids(resultado: readonly CuentaResponse[]): number[] {
  return resultado.map((item) => item.id);
}

describe('ordenarPorSaldo', () => {
  it('ordena de menor a mayor saldo en dirección ascendente', () => {
    expect(ids(ordenarPorSaldo(cuentas, 'asc'))).toEqual([3, 1, 4, 2]);
  });

  it('ordena de mayor a menor saldo en dirección descendente', () => {
    expect(ids(ordenarPorSaldo(cuentas, 'desc'))).toEqual([2, 4, 1, 3]);
  });

  it('conserva el orden original sin dirección', () => {
    expect(ordenarPorSaldo(cuentas, '')).toBe(cuentas);
  });

  it('mantiene el orden de llegada entre saldos iguales', () => {
    const empatadas = [cuenta(1, 100), cuenta(2, 50), cuenta(3, 100)];

    expect(ids(ordenarPorSaldo(empatadas, 'desc'))).toEqual([1, 3, 2]);
  });

  it('no modifica la lista recibida', () => {
    const original = [...cuentas];

    ordenarPorSaldo(cuentas, 'asc');

    expect(cuentas).toEqual(original);
  });
});
