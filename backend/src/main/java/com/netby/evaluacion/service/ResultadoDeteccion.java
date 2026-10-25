package com.netby.evaluacion.service;

import java.util.List;

public record ResultadoDeteccion(List<String> motivos) {

    public ResultadoDeteccion {
        motivos = List.copyOf(motivos);
    }

    public boolean esSospechosa() {
        return !motivos.isEmpty();
    }

    public String descripcion() {
        return String.join("; ", motivos);
    }
}
