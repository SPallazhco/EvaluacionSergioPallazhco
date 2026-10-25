package com.netby.evaluacion.dto.response;

import com.netby.evaluacion.entity.EstadoTransferencia;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferenciaResponse(
        Long id,
        String numeroCuentaOrigen,
        String numeroCuentaDestino,
        BigDecimal monto,
        Instant fecha,
        EstadoTransferencia estado,
        String motivo
) {
}
