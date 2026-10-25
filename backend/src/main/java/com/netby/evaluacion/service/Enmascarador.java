package com.netby.evaluacion.service;

public final class Enmascarador {

    private static final int DIGITOS_VISIBLES = 4;

    private Enmascarador() {
    }

    public static String numeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() <= DIGITOS_VISIBLES) {
            return "****";
        }
        int ocultos = numeroCuenta.length() - DIGITOS_VISIBLES;
        return "*".repeat(ocultos) + numeroCuenta.substring(ocultos);
    }
}
