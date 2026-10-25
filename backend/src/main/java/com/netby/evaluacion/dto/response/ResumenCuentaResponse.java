package com.netby.evaluacion.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Resumen de la cuenta calculado en tiempo real")
public record ResumenCuentaResponse(
        @Schema(description = "Identificador de la cuenta", example = "1")
        Long cuentaId,
        @Schema(description = "Número de la cuenta", example = "1234567890")
        String numeroCuenta,
        @Schema(description = "Saldo actual", example = "13000.00")
        BigDecimal saldoActual,
        @Schema(description = "Monto enviado hoy (sin rechazadas)", example = "7000.00")
        BigDecimal totalTransferidoHoy,
        @Schema(description = "Monto que aún puede transferir hoy según el límite diario", example = "3000.00")
        BigDecimal montoDisponibleHoy,
        @Schema(description = "Transferencias enviadas hoy (sin rechazadas)", example = "2")
        long cantidadTransferenciasHoy,
        @Schema(description = "Transferencias enviadas hoy marcadas EN_REVISION", example = "1")
        long cantidadSospechosasHoy,
        @Schema(description = "Total histórico de transferencias enviadas marcadas EN_REVISION", example = "3")
        long cantidadSospechosasTotal
) {
}
