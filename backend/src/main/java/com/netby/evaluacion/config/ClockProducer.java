package com.netby.evaluacion.config;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import java.time.Clock;

public class ClockProducer {

    @Produces
    @Singleton
    Clock reloj() {
        return Clock.systemUTC();
    }
}
