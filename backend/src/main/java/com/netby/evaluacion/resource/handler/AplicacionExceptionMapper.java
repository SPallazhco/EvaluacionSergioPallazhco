package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.AplicacionException;
import com.netby.evaluacion.exception.RecursoDuplicadoException;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.exception.ReglaNegocioException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AplicacionExceptionMapper implements ExceptionMapper<AplicacionException> {

    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(AplicacionException exception) {
        // No se registra el mensaje porque puede incluir datos del cliente
        Log.warnf("Solicitud rechazada por regla de negocio: %s", exception.getCodigo());

        ProblemaResponse problema = switch (exception) {
            case RecursoNoEncontradoException e -> problema(Status.NOT_FOUND, e);
            case RecursoDuplicadoException e -> problema(Status.CONFLICT, e);
            case ReglaNegocioException e -> ProblemaResponse.conDetalles(UNPROCESSABLE_ENTITY,
                    "Unprocessable Entity", e.getMessage(), e.getCodigo(), e.getDetalles());
        };
        return RespuestaProblema.construir(problema);
    }

    private static ProblemaResponse problema(Status status, AplicacionException exception) {
        return ProblemaResponse.de(status.getStatusCode(), status.getReasonPhrase(),
                exception.getMessage(), exception.getCodigo());
    }
}
