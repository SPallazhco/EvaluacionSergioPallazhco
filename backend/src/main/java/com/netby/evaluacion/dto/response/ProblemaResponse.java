package com.netby.evaluacion.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netby.evaluacion.exception.CodigoError;

import java.time.Instant;
import java.util.List;

/**
 * Cuerpo de error según RFC 7807 (Problem Details for HTTP APIs), extendido con
 * un código de negocio estable, la fecha del error y el detalle por campo.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProblemaResponse(
        String type,
        String title,
        int status,
        String detail,
        CodigoError codigo,
        Instant timestamp,
        List<ErrorCampo> errores
) {

    public static final String MEDIA_TYPE = "application/problem+json";

    private static final String TIPO_GENERICO = "about:blank";

    public ProblemaResponse {
        errores = errores == null ? null : List.copyOf(errores);
    }

    public static ProblemaResponse de(int status, String titulo, String detalle, CodigoError codigo) {
        return new ProblemaResponse(TIPO_GENERICO, titulo, status, detalle, codigo, Instant.now(), null);
    }

    public static ProblemaResponse conErrores(int status, String titulo, String detalle, CodigoError codigo,
                                              List<ErrorCampo> errores) {
        return new ProblemaResponse(TIPO_GENERICO, titulo, status, detalle, codigo, Instant.now(), errores);
    }

    public record ErrorCampo(String campo, String mensaje) {
    }
}
