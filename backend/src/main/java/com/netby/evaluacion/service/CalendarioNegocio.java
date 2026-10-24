package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Centraliza el manejo del tiempo: la hora actual (desde un {@link Clock} inyectable) y la
 * conversión de un instante UTC al día calendario de la zona horaria de negocio.
 */
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

    /**
     * Rango UTC que corresponde al día calendario (en la zona de negocio) que contiene el instante.
     */
    public RangoFechas rangoDelDia(Instant instante) {
        LocalDate dia = instante.atZone(zonaHoraria).toLocalDate();
        return new RangoFechas(
                dia.atStartOfDay(zonaHoraria).toInstant(),
                dia.plusDays(1).atStartOfDay(zonaHoraria).toInstant());
    }
}
