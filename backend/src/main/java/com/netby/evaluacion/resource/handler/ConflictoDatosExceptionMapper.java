package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;

/**
 * Último resguardo ante violaciones de restricciones de la base de datos (p. ej. dos registros
 * concurrentes con el mismo número de cuenta que superan la validación previa del servicio).
 */
@Provider
public class ConflictoDatosExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Log.warnf("Violación de restricción en base de datos: %s", exception.getConstraintName());

        Status status = Status.CONFLICT;
        return RespuestaProblema.construir(ProblemaResponse.de(
                status.getStatusCode(), status.getReasonPhrase(),
                "La operación entra en conflicto con datos existentes", CodigoError.CONFLICTO_DE_DATOS));
    }
}
