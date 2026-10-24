package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfig;
import com.netby.evaluacion.exception.LimiteDiarioExcedidoException;
import com.netby.evaluacion.repository.TransferenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Regla 4 / CA-04: ninguna cuenta puede transferir más del límite configurado en un día
 * calendario. El día se determina en la zona horaria de negocio y se consulta como rango UTC.
 */
@ApplicationScoped
public class LimiteDiarioService {

    private final TransferenciaRepository transferenciaRepository;
    private final CalendarioNegocio calendario;
    private final BigDecimal limiteDiario;

    @Inject
    public LimiteDiarioService(TransferenciaRepository transferenciaRepository, CalendarioNegocio calendario,
                               TransferenciaConfig config) {
        this.transferenciaRepository = transferenciaRepository;
        this.calendario = calendario;
        this.limiteDiario = config.limiteDiario();
    }

    public BigDecimal totalTransferidoEnElDia(Long cuentaOrigenId, Instant instante) {
        RangoFechas dia = calendario.rangoDelDia(instante);
        return transferenciaRepository.sumarMontoEjecutadoEntre(cuentaOrigenId, dia.desde(), dia.hasta());
    }

    public BigDecimal montoDisponible(Long cuentaOrigenId, Instant instante) {
        BigDecimal disponible = limiteDiario.subtract(totalTransferidoEnElDia(cuentaOrigenId, instante));
        return disponible.max(BigDecimal.ZERO);
    }

    /**
     * @throws LimiteDiarioExcedidoException si el monto haría superar el límite del día,
     *                                       indicando cuánto queda disponible
     */
    public void validar(Long cuentaOrigenId, BigDecimal monto, Instant instante) {
        BigDecimal disponible = montoDisponible(cuentaOrigenId, instante);
        if (monto.compareTo(disponible) > 0) {
            throw new LimiteDiarioExcedidoException(limiteDiario, disponible);
        }
    }
}
