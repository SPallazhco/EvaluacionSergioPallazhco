package com.netby.evaluacion.service;

import com.netby.evaluacion.dto.request.CrearTransferenciaRequest;
import com.netby.evaluacion.dto.response.TransferenciaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.Transferencia;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.exception.ReglaNegocioException;
import com.netby.evaluacion.mapper.TransferenciaMapper;
import com.netby.evaluacion.repository.CuentaRepository;
import com.netby.evaluacion.repository.TransferenciaRepository;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@ApplicationScoped
public class TransferenciaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final DeteccionSospechosaService deteccionSospechosaService;
    private final LimiteDiarioService limiteDiarioService;
    private final CalendarioNegocio calendario;
    private final TransferenciaMapper transferenciaMapper;

    @Inject
    public TransferenciaService(CuentaRepository cuentaRepository,
                                TransferenciaRepository transferenciaRepository,
                                DeteccionSospechosaService deteccionSospechosaService,
                                LimiteDiarioService limiteDiarioService,
                                CalendarioNegocio calendario,
                                TransferenciaMapper transferenciaMapper) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.deteccionSospechosaService = deteccionSospechosaService;
        this.limiteDiarioService = limiteDiarioService;
        this.calendario = calendario;
        this.transferenciaMapper = transferenciaMapper;
    }

    /**
     * Ejecuta la transferencia de forma atómica: ambos saldos y el registro se confirman juntos.
     * <p>
     * Si se incumple una regla de negocio, la transferencia se registra como RECHAZADA y la
     * transacción se confirma igualmente ({@code dontRollbackOn}) para conservar ese registro;
     * es seguro porque todas las reglas se validan antes de modificar cualquier saldo.
     * Se usa una única transacción a propósito: registrar el rechazo en una transacción nueva
     * ({@code REQUIRES_NEW}) se bloquearía esperando las filas de cuenta que esta mantiene
     * bloqueadas, ya que la verificación de la llave foránea necesita leerlas.
     */
    @Transactional(dontRollbackOn = ReglaNegocioException.class)
    public TransferenciaResponse ejecutar(CrearTransferenciaRequest request) {
        Instant ahora = calendario.ahora();
        CuentasBloqueadas cuentas = bloquearCuentas(request.numeroCuentaOrigen(), request.numeroCuentaDestino());
        Cuenta origen = cuentas.origen();
        Cuenta destino = cuentas.destino();
        BigDecimal monto = request.monto();

        try {
            validarReglas(origen, destino, monto, ahora);
        } catch (ReglaNegocioException e) {
            Transferencia rechazada = Transferencia.rechazada(origen, destino, monto, ahora, e.getMessage());
            transferenciaRepository.persist(rechazada);
            Log.infof("Transferencia id=%d rechazada: %s", rechazada.getId(), e.getCodigo());
            throw e;
        }

        ResultadoDeteccion deteccion = deteccionSospechosaService.evaluar(origen.getId(), monto, ahora);

        origen.debitar(monto);
        destino.acreditar(monto);

        Transferencia transferencia = deteccion.esSospechosa()
                ? Transferencia.enRevision(origen, destino, monto, ahora, deteccion.descripcion())
                : Transferencia.completada(origen, destino, monto, ahora);
        transferenciaRepository.persist(transferencia);

        Log.infof("Transferencia id=%d ejecutada con estado %s", transferencia.getId(), transferencia.getEstado());
        return transferenciaMapper.toResponse(transferencia);
    }

    public TransferenciaResponse obtenerPorId(Long id) {
        return transferenciaRepository.buscarPorIdConCuentas(id)
                .map(transferenciaMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(CodigoError.TRANSFERENCIA_NO_ENCONTRADA,
                        "No existe una transferencia con id " + id));
    }

    /**
     * Bloquea ambas cuentas siempre en el mismo orden (por número de cuenta). Si dos transferencias
     * cruzadas (A→B y B→A) bloquearan en el orden de la solicitud, cada una podría quedarse esperando
     * la cuenta que tiene la otra: un deadlock.
     */
    private CuentasBloqueadas bloquearCuentas(String numeroOrigen, String numeroDestino) {
        boolean origenPrimero = numeroOrigen.compareTo(numeroDestino) < 0;
        Cuenta primera = buscarParaActualizar(origenPrimero ? numeroOrigen : numeroDestino);
        Cuenta segunda = buscarParaActualizar(origenPrimero ? numeroDestino : numeroOrigen);
        return origenPrimero
                ? new CuentasBloqueadas(primera, segunda)
                : new CuentasBloqueadas(segunda, primera);
    }

    private Cuenta buscarParaActualizar(String numeroCuenta) {
        return cuentaRepository.buscarPorNumeroParaActualizar(numeroCuenta)
                .orElseThrow(() -> new RecursoNoEncontradoException(CodigoError.CUENTA_NO_ENCONTRADA,
                        "No existe la cuenta " + numeroCuenta));
    }

    private void validarReglas(Cuenta origen, Cuenta destino, BigDecimal monto, Instant ahora) {
        if (!origen.estaActiva()) {
            throw new ReglaNegocioException(CodigoError.CUENTA_INACTIVA, "La cuenta origen no está activa");
        }
        if (!destino.estaActiva()) {
            throw new ReglaNegocioException(CodigoError.CUENTA_INACTIVA, "La cuenta destino no está activa");
        }
        if (!origen.tieneSaldoSuficiente(monto)) {
            throw new ReglaNegocioException(CodigoError.SALDO_INSUFICIENTE,
                    "La cuenta origen no tiene saldo suficiente");
        }
        limiteDiarioService.validar(origen.getId(), monto, ahora);
    }

    private record CuentasBloqueadas(Cuenta origen, Cuenta destino) {
    }
}
