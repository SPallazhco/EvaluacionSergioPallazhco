export type EstadoTransferencia = 'COMPLETADA' | 'RECHAZADA' | 'EN_REVISION';

export interface CrearTransferenciaRequest {
  readonly numeroCuentaOrigen: string;
  readonly numeroCuentaDestino: string;
  readonly monto: number;
}

export interface TransferenciaResponse {
  readonly id: number;
  readonly numeroCuentaOrigen: string;
  readonly numeroCuentaDestino: string;
  readonly monto: number;
  readonly fecha: string;
  readonly estado: EstadoTransferencia;
  readonly motivo: string | null;
}
