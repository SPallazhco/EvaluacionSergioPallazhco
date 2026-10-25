package com.netby.evaluacion.exception;

public abstract sealed class AplicacionException extends RuntimeException
        permits RecursoDuplicadoException, RecursoNoEncontradoException, ReglaNegocioException {

    private final CodigoError codigo;

    protected AplicacionException(CodigoError codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public CodigoError getCodigo() {
        return codigo;
    }
}
