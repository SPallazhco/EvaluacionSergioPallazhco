package com.netby.evaluacion.dto.response;

import com.netby.evaluacion.entity.EstadoTransferencia;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Transferencia entre cuentas")
public record TransferenciaResponse(
        @Schema(description = "Identificador de la transferencia", example = "10")
        Long id,
        @Schema(description = "Cuenta que envía el dinero", example = "1234567890")
        String numeroCuentaOrigen,
        @Schema(description = "Cuenta que recibe el dinero", example = "0987654321")
        String numeroCuentaDestino,
        @Schema(description = "Monto transferido", example = "250.00")
        BigDecimal monto,
        @Schema(description = "Fecha y hora de la operación en UTC (ISO-8601)", example = "2026-10-24T15:30:00Z")
        Instant fecha,
        @Schema(description = "COMPLETADA: ejecutada. EN_REVISION: ejecutada y marcada como sospechosa. RECHAZADA: no ejecutada", example = "COMPLETADA")
        EstadoTransferencia estado,
        @Schema(description = "Causa del rechazo o de la marca de revisión; ausente si está COMPLETADA")
        String motivo
) {
}
