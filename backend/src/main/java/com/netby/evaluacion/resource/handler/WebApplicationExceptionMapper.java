package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Unifica el formato de los errores que genera el propio framework (ruta inexistente,
 * método no permitido, JSON mal formado, etc.) conservando su código HTTP.
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Override
    public Response toResponse(WebApplicationException exception) {
        int codigoHttp = exception.getResponse().getStatus();
        Status status = Status.fromStatusCode(codigoHttp);
        String titulo = status != null ? status.getReasonPhrase() : "Error HTTP";

        return RespuestaProblema.construir(ProblemaResponse.de(
                codigoHttp, titulo, mensajePara(codigoHttp), codigoPara(codigoHttp)));
    }

    private static String mensajePara(int codigoHttp) {
        return switch (codigoHttp) {
            case 400 -> "La solicitud no tiene un formato válido";
            case 404 -> "El recurso solicitado no existe";
            case 405 -> "Método HTTP no permitido para este recurso";
            case 415 -> "Tipo de contenido no soportado";
            default -> "No se pudo procesar la solicitud";
        };
    }

    private static CodigoError codigoPara(int codigoHttp) {
        return switch (codigoHttp) {
            case 400 -> CodigoError.SOLICITUD_INVALIDA;
            case 404 -> CodigoError.RECURSO_NO_ENCONTRADO;
            default -> CodigoError.ERROR_HTTP;
        };
    }
}
