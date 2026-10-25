package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import jakarta.ws.rs.core.Response;

final class RespuestaProblema {

    private RespuestaProblema() {
    }

    static Response construir(ProblemaResponse problema) {
        return Response.status(problema.status())
                .type(ProblemaResponse.MEDIA_TYPE)
                .entity(problema)
                .build();
    }
}
