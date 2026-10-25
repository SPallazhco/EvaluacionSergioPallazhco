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

export interface ResumenCuentaResponse {
  readonly cuentaId: number;
  readonly numeroCuenta: string;
  readonly saldoActual: number;
  readonly totalTransferidoHoy: number;
  readonly montoDisponibleHoy: number;
  readonly cantidadTransferenciasHoy: number;
  readonly cantidadSospechosasHoy: number;
  readonly cantidadSospechosasTotal: number;
}

export const ETIQUETAS_TIPO_CUENTA: Readonly<Record<TipoCuenta, string>> = {
  AHORRO: 'Ahorro',
  CORRIENTE: 'Corriente',
};

export const ETIQUETAS_ESTADO_CUENTA: Readonly<Record<EstadoCuenta, string>> = {
  ACTIVA: 'Activa',
  INACTIVA: 'Inactiva',
};
