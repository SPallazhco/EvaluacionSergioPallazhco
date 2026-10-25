package com.netby.evaluacion.dto.response;

import java.math.BigDecimal;

/**
 * Resumen de la actividad de una cuenta como origen de transferencias. Los valores "del día"
 * corresponden al día calendario en curso en la zona horaria de negocio y no incluyen rechazadas.
 */
public record ResumenCuentaResponse(
        Long cuentaId,
        String numeroCuenta,
        BigDecimal saldoActual,
        BigDecimal totalTransferidoHoy,
        BigDecimal montoDisponibleHoy,
        long cantidadTransferenciasHoy,
        long cantidadSospechosasHoy,
        long cantidadSospechosasTotal
) {
}
