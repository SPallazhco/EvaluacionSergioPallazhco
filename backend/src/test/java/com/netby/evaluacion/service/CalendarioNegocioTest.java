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
        // 24/10 a las 10:00 en Ecuador (UTC-5) -> el día va de 05:00Z del 24 a 05:00Z del 25
        RangoFechas rango = calendario.rangoDelDia(Instant.parse("2026-10-24T15:00:00Z"));

        assertThat(rango.desde()).isEqualTo(Instant.parse("2026-10-24T05:00:00Z"));
        assertThat(rango.hasta()).isEqualTo(Instant.parse("2026-10-25T05:00:00Z"));
    }

    @Test
    void unaTransferenciaNocturnaEnEcuadorPerteneceAlDiaLocalAunqueEnUtcSeaElDiaSiguiente() {
        // 23/10 a las 22:30 en Ecuador = 24/10 a las 03:30 UTC: pertenece al día 23
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
