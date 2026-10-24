package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;
import com.netby.evaluacion.entity.Transferencia;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Valida las consultas de acumulados contra MySQL real. Cada prueba hace rollback al terminar.
 * Los datos se crean dentro de cada prueba: con {@code @TestTransaction}, un {@code @BeforeEach}
 * se ejecuta en su propia transacción y también hace rollback.
 */
@QuarkusTest
@TestTransaction
class TransferenciaRepositoryTest {

    private static final AtomicLong SECUENCIA = new AtomicLong(System.nanoTime() % 1_000_000_000L);
    private static final Instant INICIO_DIA = Instant.parse("2026-10-24T05:00:00Z");
    private static final Instant FIN_DIA = Instant.parse("2026-10-25T05:00:00Z");

    @Inject
    TransferenciaRepository transferenciaRepository;

    @Inject
    CuentaRepository cuentaRepository;

    private Cuenta origen;
    private Cuenta destino;
    private Cuenta otraCuenta;

    private void crearCuentas() {
        origen = nuevaCuenta();
        destino = nuevaCuenta();
        otraCuenta = nuevaCuenta();
    }

    private Cuenta nuevaCuenta() {
        Cuenta cuenta = new Cuenta(String.format("%010d", SECUENCIA.incrementAndGet()), "Titular de prueba",
                TipoCuenta.AHORRO, new BigDecimal("50000.00"), EstadoCuenta.ACTIVA);
        cuentaRepository.persist(cuenta);
        return cuenta;
    }

    private void completada(Cuenta desde, String monto, String fecha) {
        transferenciaRepository.persist(
                Transferencia.completada(desde, destino, new BigDecimal(monto), Instant.parse(fecha)));
    }

    @Test
    void sumarDebeIncluirCompletadasYEnRevisionDelRangoYExcluirRechazadas() {
        crearCuentas();
        completada(origen, "1000.00", "2026-10-24T05:00:00Z");
        transferenciaRepository.persist(Transferencia.enRevision(origen, destino, new BigDecimal("6000.00"),
                Instant.parse("2026-10-24T20:00:00Z"), "Monto superior a 5000.00"));
        transferenciaRepository.persist(Transferencia.rechazada(origen, destino, new BigDecimal("9999.00"),
                Instant.parse("2026-10-24T21:00:00Z"), "Saldo insuficiente"));

        BigDecimal total = transferenciaRepository.sumarMontoEjecutadoEntre(origen.getId(), INICIO_DIA, FIN_DIA);

        assertThat(total).isEqualByComparingTo("7000.00");
    }

    @Test
    void sumarDebeRespetarElRangoSemiabierto() {
        crearCuentas();
        completada(origen, "100.00", "2026-10-24T04:59:59.999999Z");
        completada(origen, "200.00", "2026-10-24T05:00:00Z");
        completada(origen, "400.00", "2026-10-25T05:00:00Z");

        BigDecimal total = transferenciaRepository.sumarMontoEjecutadoEntre(origen.getId(), INICIO_DIA, FIN_DIA);

        assertThat(total).isEqualByComparingTo("200.00");
    }

    @Test
    void sumarDebeConsiderarSoloLaCuentaOrigenIndicada() {
        crearCuentas();
        completada(otraCuenta, "500.00", "2026-10-24T12:00:00Z");

        BigDecimal total = transferenciaRepository.sumarMontoEjecutadoEntre(origen.getId(), INICIO_DIA, FIN_DIA);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void contarDebeIncluirSoloEjecutadasDeLaCuentaDesdeElInstanteIndicado() {
        crearCuentas();
        Instant desde = Instant.parse("2026-10-24T14:00:00Z");
        completada(origen, "10.00", "2026-10-24T13:59:59Z");
        completada(origen, "10.00", "2026-10-24T14:00:00Z");
        completada(origen, "10.00", "2026-10-24T14:30:00Z");
        transferenciaRepository.persist(Transferencia.rechazada(origen, destino, BigDecimal.TEN,
                Instant.parse("2026-10-24T14:40:00Z"), "Saldo insuficiente"));
        completada(otraCuenta, "10.00", "2026-10-24T14:45:00Z");

        long cantidad = transferenciaRepository.contarEjecutadasDesde(origen.getId(), desde);

        assertThat(cantidad).isEqualTo(2);
    }
}
