package com.netby.evaluacion.config;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprueba que la configuración real de la aplicación refleja los valores del enunciado.
 */
@QuarkusTest
class TransferenciaConfigTest {

    @Inject
    TransferenciaConfig config;

    @Test
    void debeCargarLosParametrosDeNegocioDelEnunciado() {
        assertThat(config.zonaHoraria()).isEqualTo(ZoneId.of("America/Guayaquil"));
        assertThat(config.limiteDiario()).isEqualByComparingTo("10000");
        assertThat(config.sospecha().montoMaximo()).isEqualByComparingTo("5000");
        assertThat(config.sospecha().maximoTransferenciasEnVentana()).isEqualTo(5);
        assertThat(config.sospecha().ventana()).isEqualTo(Duration.ofHours(1));
    }
}
