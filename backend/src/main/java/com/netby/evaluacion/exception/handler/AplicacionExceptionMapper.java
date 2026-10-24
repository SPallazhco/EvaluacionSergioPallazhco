package com.netby.evaluacion.exception.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.AplicacionException;
import com.netby.evaluacion.exception.RecursoDuplicadoException;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Traduce las excepciones de negocio a respuestas HTTP. Al ser {@link AplicacionException}
 * una jerarquía sellada, el switch es exhaustivo: una categoría nueva sin mapear no compila.
 */
@Provider
public class AplicacionExceptionMapper implements ExceptionMapper<AplicacionException> {

    @Override
    public Response toResponse(AplicacionException exception) {
        Status status = switch (exception) {
            case RecursoNoEncontradoException e -> Status.NOT_FOUND;
            case RecursoDuplicadoException e -> Status.CONFLICT;
        };

        // El mensaje puede incluir datos del cliente (p. ej. número de cuenta): solo se registra el código
        Log.warnf("Solicitud rechazada por regla de negocio: %s", exception.getCodigo());
        return RespuestaProblema.construir(ProblemaResponse.de(
                status.getStatusCode(), status.getReasonPhrase(), exception.getMessage(), exception.getCodigo()));
    }
}
