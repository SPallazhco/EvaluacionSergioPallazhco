package com.netby.evaluacion.exception;

import java.util.Map;

public non-sealed class ReglaNegocioException extends AplicacionException {

    private final transient Map<String, Object> detalles;

    public ReglaNegocioException(CodigoError codigo, String mensaje) {
        this(codigo, mensaje, Map.of());
    }

    public ReglaNegocioException(CodigoError codigo, String mensaje, Map<String, Object> detalles) {
        super(codigo, mensaje);
        this.detalles = Map.copyOf(detalles);
    }

    public Map<String, Object> getDetalles() {
        return detalles;
    }
}
