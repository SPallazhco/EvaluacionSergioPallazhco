package com.netby.evaluacion.exception.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Captura cualquier error no previsto: lo registra completo en el log y responde un mensaje
 * genérico, sin exponer detalles internos (stack trace, SQL, nombres de clases) al cliente.
 */
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
