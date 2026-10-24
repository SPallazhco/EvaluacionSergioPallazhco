package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfigDePrueba;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.LimiteDiarioExcedidoException;
import com.netby.evaluacion.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LimiteDiarioServiceTest {

    private static final Long CUENTA_ID = 1L;
    private static final Instant AHORA = Instant.parse("2026-10-24T15:00:00Z");

    @Mock
    private TransferenciaRepository transferenciaRepository;

    private LimiteDiarioService servicio;

    @BeforeEach
    void setUp() {
        TransferenciaConfigDePrueba config = new TransferenciaConfigDePrueba();
        CalendarioNegocio calendario = new CalendarioNegocio(Clock.fixed(AHORA, ZoneOffset.UTC), config);
        servicio = new LimiteDiarioService(transferenciaRepository, calendario, config);
    }

    private void acumuladoDelDia(String monto) {
        when(transferenciaRepository.sumarMontoEjecutadoEntre(anyLong(), any(Instant.class), any(Instant.class)))
                .thenReturn(new BigDecimal(monto));
    }

    @Test
    void debePermitirTransferirExactamenteElLimiteEnUnDiaSinMovimientos() {
        acumuladoDelDia("0");

        assertThatCode(() -> servicio.validar(CUENTA_ID, new BigDecimal("10000.00"), AHORA))
                .doesNotThrowAnyException();
    }

    @Test
    void debePermitirAlcanzarElLimiteExactoConElAcumulado() {
        acumuladoDelDia("9000.00");

        assertThatCode(() -> servicio.validar(CUENTA_ID, new BigDecimal("1000.00"), AHORA))
                .doesNotThrowAnyException();
    }

    @Test
    void debeRechazarCuandoSuperaElLimiteEInformarElMontoDisponible() {
        acumuladoDelDia("9000.00");
        BigDecimal monto = new BigDecimal("1000.01");

        assertThatThrownBy(() -> servicio.validar(CUENTA_ID, monto, AHORA))
                .isInstanceOfSatisfying(LimiteDiarioExcedidoException.class, e -> {
                    assertThat(e.getCodigo()).isEqualTo(CodigoError.LIMITE_DIARIO_EXCEDIDO);
                    assertThat(e.getMontoDisponible()).isEqualByComparingTo("1000.00");
                    assertThat(e.getMessage()).isEqualTo(
                            "La transferencia supera el límite diario de 10000.00. Monto disponible hoy: 1000.00");
                    assertThat(e.getDetalles()).containsKeys("limiteDiario", "montoDisponible");
                });
    }

    @Test
    void unMontoMayorAlLimiteDebeRechazarseAunSinMovimientosPrevios() {
        acumuladoDelDia("0");
        BigDecimal monto = new BigDecimal("10000.01");

        assertThatThrownBy(() -> servicio.validar(CUENTA_ID, monto, AHORA))
                .isInstanceOf(LimiteDiarioExcedidoException.class);
    }

    @Test
    void elMontoDisponibleNuncaDebeSerNegativo() {
        acumuladoDelDia("10000.00");

        assertThat(servicio.montoDisponible(CUENTA_ID, AHORA)).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void debeSumarLasTransferenciasDelDiaCalendarioDeEcuador() {
        acumuladoDelDia("0");

        servicio.totalTransferidoEnElDia(CUENTA_ID, AHORA);

        verify(transferenciaRepository).sumarMontoEjecutadoEntre(CUENTA_ID,
                Instant.parse("2026-10-24T05:00:00Z"), Instant.parse("2026-10-25T05:00:00Z"));
    }
}
