package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfigDePrueba;
import com.netby.evaluacion.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeteccionSospechosaServiceTest {

    private static final Long CUENTA_ID = 1L;
    private static final Instant AHORA = Instant.parse("2026-10-24T15:00:00Z");

    @Mock
    private TransferenciaRepository transferenciaRepository;

    private DeteccionSospechosaService servicio;

    @BeforeEach
    void setUp() {
        servicio = new DeteccionSospechosaService(transferenciaRepository, new TransferenciaConfigDePrueba());
    }

    private void transferenciasPreviasEnLaUltimaHora(long cantidad) {
        when(transferenciaRepository.contarEjecutadasDesde(anyLong(), any(Instant.class))).thenReturn(cantidad);
    }

    @ParameterizedTest(name = "monto {0} -> sospechosa: {1}")
    @CsvSource({
            "100.00,  false",
            "5000.00, false",
            "5000.01, true",
            "9999.99, true"
    })
    void debeMarcarSoloLosMontosEstrictamenteMayoresAlUmbral(String monto, boolean esperado) {
        transferenciasPreviasEnLaUltimaHora(0);

        ResultadoDeteccion resultado = servicio.evaluar(CUENTA_ID, new BigDecimal(monto), AHORA);

        assertThat(resultado.esSospechosa()).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "{0} previas -> sospechosa: {1}")
    @CsvSource({
            "0, false",
            "4, false",
            "5, true",
            "8, true"
    })
    void debeMarcarLaSextaTransferenciaDentroDeLaUltimaHora(long previas, boolean esperado) {
        transferenciasPreviasEnLaUltimaHora(previas);

        ResultadoDeteccion resultado = servicio.evaluar(CUENTA_ID, new BigDecimal("10.00"), AHORA);

        assertThat(resultado.esSospechosa()).isEqualTo(esperado);
    }

    @Test
    void debeContarDesdeUnaHoraAntesDelMomentoDeLaTransferencia() {
        transferenciasPreviasEnLaUltimaHora(0);

        servicio.evaluar(CUENTA_ID, BigDecimal.TEN, AHORA);

        verify(transferenciaRepository).contarEjecutadasDesde(CUENTA_ID, Instant.parse("2026-10-24T14:00:00Z"));
    }

    @Test
    void debeRegistrarTodosLosMotivosCuandoSeCumplenAmbasCondiciones() {
        transferenciasPreviasEnLaUltimaHora(5);

        ResultadoDeteccion resultado = servicio.evaluar(CUENTA_ID, new BigDecimal("6000.00"), AHORA);

        assertThat(resultado.motivos()).containsExactly(
                "Monto superior a 5000.00",
                "Más de 5 transferencias en los últimos 60 minutos");
        assertThat(resultado.descripcion())
                .isEqualTo("Monto superior a 5000.00; Más de 5 transferencias en los últimos 60 minutos");
    }

    @Test
    void unaTransferenciaNormalNoDebeTenerMotivos() {
        transferenciasPreviasEnLaUltimaHora(1);

        ResultadoDeteccion resultado = servicio.evaluar(CUENTA_ID, new BigDecimal("250.00"), AHORA);

        assertThat(resultado.esSospechosa()).isFalse();
        assertThat(resultado.motivos()).isEmpty();
    }
}
