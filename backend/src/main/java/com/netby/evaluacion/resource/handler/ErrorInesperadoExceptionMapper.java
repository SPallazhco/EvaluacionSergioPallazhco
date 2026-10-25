package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErrorInesperadoExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        Log.error("Error inesperado procesando la solicitud", exception);

        Status status = Status.INTERNAL_SERVER_ERROR;
        return RespuestaProblema.construir(ProblemaResponse.de(
                status.getStatusCode(), status.getReasonPhrase(),
                "Ocurrió un error inesperado. Intente nuevamente más tarde", CodigoError.ERROR_INTERNO));
    }
}
