package com.netby.evaluacion.exception.handler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.dto.response.ProblemaResponse.ErrorCampo;
import com.netby.evaluacion.exception.CodigoError;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Errores al convertir el JSON recibido en el DTO: tipo de dato incorrecto, valor de enum
 * inexistente o propiedad desconocida. Reemplaza el mapper genérico de Quarkus para mantener
 * el formato de error uniforme e indicar el campo afectado.
 */
@Provider
public class JsonInvalidoExceptionMapper implements ExceptionMapper<MismatchedInputException> {

    @Override
    public Response toResponse(MismatchedInputException exception) {
        ErrorCampo error = new ErrorCampo(nombreCampo(exception), mensaje(exception));

        Status status = Status.BAD_REQUEST;
        return RespuestaProblema.construir(ProblemaResponse.conErrores(
                status.getStatusCode(), status.getReasonPhrase(),
                "El cuerpo de la solicitud contiene valores con formato inválido",
                CodigoError.SOLICITUD_INVALIDA, List.of(error)));
    }

    private static String nombreCampo(MismatchedInputException exception) {
        return exception.getPath().stream()
                .map(JsonMappingException.Reference::getFieldName)
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .collect(Collectors.joining("."));
    }

    private static String mensaje(MismatchedInputException exception) {
        if (exception instanceof UnrecognizedPropertyException) {
            return "Propiedad no reconocida";
        }
        Class<?> tipo = exception.getTargetType();
        if (exception instanceof InvalidFormatException && tipo != null && tipo.isEnum()) {
            String permitidos = Arrays.stream(tipo.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return "Valor no permitido. Valores aceptados: " + permitidos;
        }
        return "Tipo de dato inválido";
    }
}
