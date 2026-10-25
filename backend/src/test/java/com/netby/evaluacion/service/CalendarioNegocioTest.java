package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfigDePrueba;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CalendarioNegocioTest {

    private static final Instant MOMENTO = Instant.parse("2026-10-24T15:00:00Z");

    private final CalendarioNegocio calendario = new CalendarioNegocio(
            Clock.fixed(MOMENTO, ZoneOffset.UTC), new TransferenciaConfigDePrueba());

    @Test
    void ahoraDebeProvenirDelRelojInyectado() {
        assertThat(calendario.ahora()).isEqualTo(MOMENTO);
    }

    @Test
    void rangoDelDiaDebeCorresponderAlDiaCalendarioDeEcuador() {
        // 10:00 en Ecuador (UTC-5)
        RangoFechas rango = calendario.rangoDelDia(Instant.parse("2026-10-24T15:00:00Z"));

        assertThat(rango.desde()).isEqualTo(Instant.parse("2026-10-24T05:00:00Z"));
        assertThat(rango.hasta()).isEqualTo(Instant.parse("2026-10-25T05:00:00Z"));
    }

    @Test
    void unaTransferenciaNocturnaEnEcuadorPerteneceAlDiaLocalAunqueEnUtcSeaElDiaSiguiente() {
        // 22:30 del 23 en Ecuador = 03:30 del 24 en UTC
        RangoFechas rango = calendario.rangoDelDia(Instant.parse("2026-10-24T03:30:00Z"));

        assertThat(rango.desde()).isEqualTo(Instant.parse("2026-10-23T05:00:00Z"));
        assertThat(rango.hasta()).isEqualTo(Instant.parse("2026-10-24T05:00:00Z"));
    }

    @Test
    void inicioYFinDeUnDiaCalendarioDebenConvertirseAInstantesUtc() {
        LocalDate dia = LocalDate.of(2026, 10, 24);

        assertThat(calendario.inicioDelDia(dia)).isEqualTo(Instant.parse("2026-10-24T05:00:00Z"));
        assertThat(calendario.finDelDia(dia)).isEqualTo(Instant.parse("2026-10-25T05:00:00Z"));
    }

    @Test
    void laMedianocheLocalDebeIniciarUnNuevoDia() {
        RangoFechas rango = calendario.rangoDelDia(Instant.parse("2026-10-24T05:00:00Z"));

        assertThat(rango.desde()).isEqualTo(Instant.parse("2026-10-24T05:00:00Z"));
    }
}
