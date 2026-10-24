package com.netby.evaluacion.entity;

public enum EstadoTransferencia {

    /** Ejecutada: los saldos de ambas cuentas fueron actualizados. */
    COMPLETADA,

    /** No ejecutada por incumplir una regla de negocio; los saldos no cambiaron. */
    RECHAZADA,

    /** Ejecutada, pero marcada como sospechosa para revisión posterior. */
    EN_REVISION
}
