package com.netby.evaluacion.exception;

import jakarta.ws.rs.BadRequestException;

// Debe ser WebApplicationException; de lo contrario JAX-RS responde 404
public class ParametroInvalidoException extends BadRequestException {

    private final String parametro;

    public ParametroInvalidoException(String parametro, String mensaje) {
        super(mensaje);
        this.parametro = parametro;
    }

    public String getParametro() {
        return parametro;
    }
}
