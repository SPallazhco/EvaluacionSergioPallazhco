export type TipoCuenta = 'AHORRO' | 'CORRIENTE';

export type EstadoCuenta = 'ACTIVA' | 'INACTIVA';

export interface CuentaResponse {
  readonly id: number;
  readonly numeroCuenta: string;
  readonly titular: string;
  readonly tipo: TipoCuenta;
  readonly saldo: number;
  readonly estado: EstadoCuenta;
  readonly fechaCreacion: string;
}
