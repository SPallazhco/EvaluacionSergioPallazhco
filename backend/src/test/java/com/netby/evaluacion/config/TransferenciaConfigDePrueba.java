package com.netby.evaluacion.config;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;

/**
 * Configuración con los valores del enunciado para pruebas unitarias que no levantan Quarkus.
 */
public final class TransferenciaConfigDePrueba implements TransferenciaConfig {

    public static final ZoneId ZONA_ECUADOR = ZoneId.of("America/Guayaquil");

    @Override
    public ZoneId zonaHoraria() {
        return ZONA_ECUADOR;
    }

    @Override
    public BigDecimal limiteDiario() {
        return new BigDecimal("10000.00");
    }

    @Override
    public Sospecha sospecha() {
        return new Sospecha() {
            @Override
            public BigDecimal montoMaximo() {
                return new BigDecimal("5000.00");
            }

            @Override
            public int maximoTransferenciasEnVentana() {
                return 5;
            }

            @Override
            public Duration ventana() {
                return Duration.ofHours(1);
            }
        };
    }
}
