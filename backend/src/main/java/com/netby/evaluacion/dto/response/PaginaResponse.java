package com.netby.evaluacion.dto.response;

import java.util.List;

public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {

    public PaginaResponse {
        contenido = List.copyOf(contenido);
    }
}
