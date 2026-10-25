import { CodigoError, DetallesProblema, ErrorCampo } from './problema.model';

export type CodigoErrorCliente = 'SIN_CONEXION' | 'RESPUESTA_INESPERADA';

export interface DatosErrorApi {
  readonly status: number;
  readonly codigo: CodigoError | CodigoErrorCliente;
  readonly mensaje: string;
  readonly errores: readonly ErrorCampo[];
  readonly detalles: DetallesProblema;
  readonly requestId: string | null;
}

export class ErrorApi extends Error {
  override readonly name = 'ErrorApi';
  readonly status: number;
  readonly codigo: CodigoError | CodigoErrorCliente;
  readonly errores: readonly ErrorCampo[];
  readonly detalles: DetallesProblema;
  readonly requestId: string | null;

  constructor(datos: DatosErrorApi) {
    super(datos.mensaje);
    this.status = datos.status;
    this.codigo = datos.codigo;
    this.errores = datos.errores;
    this.detalles = datos.detalles;
    this.requestId = datos.requestId;
  }
}
