package com.netby.evaluacion.service;

import com.netby.evaluacion.config.TransferenciaConfig;
import com.netby.evaluacion.repository.TransferenciaRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Regla 3 / CA-03: una transferencia es sospechosa si su monto supera el umbral configurado
 * o si la cuenta origen ya realizó el máximo de transferencias permitido en la ventana móvil.
 * Ambas condiciones se evalúan siempre para registrar todos los motivos aplicables.
 */
@ApplicationScoped
public class DeteccionSospechosaService {

    private final TransferenciaRepository transferenciaRepository;
    private final TransferenciaConfig.Sospecha config;

    @Inject
    public DeteccionSospechosaService(TransferenciaRepository transferenciaRepository, TransferenciaConfig config) {
        this.transferenciaRepository = transferenciaRepository;
        this.config = config.sospecha();
    }

    public ResultadoDeteccion evaluar(Long cuentaOrigenId, BigDecimal monto, Instant ahora) {
        List<String> motivos = new ArrayList<>();

        if (monto.compareTo(config.montoMaximo()) > 0) {
            motivos.add("Monto superior a " + config.montoMaximo().toPlainString());
        }

        Instant inicioVentana = ahora.minus(config.ventana());
        long previas = transferenciaRepository.contarEjecutadasDesde(cuentaOrigenId, inicioVentana);
        if (previas >= config.maximoTransferenciasEnVentana()) {
            motivos.add("Más de " + config.maximoTransferenciasEnVentana()
                    + " transferencias en los últimos " + config.ventana().toMinutes() + " minutos");
        }

        ResultadoDeteccion resultado = new ResultadoDeteccion(motivos);
        if (resultado.esSospechosa()) {
            Log.infof("Transferencia marcada para revisión. Cuenta origen id=%d, motivos=[%s]",
                    cuentaOrigenId, resultado.descripcion());
        }
        return resultado;
    }
}
