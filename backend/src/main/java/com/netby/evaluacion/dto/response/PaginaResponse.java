package com.netby.evaluacion.dto.response;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Página de resultados")
public record PaginaResponse<T>(
        @Schema(description = "Elementos de la página")
        List<T> contenido,
        @Schema(description = "Número de página (desde 0)", example = "0")
        int pagina,
        @Schema(description = "Tamaño de página solicitado", example = "20")
        int tamanio,
        @Schema(description = "Total de elementos que cumplen la consulta", example = "42")
        long totalElementos,
        @Schema(description = "Total de páginas", example = "3")
        int totalPaginas
) {

    public PaginaResponse {
        contenido = List.copyOf(contenido);
    }
}
