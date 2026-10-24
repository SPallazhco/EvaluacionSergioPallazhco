package com.netby.evaluacion.service;

import java.time.Instant;

/**
 * Intervalo semiabierto {@code [desde, hasta)}: incluye el inicio y excluye el fin, de modo que
 * dos días consecutivos nunca comparten un instante.
 */
public record RangoFechas(Instant desde, Instant hasta) {
}
