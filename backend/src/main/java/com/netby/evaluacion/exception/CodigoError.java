package com.netby.evaluacion.exception;

/**
 * Códigos estables que el cliente puede usar para distinguir errores sin depender del mensaje.
 */
public enum CodigoError {
    SOLICITUD_INVALIDA,
    RECURSO_NO_ENCONTRADO,
    CONFLICTO_DE_DATOS,
    ERROR_HTTP,
    ERROR_INTERNO,
    CUENTA_DUPLICADA,
    CUENTA_NO_ENCONTRADA,
    CUENTA_INACTIVA,
    SALDO_INSUFICIENTE,
    LIMITE_DIARIO_EXCEDIDO,
    TRANSFERENCIA_NO_ENCONTRADA
}
