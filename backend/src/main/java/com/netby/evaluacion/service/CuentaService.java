package com.netby.evaluacion.service;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.entity.Cuenta;
import com.netby.evaluacion.exception.CodigoError;
import com.netby.evaluacion.exception.RecursoDuplicadoException;
import com.netby.evaluacion.exception.RecursoNoEncontradoException;
import com.netby.evaluacion.mapper.CuentaMapper;
import com.netby.evaluacion.repository.CuentaRepository;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final CuentaMapper cuentaMapper;

    @Inject
    public CuentaService(CuentaRepository cuentaRepository, CuentaMapper cuentaMapper) {
        this.cuentaRepository = cuentaRepository;
        this.cuentaMapper = cuentaMapper;
    }

    @Transactional
    public CuentaResponse registrar(CrearCuentaRequest request) {
        if (cuentaRepository.existePorNumeroCuenta(request.numeroCuenta())) {
            throw new RecursoDuplicadoException(CodigoError.CUENTA_DUPLICADA,
                    "Ya existe una cuenta con el número " + request.numeroCuenta());
        }

        Cuenta cuenta = cuentaMapper.toEntity(request);
        cuentaRepository.persist(cuenta);

        Log.infof("Cuenta registrada id=%d numero=%s tipo=%s", cuenta.getId(),
                Enmascarador.numeroCuenta(cuenta.getNumeroCuenta()), cuenta.getTipo());
        return cuentaMapper.toResponse(cuenta);
    }

    public CuentaResponse obtenerPorId(Long id) {
        return cuentaRepository.findByIdOptional(id)
                .map(cuentaMapper::toResponse)
                .orElseThrow(() -> new RecursoNoEncontradoException(CodigoError.CUENTA_NO_ENCONTRADA,
                        "No existe una cuenta con id " + id));
    }

    public PaginaResponse<CuentaResponse> listar(int pagina, int tamanio) {
        PanacheQuery<Cuenta> consulta = cuentaRepository.findAll(Sort.by("id"))
                .page(Page.of(pagina, tamanio));

        return new PaginaResponse<>(
                consulta.list().stream().map(cuentaMapper::toResponse).toList(),
                pagina,
                tamanio,
                consulta.count(),
                consulta.pageCount());
    }
}
