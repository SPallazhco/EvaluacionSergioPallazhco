package com.netby.evaluacion.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransferenciaTest {

    private static final Instant FECHA = Instant.parse("2026-10-24T15:30:00Z");
    private static final BigDecimal MONTO = new BigDecimal("250.00");

    private final Cuenta origen = new Cuenta("1111111111", "Ana Pérez", TipoCuenta.AHORRO,
            new BigDecimal("1000.00"), EstadoCuenta.ACTIVA);
    private final Cuenta destino = new Cuenta("2222222222", "Luis Torres", TipoCuenta.CORRIENTE,
            BigDecimal.ZERO, EstadoCuenta.ACTIVA);

    @Test
    void completadaNoDebeSerSospechosaNiTenerMotivo() {
        Transferencia transferencia = Transferencia.completada(origen, destino, MONTO, FECHA);

        assertThat(transferencia.getEstado()).isEqualTo(EstadoTransferencia.COMPLETADA);
        assertThat(transferencia.esSospechosa()).isFalse();
        assertThat(transferencia.getMotivo()).isNull();
        assertThat(transferencia.getCuentaOrigen()).isSameAs(origen);
        assertThat(transferencia.getCuentaDestino()).isSameAs(destino);
        assertThat(transferencia.getMonto()).isEqualByComparingTo(MONTO);
        assertThat(transferencia.getFecha()).isEqualTo(FECHA);
    }

    @Test
    void enRevisionDebeSerSospechosaYConservarElMotivo() {
        Transferencia transferencia = Transferencia.enRevision(origen, destino, MONTO, FECHA,
                "Monto superior a 5000.00");

        assertThat(transferencia.getEstado()).isEqualTo(EstadoTransferencia.EN_REVISION);
        assertThat(transferencia.esSospechosa()).isTrue();
        assertThat(transferencia.getMotivo()).isEqualTo("Monto superior a 5000.00");
    }

    @Test
    void rechazadaNoDebeSerSospechosaYConservarElMotivo() {
        Transferencia transferencia = Transferencia.rechazada(origen, destino, MONTO, FECHA,
                "Saldo insuficiente");

        assertThat(transferencia.getEstado()).isEqualTo(EstadoTransferencia.RECHAZADA);
        assertThat(transferencia.esSospechosa()).isFalse();
        assertThat(transferencia.getMotivo()).isEqualTo("Saldo insuficiente");
    }

    @Test
    void transferenciasSinPersistirSoloSonIgualesASiMismas() {
        Transferencia transferencia = Transferencia.completada(origen, destino, MONTO, FECHA);
        Transferencia otra = Transferencia.completada(origen, destino, MONTO, FECHA);

        assertThat(transferencia).isEqualTo(transferencia)
                .isNotEqualTo(otra)
                .isNotEqualTo(null)
                .isNotEqualTo(MONTO)
                .hasSameHashCodeAs(otra);
    }
}
