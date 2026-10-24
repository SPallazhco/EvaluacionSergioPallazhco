package com.netby.evaluacion.config;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

/**
 * Expone el reloj del sistema como dependencia inyectable. Ninguna clase llama a
 * {@code Instant.now()} directamente: así las reglas basadas en tiempo se prueban con un reloj fijo.
 */
public class ClockProducer {

    @Produces
    @Singleton
    Clock reloj() {
        return Clock.systemUTC();
    }
}
