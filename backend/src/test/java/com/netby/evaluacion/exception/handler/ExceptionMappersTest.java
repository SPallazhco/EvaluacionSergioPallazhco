package com.netby.evaluacion.exception.handler;

import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.LimiteDiarioExcedidoException;
import jakarta.ws.rs.core.Response;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
    void reglaDeNegocioDebeTraducirseA422ConSusDetalles() {
        LimiteDiarioExcedidoException exception = new LimiteDiarioExcedidoException(
                new BigDecimal("10000.00"), new BigDecimal("1500.00"));

        Response respuesta = new AplicacionExceptionMapper().toResponse(exception);

        ProblemaResponse problema = (ProblemaResponse) respuesta.getEntity();
        assertThat(respuesta.getStatus()).isEqualTo(422);
        assertThat(problema.codigo()).isEqualTo(CodigoError.LIMITE_DIARIO_EXCEDIDO);
        assertThat(problema.detail()).contains("Monto disponible hoy: 1500.00");
        assertThat(problema.detalles()).containsEntry("montoDisponible", new BigDecimal("1500.00"));
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
