import { registerLocaleData } from '@angular/common';
import localeEsEc from '@angular/common/locales/es-EC';
import { ErrorApi } from '../../core/models/error-api.model';
import { resultadoExitoso, resultadoFallido } from './resultado-transferencia';
import { TransferenciaResponse } from './transferencia.model';

const TRANSFERENCIA: TransferenciaResponse = {
  id: 10,
  numeroCuentaOrigen: '1111111111',
  numeroCuentaDestino: '2222222222',
  monto: 1250.5,
  fecha: '2026-10-24T15:30:00Z',
  estado: 'COMPLETADA',
  motivo: null,
};

describe('resultado de una transferencia', () => {
  beforeAll(() => {
    registerLocaleData(localeEsEc);
  });

  it('describe una transferencia completada con el monto en USD', () => {
    expect(resultadoExitoso(TRANSFERENCIA, 'es-EC')).toEqual({
      tipo: 'exito',
      texto:
        'Transferencia realizada por $1.250,50 de la cuenta 1111111111 a la cuenta 2222222222.',
      detalles: [],
    });
  });

  it('advierte cuando la transferencia quedó en revisión e incluye el motivo', () => {
    const enRevision: TransferenciaResponse = {
      ...TRANSFERENCIA,
      estado: 'EN_REVISION',
      motivo: 'El monto supera 5000.00',
    };

    expect(resultadoExitoso(enRevision, 'es-EC')).toEqual({
      tipo: 'advertencia',
      texto:
        'Transferencia realizada por $1.250,50 de la cuenta 1111111111 a la cuenta 2222222222 y marcada para revisión.',
      detalles: ['El monto supera 5000.00'],
    });
  });

  it('muestra el mensaje del backend, los errores por campo y el código de seguimiento', () => {
    const error = new ErrorApi({
      status: 400,
      codigo: 'SOLICITUD_INVALIDA',
      mensaje: 'La solicitud contiene datos inválidos',
      errores: [{ campo: 'monto', mensaje: 'El monto debe ser mayor a cero' }],
      detalles: {},
      requestId: 'abc-123',
    });

    expect(resultadoFallido(error)).toEqual({
      tipo: 'error',
      texto: 'La solicitud contiene datos inválidos',
      detalles: ['El monto debe ser mayor a cero', 'Código de seguimiento: abc-123'],
    });
  });

  it('usa un mensaje genérico si el error no viene de la API', () => {
    expect(resultadoFallido(new Error('fallo'))).toEqual({
      tipo: 'error',
      texto: 'No se pudo completar la transferencia.',
      detalles: [],
    });
  });
});
