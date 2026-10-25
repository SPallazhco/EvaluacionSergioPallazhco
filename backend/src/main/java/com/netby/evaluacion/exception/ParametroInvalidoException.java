package com.netby.evaluacion.exception;

import jakarta.ws.rs.BadRequestException;

/**
 * Parámetro de consulta o de ruta con formato inválido. Extiende {@link BadRequestException}
 * porque JAX-RS solo respeta el código de una {@code WebApplicationException} lanzada durante la
 * conversión de parámetros; cualquier otra excepción la convierte en un 404.
 */
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
