package com.netby.evaluacion.config;

import io.smallrye.config.ConfigMapping;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;

/**
 * Parámetros de las reglas de negocio de transferencias, definidos en {@code application.properties}.
 * Se externalizan para poder ajustarlos por entorno sin recompilar. No tienen valor por defecto:
 * si falta alguno la aplicación no arranca, en lugar de operar con un límite equivocado.
 */
@ConfigMapping(prefix = "transferencias")
public interface TransferenciaConfig {

    /**
     * Zona horaria con la que se determina el "día calendario" del límite diario.
     */
    ZoneId zonaHoraria();

    /**
     * Monto acumulado máximo que una cuenta puede transferir en un día calendario.
     */
    BigDecimal limiteDiario();

    Sospecha sospecha();

    interface Sospecha {

        /**
         * Una transferencia con un monto mayor a este valor se marca para revisión.
         */
        BigDecimal montoMaximo();

        /**
         * Cantidad de transferencias previas en la ventana a partir de la cual la siguiente
         * se marca para revisión ("más de 5 en la última hora" = la sexta en adelante).
         */
        int maximoTransferenciasEnVentana();

        /**
         * Ventana de tiempo móvil en la que se cuentan las transferencias previas.
         */
        Duration ventana();
    }
}
