package com.netby.evaluacion.exception;

import java.util.Map;

/**
 * La solicitud es válida en forma, pero incumple una regla de negocio (HTTP 422).
 * Es {@code non-sealed} para que cada regla defina su propia excepción con datos adicionales.
 */
public non-sealed class ReglaNegocioException extends AplicacionException {

    private final transient Map<String, Object> detalles;

    public ReglaNegocioException(CodigoError codigo, String mensaje) {
        this(codigo, mensaje, Map.of());
    }

    public ReglaNegocioException(CodigoError codigo, String mensaje, Map<String, Object> detalles) {
        super(codigo, mensaje);
        this.detalles = Map.copyOf(detalles);
    }

    /**
     * Datos estructurados para que el cliente no tenga que interpretar el mensaje.
     */
    public Map<String, Object> getDetalles() {
        return detalles;
    }
}
