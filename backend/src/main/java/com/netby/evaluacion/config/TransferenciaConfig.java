package com.netby.evaluacion.config;

import io.smallrye.config.ConfigMapping;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;

@ConfigMapping(prefix = "transferencias")
public interface TransferenciaConfig {

    ZoneId zonaHoraria();

    BigDecimal limiteDiario();

    Sospecha sospecha();

    interface Sospecha {

        BigDecimal montoMaximo();

        // Con 5 previas en la ventana, la sexta se marca
        int maximoTransferenciasEnVentana();

        Duration ventana();
    }
}
