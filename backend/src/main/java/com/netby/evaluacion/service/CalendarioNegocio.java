package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@ApplicationScoped
public class CalendarioNegocio {

    private final Clock reloj;
    private final ZoneId zonaHoraria;

    @Inject
    public CalendarioNegocio(Clock reloj, TransferenciaConfig config) {
        this.reloj = reloj;
        this.zonaHoraria = config.zonaHoraria();
    }

    public Instant ahora() {
        return reloj.instant();
    }

    public RangoFechas rangoDelDia(Instant instante) {
        LocalDate dia = instante.atZone(zonaHoraria).toLocalDate();
        return new RangoFechas(inicioDelDia(dia), finDelDia(dia));
    }

    public Instant inicioDelDia(LocalDate dia) {
        return dia.atStartOfDay(zonaHoraria).toInstant();
    }

    public Instant finDelDia(LocalDate dia) {
        return dia.plusDays(1).atStartOfDay(zonaHoraria).toInstant();
    }
}
