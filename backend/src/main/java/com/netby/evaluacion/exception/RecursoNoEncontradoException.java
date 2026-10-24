package com.netby.evaluacion.exception;

public final class RecursoNoEncontradoException extends AplicacionException {

    public RecursoNoEncontradoException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
