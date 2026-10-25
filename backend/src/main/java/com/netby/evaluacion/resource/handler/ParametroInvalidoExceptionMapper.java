package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.dto.response.ProblemaResponse.ErrorCampo;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.ParametroInvalidoException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class ParametroInvalidoExceptionMapper implements ExceptionMapper<ParametroInvalidoException> {

    @Override
    public Response toResponse(ParametroInvalidoException exception) {
        Status status = Status.BAD_REQUEST;
        return RespuestaProblema.construir(ProblemaResponse.conErrores(
                status.getStatusCode(), status.getReasonPhrase(),
                "La solicitud contiene parámetros con formato inválido", CodigoError.SOLICITUD_INVALIDA,
                List.of(new ErrorCampo(exception.getParametro(), exception.getMessage()))));
    }
}
