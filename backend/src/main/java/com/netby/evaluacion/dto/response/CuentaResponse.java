package com.netby.evaluacion.dto.response;

import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;

import java.math.BigDecimal;
import java.time.Instant;

public record CuentaResponse(
        Long id,
        String numeroCuenta,
        String titular,
        TipoCuenta tipo,
        BigDecimal saldo,
        EstadoCuenta estado,
        Instant fechaCreacion
) {
}
