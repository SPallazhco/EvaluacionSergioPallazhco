package com.netby.evaluacion.exception.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import jakarta.ws.rs.core.Response;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cubre los mappers cuyos escenarios son difíciles de provocar desde una prueba HTTP
 * (condiciones de carrera en base de datos y fallos inesperados).
 */
class ExceptionMappersTest {

    @Test
    void conflictoEnBaseDeDatosDebeTraducirseA409SinExponerDetallesInternos() {
        ConstraintViolationException exception = new ConstraintViolationException(
                "Duplicate entry", new SQLException("Duplicate entry '1234567890'"), "uk_cuenta_numero_cuenta");

        Response respuesta = new ConflictoDatosExceptionMapper().toResponse(exception);

        ProblemaResponse problema = (ProblemaResponse) respuesta.getEntity();
        assertThat(respuesta.getStatus()).isEqualTo(409);
        assertThat(respuesta.getMediaType()).hasToString(ProblemaResponse.MEDIA_TYPE);
        assertThat(problema.codigo()).isEqualTo(CodigoError.CONFLICTO_DE_DATOS);
        assertThat(problema.detail()).doesNotContain("1234567890", "uk_cuenta");
    }

    @Test
    void errorInesperadoDebeTraducirseA500ConMensajeGenerico() {
        Response respuesta = new ErrorInesperadoExceptionMapper()
                .toResponse(new IllegalStateException("detalle interno sensible"));

        ProblemaResponse problema = (ProblemaResponse) respuesta.getEntity();
        assertThat(respuesta.getStatus()).isEqualTo(500);
        assertThat(problema.codigo()).isEqualTo(CodigoError.ERROR_INTERNO);
        assertThat(problema.detail()).doesNotContain("detalle interno sensible");
    }
}
