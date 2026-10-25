package com.netby.evaluacion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netby.evaluacion.exception.CodigoError;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Error en formato RFC 7807 (application/problem+json)")
public record ProblemaResponse(
        @Schema(description = "Tipo de problema (RFC 7807)", example = "about:blank")
        String type,
        @Schema(description = "Descripción breve del código HTTP", example = "Unprocessable Entity")
        String title,
        @Schema(description = "Código HTTP", example = "422")
        int status,
        @Schema(description = "Explicación legible del error", example = "La cuenta origen no tiene saldo suficiente")
        String detail,
        @Schema(description = "Código de error estable para el cliente", example = "SALDO_INSUFICIENTE")
        CodigoError codigo,
        @Schema(description = "Momento del error en UTC", example = "2026-10-24T15:30:00Z")
        Instant timestamp,
        @Schema(description = "Errores por campo (solo en errores de validación)")
        List<ErrorCampo> errores,
        @Schema(description = "Datos adicionales de la regla incumplida (p. ej. montoDisponible)")
        Map<String, Object> detalles
) {

    public static final String MEDIA_TYPE = "application/problem+json";

    private static final String TIPO_GENERICO = "about:blank";

    public ProblemaResponse {
        errores = errores == null ? List.of() : List.copyOf(errores);
        detalles = detalles == null ? Map.of() : Map.copyOf(detalles);
    }

    public static ProblemaResponse de(int status, String titulo, String detalle, CodigoError codigo) {
        return new ProblemaResponse(TIPO_GENERICO, titulo, status, detalle, codigo, Instant.now(), null, null);
    }

    public static ProblemaResponse conErrores(int status, String titulo, String detalle, CodigoError codigo,
                                              List<ErrorCampo> errores) {
        return new ProblemaResponse(TIPO_GENERICO, titulo, status, detalle, codigo, Instant.now(), errores, null);
    }

    public static ProblemaResponse conDetalles(int status, String titulo, String detalle, CodigoError codigo,
                                               Map<String, Object> detalles) {
        return new ProblemaResponse(TIPO_GENERICO, titulo, status, detalle, codigo, Instant.now(), null, detalles);
    }

    @Schema(description = "Error de validación de un campo o parámetro")
    public record ErrorCampo(
            @Schema(description = "Nombre del campo o parámetro", example = "numeroCuenta")
            String campo,
            @Schema(description = "Motivo del error", example = "El número de cuenta debe tener exactamente 10 dígitos")
            String mensaje) {
    }
}
