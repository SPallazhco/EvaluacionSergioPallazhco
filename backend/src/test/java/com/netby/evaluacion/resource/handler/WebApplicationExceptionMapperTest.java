package com.netby.evaluacion.resource.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class WebApplicationExceptionMapperTest {

    private final WebApplicationExceptionMapper mapper = new WebApplicationExceptionMapper();

    @ParameterizedTest(name = "HTTP {0}")
    @CsvSource(delimiter = '|', value = {
            "400 | Bad Request            | SOLICITUD_INVALIDA    | La solicitud no tiene un formato válido",
            "404 | Not Found              | RECURSO_NO_ENCONTRADO | El recurso solicitado no existe",
            "405 | Method Not Allowed     | ERROR_HTTP            | Método HTTP no permitido para este recurso",
            "415 | Unsupported Media Type | ERROR_HTTP            | Tipo de contenido no soportado",
            "503 | Service Unavailable    | ERROR_HTTP            | No se pudo procesar la solicitud",
            "599 | Error HTTP             | ERROR_HTTP            | No se pudo procesar la solicitud"
    })
    void debeConservarElCodigoHttpConFormatoProblema(int codigoHttp, String titulo, CodigoError codigo,
                                                     String detalle) {
        Response respuesta = mapper.toResponse(new WebApplicationException(codigoHttp));

        ProblemaResponse problema = (ProblemaResponse) respuesta.getEntity();
        assertThat(respuesta.getStatus()).isEqualTo(codigoHttp);
        assertThat(problema.title()).isEqualTo(titulo);
        assertThat(problema.codigo()).isEqualTo(codigo);
        assertThat(problema.detail()).isEqualTo(detalle);
    }
}
