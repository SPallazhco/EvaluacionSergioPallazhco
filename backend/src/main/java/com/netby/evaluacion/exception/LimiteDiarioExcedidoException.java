package com.netby.evaluacion.exception;

import java.math.BigDecimal;
import java.util.Map;

public class LimiteDiarioExcedidoException extends ReglaNegocioException {

    private final BigDecimal montoDisponible;

    public LimiteDiarioExcedidoException(BigDecimal limiteDiario, BigDecimal montoDisponible) {
        super(CodigoError.LIMITE_DIARIO_EXCEDIDO,
                "La transferencia supera el límite diario de " + limiteDiario.toPlainString()
                        + ". Monto disponible hoy: " + montoDisponible.toPlainString(),
                Map.of("limiteDiario", limiteDiario, "montoDisponible", montoDisponible));
        this.montoDisponible = montoDisponible;
    }

    public BigDecimal getMontoDisponible() {
        return montoDisponible;
    }
}
