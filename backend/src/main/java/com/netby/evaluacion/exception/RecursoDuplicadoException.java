package com.netby.evaluacion.exception;

public final class RecursoDuplicadoException extends AplicacionException {

    public RecursoDuplicadoException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
