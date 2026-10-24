package com.netby.evaluacion.entity;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica el mapeo de {@link Transferencia} contra MySQL real: relaciones, almacenamiento
 * de fechas en UTC y las restricciones definidas en la migración. Cada prueba hace rollback.
 */
@QuarkusTest
class TransferenciaPersistenciaTest {

    private static final AtomicLong SECUENCIA = new AtomicLong(System.nanoTime() % 1_000_000_000L);

    @Inject
    EntityManager entityManager;

    private Cuenta nuevaCuenta() {
        Cuenta cuenta = new Cuenta(String.format("%010d", SECUENCIA.incrementAndGet()), "Titular de prueba",
                TipoCuenta.AHORRO, new BigDecimal("1000.00"), EstadoCuenta.ACTIVA);
        entityManager.persist(cuenta);
        return cuenta;
    }

    @Test
    @TestTransaction
    void debePersistirLaTransferenciaYGuardarLaFechaEnUtc() {
        Cuenta origen = nuevaCuenta();
        Cuenta destino = nuevaCuenta();
        Instant fecha = Instant.parse("2026-10-24T03:30:00.123456Z");

        Transferencia transferencia = Transferencia.enRevision(origen, destino, new BigDecimal("5000.01"),
                fecha, "Monto superior a 5000.00");
        entityManager.persist(transferencia);
        entityManager.flush();
        entityManager.clear();

        Transferencia leida = entityManager.find(Transferencia.class, transferencia.getId());
        assertThat(leida.getEstado()).isEqualTo(EstadoTransferencia.EN_REVISION);
        assertThat(leida.getMonto()).isEqualByComparingTo("5000.01");
        assertThat(leida.getFecha()).isEqualTo(fecha);
        assertThat(leida.getCuentaOrigen().getId()).isEqualTo(origen.getId());
        assertThat(leida.getCuentaDestino().getId()).isEqualTo(destino.getId());

        // Las 22:30 del 23 en Ecuador son las 03:30 del 24 en UTC: la columna debe guardar UTC.
        // MySQL formatea el valor para leerlo tal cual está almacenado, sin conversiones del driver.
        Object valorEnColumna = entityManager
                .createNativeQuery("SELECT DATE_FORMAT(fecha, '%Y-%m-%d %H:%i:%s') FROM transferencia WHERE id = :id")
                .setParameter("id", transferencia.getId())
                .getSingleResult();
        assertThat(valorEnColumna).hasToString("2026-10-24 03:30:00");
    }

    @Test
    @TestTransaction
    void laBaseDeDatosDebeRechazarUnaTransferenciaHaciaLaMismaCuenta() {
        Cuenta cuenta = nuevaCuenta();
        Transferencia invalida = Transferencia.completada(cuenta, cuenta, BigDecimal.TEN, Instant.now());

        assertThatThrownBy(() -> {
            entityManager.persist(invalida);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    @TestTransaction
    void laBaseDeDatosDebeRechazarUnMontoNoPositivo() {
        Cuenta origen = nuevaCuenta();
        Cuenta destino = nuevaCuenta();
        Transferencia invalida = Transferencia.completada(origen, destino, BigDecimal.ZERO, Instant.now());

        assertThatThrownBy(() -> {
            entityManager.persist(invalida);
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }
}
