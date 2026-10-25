package com.netby.evaluacion.service;

/**
 * Oculta datos sensibles antes de escribirlos en los logs.
 */
public final class Enmascarador {

    private static final int DIGITOS_VISIBLES = 4;

    private Enmascarador() {
    }

    /**
     * Deja visibles solo los últimos 4 dígitos: {@code 1234567890 -> ******7890}.
     */
    public static String numeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() <= DIGITOS_VISIBLES) {
            return "****";
        }
        int ocultos = numeroCuenta.length() - DIGITOS_VISIBLES;
        return "*".repeat(ocultos) + numeroCuenta.substring(ocultos);
    }
}
