package com.netby.evaluacion.exception;

/**
 * Base de las excepciones de negocio. Cada subtipo representa una categoría de error
 * que la capa REST traduce a un código HTTP; los servicios no conocen HTTP.
 */
public abstract class AplicacionException extends RuntimeException {

    private final CodigoError codigo;

    protected AplicacionException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
