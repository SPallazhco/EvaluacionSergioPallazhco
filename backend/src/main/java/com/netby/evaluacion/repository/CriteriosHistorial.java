package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.EstadoTransferencia;

import java.time.Instant;

// Un valor null significa sin filtro
public record CriteriosHistorial(
        String numeroCuentaOrigen,
        String numeroCuentaDestino,
        Instant desde,
        Instant hasta,
        EstadoTransferencia estado
) {
}
