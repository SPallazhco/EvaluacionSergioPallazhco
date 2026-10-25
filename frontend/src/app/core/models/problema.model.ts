export type CodigoError =
  | 'SOLICITUD_INVALIDA'
  | 'RECURSO_NO_ENCONTRADO'
  | 'CONFLICTO_DE_DATOS'
  | 'ERROR_HTTP'
  | 'ERROR_INTERNO'
  | 'CUENTA_DUPLICADA'
  | 'CUENTA_NO_ENCONTRADA'
  | 'CUENTA_INACTIVA'
  | 'SALDO_INSUFICIENTE'
  | 'LIMITE_DIARIO_EXCEDIDO'
  | 'TRANSFERENCIA_NO_ENCONTRADA';

export interface ErrorCampo {
  readonly campo: string;
  readonly mensaje: string;
}

export interface DetallesProblema {
  readonly limiteDiario?: number;
  readonly montoDisponible?: number;
}

export interface ProblemaResponse {
  readonly type: string;
  readonly title: string;
  readonly status: number;
  readonly detail: string;
  readonly codigo: CodigoError;
  readonly timestamp: string;
  readonly errores?: readonly ErrorCampo[];
  readonly detalles?: DetallesProblema;
}
