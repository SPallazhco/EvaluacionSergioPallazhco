package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.dto.response.ProblemaResponse.ErrorCampo;
import com.netby.evaluacion.exception.CodigoError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Comparator;
import java.util.List;

@Provider
public class ValidacionExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<ErrorCampo> errores = exception.getConstraintViolations().stream()
                .map(violacion -> new ErrorCampo(nombreCampo(violacion), violacion.getMessage()))
                .sorted(Comparator.comparing(ErrorCampo::campo).thenComparing(ErrorCampo::mensaje))
                .toList();

        Status status = Status.BAD_REQUEST;
        return RespuestaProblema.construir(ProblemaResponse.conErrores(
                status.getStatusCode(), status.getReasonPhrase(),
                "La solicitud contiene datos inválidos", CodigoError.SOLICITUD_INVALIDA, errores));
    }

    private static String nombreCampo(ConstraintViolation<?> violacion) {
        String campo = null;
        for (Path.Node nodo : violacion.getPropertyPath()) {
            campo = nodo.getName();
        }
        return campo;
    }
}
