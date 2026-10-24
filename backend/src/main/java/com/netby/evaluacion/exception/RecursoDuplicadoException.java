package com.netby.evaluacion.exception;

public class RecursoDuplicadoException extends AplicacionException {

    public RecursoDuplicadoException(CodigoError codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
