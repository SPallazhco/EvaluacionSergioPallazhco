package com.netby.evaluacion.service;

import com.netby.evaluacion.dto.response.ResumenCuentaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.repository.CuentaRepository;
import com.netby.evaluacion.repository.TransferenciaRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@ApplicationScoped
public class ResumenCuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final LimiteDiarioService limiteDiarioService;
    private final CalendarioNegocio calendario;

    @Inject
    public ResumenCuentaService(CuentaRepository cuentaRepository,
                                TransferenciaRepository transferenciaRepository,
                                LimiteDiarioService limiteDiarioService,
                                CalendarioNegocio calendario) {
        this.cuentaRepository = cuentaRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.limiteDiarioService = limiteDiarioService;
        this.calendario = calendario;
    }

    // Una sola transacción para que todas las consultas lean los mismos datos
    @Transactional
    public ResumenCuentaResponse obtener(Long cuentaId) {
        Cuenta cuenta = cuentaRepository.findByIdOptional(cuentaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(CodigoError.CUENTA_NO_ENCONTRADA,
                        "No existe una cuenta con id " + cuentaId));

        Instant ahora = calendario.ahora();
        RangoFechas hoy = calendario.rangoDelDia(ahora);
        BigDecimal totalHoy = limiteDiarioService.totalTransferidoEnElDia(cuentaId, ahora);

        return new ResumenCuentaResponse(
                cuenta.getId(),
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                totalHoy,
                limiteDiarioService.disponibleSegunAcumulado(totalHoy),
                transferenciaRepository.contarEjecutadasEntre(cuentaId, hoy.desde(), hoy.hasta()),
                transferenciaRepository.contarPorEstadoEntre(cuentaId, EstadoTransferencia.EN_REVISION,
                        hoy.desde(), hoy.hasta()),
                transferenciaRepository.contarPorEstado(cuentaId, EstadoTransferencia.EN_REVISION));
    }
}
