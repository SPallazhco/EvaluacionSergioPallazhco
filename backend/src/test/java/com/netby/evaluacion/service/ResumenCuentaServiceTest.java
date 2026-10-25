package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfigDePrueba;
import com.netby.evaluacion.dto.response.ResumenCuentaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.entity.TipoCuenta;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.repository.CuentaRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResumenCuentaServiceTest {

    private static final Long CUENTA_ID = 7L;
    private static final Instant AHORA = Instant.parse("2026-10-24T15:00:00Z");
    private static final Instant INICIO_HOY = Instant.parse("2026-10-24T05:00:00Z");
    private static final Instant FIN_HOY = Instant.parse("2026-10-25T05:00:00Z");

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;

    private ResumenCuentaService servicio;

    @BeforeEach
    void setUp() {
        TransferenciaConfigDePrueba config = new TransferenciaConfigDePrueba();
        CalendarioNegocio calendario = new CalendarioNegocio(Clock.fixed(AHORA, ZoneOffset.UTC), config);
        LimiteDiarioService limiteDiario = new LimiteDiarioService(transferenciaRepository, calendario, config);
        servicio = new ResumenCuentaService(cuentaRepository, transferenciaRepository, limiteDiario, calendario);
    }

    @Test
    void debeCalcularElResumenDelDiaEnCurso() {
        Cuenta cuenta = new Cuenta("1234567890", "Ana Pérez", TipoCuenta.AHORRO,
                new BigDecimal("2500.00"), EstadoCuenta.ACTIVA);
        when(cuentaRepository.findByIdOptional(CUENTA_ID)).thenReturn(Optional.of(cuenta));
        when(transferenciaRepository.sumarMontoEjecutadoEntre(CUENTA_ID, INICIO_HOY, FIN_HOY))
                .thenReturn(new BigDecimal("7500.00"));
        when(transferenciaRepository.contarEjecutadasEntre(CUENTA_ID, INICIO_HOY, FIN_HOY)).thenReturn(3L);
        when(transferenciaRepository.contarPorEstadoEntre(CUENTA_ID, EstadoTransferencia.EN_REVISION,
                INICIO_HOY, FIN_HOY)).thenReturn(1L);
        when(transferenciaRepository.contarPorEstado(CUENTA_ID, EstadoTransferencia.EN_REVISION)).thenReturn(4L);

        ResumenCuentaResponse resumen = servicio.obtener(CUENTA_ID);

        assertThat(resumen.numeroCuenta()).isEqualTo("1234567890");
        assertThat(resumen.saldoActual()).isEqualByComparingTo("2500.00");
        assertThat(resumen.totalTransferidoHoy()).isEqualByComparingTo("7500.00");
        assertThat(resumen.montoDisponibleHoy()).isEqualByComparingTo("2500.00");
        assertThat(resumen.cantidadTransferenciasHoy()).isEqualTo(3);
        assertThat(resumen.cantidadSospechosasHoy()).isEqualTo(1);
        assertThat(resumen.cantidadSospechosasTotal()).isEqualTo(4);
    }

    @Test
    void debeFallarSiLaCuentaNoExiste() {
        when(cuentaRepository.findByIdOptional(CUENTA_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.obtener(CUENTA_ID))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .extracting("codigo").isEqualTo(CodigoError.CUENTA_NO_ENCONTRADA);
    }
}
