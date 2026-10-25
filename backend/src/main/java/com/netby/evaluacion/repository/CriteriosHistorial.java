package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.EstadoTransferencia;

import java.time.Instant;

/**
 * Filtros opcionales del historial de transferencias. Un valor {@code null} significa "sin filtro".
 * Las fechas forman un rango semiabierto {@code [desde, hasta)} en UTC.
 */
public record CriteriosHistorial(
        String numeroCuentaOrigen,
        String numeroCuentaDestino,
        Instant desde,
        Instant hasta,
        EstadoTransferencia estado
) {
}
