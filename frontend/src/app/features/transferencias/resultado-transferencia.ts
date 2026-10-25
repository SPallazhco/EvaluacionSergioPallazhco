import { formatCurrency } from '@angular/common';
import { ErrorApi } from '../../core/models/error-api.model';
import { TipoMensaje } from '../../shared/components/mensaje/mensaje.component';
import { TransferenciaResponse } from './transferencia.model';

export interface ResultadoTransferencia {
  readonly tipo: TipoMensaje;
  readonly texto: string;
  readonly detalles: readonly string[];
}

const MENSAJE_ERROR_GENERICO = 'No se pudo completar la transferencia.';

export function resultadoExitoso(
  transferencia: TransferenciaResponse,
  locale: string,
): ResultadoTransferencia {
  const monto = formatCurrency(transferencia.monto, locale, '$', 'USD');
  const resumen = `${monto} de la cuenta ${transferencia.numeroCuentaOrigen} a la cuenta ${transferencia.numeroCuentaDestino}`;

  if (transferencia.estado === 'EN_REVISION') {
    return {
      tipo: 'advertencia',
      texto: `Transferencia realizada por ${resumen} y marcada para revisión.`,
      detalles: transferencia.motivo ? [transferencia.motivo] : [],
    };
  }
  return { tipo: 'exito', texto: `Transferencia realizada por ${resumen}.`, detalles: [] };
}

export function resultadoFallido(error: unknown): ResultadoTransferencia {
  if (!(error instanceof ErrorApi)) {
    return { tipo: 'error', texto: MENSAJE_ERROR_GENERICO, detalles: [] };
  }
  const seguimiento = error.requestId ? [`Código de seguimiento: ${error.requestId}`] : [];
  return {
    tipo: 'error',
    texto: error.message,
    detalles: [...error.errores.map((errorCampo) => errorCampo.mensaje), ...seguimiento],
  };
}
