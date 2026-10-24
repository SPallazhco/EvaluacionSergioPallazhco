package com.netby.evaluacion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netby.evaluacion.exception.CodigoError;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Cuerpo de error según RFC 7807 (Problem Details for HTTP APIs), extendido con
 * un código de negocio estable, la fecha del error, el detalle por campo y datos
 * estructurados propios de cada regla de negocio.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ProblemaResponse(
        String type,
        String title,
        int status,
        String detail,
        CodigoError codigo,
        Instant timestamp,
        List<ErrorCampo> errores,
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

    public record ErrorCampo(String campo, String mensaje) {
    }
}
