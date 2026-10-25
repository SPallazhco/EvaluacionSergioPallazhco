package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfigDePrueba;
import com.netby.evaluacion.dto.request.CrearTransferenciaRequest;
import com.netby.evaluacion.dto.response.TransferenciaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.entity.TipoCuenta;
import com.netby.evaluacion.entity.Transferencia;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.LimiteDiarioExcedidoException;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.exception.ReglaNegocioException;
import com.netby.evaluacion.mapper.TransferenciaMapperImpl;
import com.netby.evaluacion.repository.CuentaRepository;
import com.netby.evaluacion.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferenciaServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-24T15:00:00Z");
    private static final String NUMERO_ORIGEN = "2222222222";
    private static final String NUMERO_DESTINO = "1111111111";

    @Mock
    private CuentaRepository cuentaRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;
    @Mock
    private DeteccionSospechosaService deteccionSospechosaService;
    @Mock
    private LimiteDiarioService limiteDiarioService;

    private TransferenciaService servicio;

    @BeforeEach
    void setUp() {
        CalendarioNegocio calendario = new CalendarioNegocio(
                Clock.fixed(AHORA, ZoneOffset.UTC), new TransferenciaConfigDePrueba());
        servicio = new TransferenciaService(cuentaRepository, transferenciaRepository, deteccionSospechosaService,
                limiteDiarioService, calendario, new TransferenciaMapperImpl());
    }

    private static Cuenta cuenta(String numero, String saldo, EstadoCuenta estado) {
        return new Cuenta(numero, "Titular " + numero, TipoCuenta.AHORRO, new BigDecimal(saldo), estado);
    }

    private void existen(Cuenta origen, Cuenta destino) {
        when(cuentaRepository.buscarPorNumeroParaActualizar(origen.getNumeroCuenta())).thenReturn(Optional.of(origen));
        when(cuentaRepository.buscarPorNumeroParaActualizar(destino.getNumeroCuenta())).thenReturn(Optional.of(destino));
    }

    private static CrearTransferenciaRequest solicitud(String monto) {
        return new CrearTransferenciaRequest(NUMERO_ORIGEN, NUMERO_DESTINO, new BigDecimal(monto));
    }

    private Transferencia transferenciaPersistida() {
        ArgumentCaptor<Transferencia> captor = ArgumentCaptor.forClass(Transferencia.class);
        verify(transferenciaRepository).persist(captor.capture());
        return captor.getValue();
    }

    @Test
    void debeDebitarAcreditarYRegistrarUnaTransferenciaCompletada() {
        Cuenta origen = cuenta(NUMERO_ORIGEN, "1000.00", EstadoCuenta.ACTIVA);
        Cuenta destino = cuenta(NUMERO_DESTINO, "50.00", EstadoCuenta.ACTIVA);
        existen(origen, destino);
        when(deteccionSospechosaService.evaluar(any(), any(), any())).thenReturn(new ResultadoDeteccion(List.of()));

        TransferenciaResponse respuesta = servicio.ejecutar(solicitud("300.00"));

        assertThat(origen.getSaldo()).isEqualByComparingTo("700.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("350.00");
        Transferencia registrada = transferenciaPersistida();
        assertThat(registrada.getEstado()).isEqualTo(EstadoTransferencia.COMPLETADA);
        assertThat(registrada.getFecha()).isEqualTo(AHORA);
        assertThat(respuesta.estado()).isEqualTo(EstadoTransferencia.COMPLETADA);
        assertThat(respuesta.numeroCuentaOrigen()).isEqualTo(NUMERO_ORIGEN);
        assertThat(respuesta.numeroCuentaDestino()).isEqualTo(NUMERO_DESTINO);
    }

    @Test
    void unaTransferenciaSospechosaDebeEjecutarseYQuedarEnRevisionConSuMotivo() {
        Cuenta origen = cuenta(NUMERO_ORIGEN, "9000.00", EstadoCuenta.ACTIVA);
        Cuenta destino = cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.ACTIVA);
        existen(origen, destino);
        when(deteccionSospechosaService.evaluar(any(), any(), any()))
                .thenReturn(new ResultadoDeteccion(List.of("Monto superior a 5000.00")));

        TransferenciaResponse respuesta = servicio.ejecutar(solicitud("6000.00"));

        assertThat(origen.getSaldo()).isEqualByComparingTo("3000.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("6000.00");
        assertThat(respuesta.estado()).isEqualTo(EstadoTransferencia.EN_REVISION);
        assertThat(respuesta.motivo()).isEqualTo("Monto superior a 5000.00");
    }

    @Test
    void debeBloquearLasCuentasSiempreEnOrdenDeNumeroParaEvitarDeadlocks() {
        Cuenta origen = cuenta(NUMERO_ORIGEN, "1000.00", EstadoCuenta.ACTIVA);
        Cuenta destino = cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.ACTIVA);
        existen(origen, destino);
        when(deteccionSospechosaService.evaluar(any(), any(), any())).thenReturn(new ResultadoDeteccion(List.of()));

        servicio.ejecutar(solicitud("10.00"));

        // El origen (222...) es mayor que el destino (111...): se bloquea primero el destino
        InOrder orden = inOrder(cuentaRepository);
        orden.verify(cuentaRepository).buscarPorNumeroParaActualizar(NUMERO_DESTINO);
        orden.verify(cuentaRepository).buscarPorNumeroParaActualizar(NUMERO_ORIGEN);
    }

    @Test
    void conSaldoInsuficienteDebeRegistrarElRechazoSinModificarSaldos() {
        Cuenta origen = cuenta(NUMERO_ORIGEN, "100.00", EstadoCuenta.ACTIVA);
        Cuenta destino = cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.ACTIVA);
        existen(origen, destino);
        CrearTransferenciaRequest request = solicitud("100.01");

        assertThatThrownBy(() -> servicio.ejecutar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .extracting("codigo").isEqualTo(CodigoError.SALDO_INSUFICIENTE);

        assertThat(origen.getSaldo()).isEqualByComparingTo("100.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("0.00");
        Transferencia rechazada = transferenciaPersistida();
        assertThat(rechazada.getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
        assertThat(rechazada.getMotivo()).isEqualTo("La cuenta origen no tiene saldo suficiente");
        verifyNoInteractions(deteccionSospechosaService);
    }

    @Test
    void conCuentaOrigenInactivaDebeRegistrarElRechazo() {
        existen(cuenta(NUMERO_ORIGEN, "1000.00", EstadoCuenta.INACTIVA),
                cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.ACTIVA));
        CrearTransferenciaRequest request = solicitud("10.00");

        assertThatThrownBy(() -> servicio.ejecutar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La cuenta origen no está activa");
        assertThat(transferenciaPersistida().getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
    }

    @Test
    void conCuentaDestinoInactivaDebeRegistrarElRechazo() {
        existen(cuenta(NUMERO_ORIGEN, "1000.00", EstadoCuenta.ACTIVA),
                cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.INACTIVA));
        CrearTransferenciaRequest request = solicitud("10.00");

        assertThatThrownBy(() -> servicio.ejecutar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessage("La cuenta destino no está activa")
                .extracting("codigo").isEqualTo(CodigoError.CUENTA_INACTIVA);
        assertThat(transferenciaPersistida().getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
    }

    @Test
    void alExcederElLimiteDiarioDebeRegistrarElRechazoYPropagarElMontoDisponible() {
        Cuenta origen = cuenta(NUMERO_ORIGEN, "50000.00", EstadoCuenta.ACTIVA);
        existen(origen, cuenta(NUMERO_DESTINO, "0.00", EstadoCuenta.ACTIVA));
        doThrow(new LimiteDiarioExcedidoException(new BigDecimal("10000.00"), new BigDecimal("500.00")))
                .when(limiteDiarioService).validar(any(), any(), any());
        CrearTransferenciaRequest request = solicitud("600.00");

        assertThatThrownBy(() -> servicio.ejecutar(request))
                .isInstanceOfSatisfying(LimiteDiarioExcedidoException.class,
                        e -> assertThat(e.getMontoDisponible()).isEqualByComparingTo("500.00"));
        assertThat(origen.getSaldo()).isEqualByComparingTo("50000.00");
        assertThat(transferenciaPersistida().getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
    }

    @Test
    void siUnaCuentaNoExisteDebeResponderNoEncontradaSinRegistrarNada() {
        when(cuentaRepository.buscarPorNumeroParaActualizar(NUMERO_DESTINO)).thenReturn(Optional.empty());
        CrearTransferenciaRequest request = solicitud("10.00");

        assertThatThrownBy(() -> servicio.ejecutar(request))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("No existe la cuenta " + NUMERO_DESTINO);
        verify(transferenciaRepository, never()).persist(any(Transferencia.class));
    }
}
