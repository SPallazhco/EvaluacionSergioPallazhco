package com.netby.evaluacion.service;

import java.util.List;

/**
 * Resultado de evaluar una transferencia: si es sospechosa y por qué motivos.
 */
public record ResultadoDeteccion(List<String> motivos) {

    public ResultadoDeteccion {
        motivos = List.copyOf(motivos);
    }

    public boolean esSospechosa() {
        return !motivos.isEmpty();
    }

    /**
     * Motivos unidos en un solo texto, apto para persistir en la transferencia.
     */
    public String descripcion() {
        return String.join("; ", motivos);
    }
}
