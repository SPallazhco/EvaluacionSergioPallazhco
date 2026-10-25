package com.netby.evaluacion.dto.response;

import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Cuenta bancaria")
public record CuentaResponse(
        @Schema(description = "Identificador interno", example = "1")
        Long id,
        @Schema(description = "Número único de 10 dígitos", example = "1234567890")
        String numeroCuenta,
        @Schema(description = "Nombre del titular", example = "Ana Pérez")
        String titular,
        @Schema(description = "Tipo de cuenta", example = "AHORRO")
        TipoCuenta tipo,
        @Schema(description = "Saldo actual", example = "1500.00")
        BigDecimal saldo,
        @Schema(description = "Estado de la cuenta", example = "ACTIVA")
        EstadoCuenta estado,
        @Schema(description = "Fecha de registro en UTC (ISO-8601)", example = "2026-10-24T15:30:00Z")
        Instant fechaCreacion
) {
}
