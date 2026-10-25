package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.Cuenta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

import java.util.Optional;

@ApplicationScoped
public class CuentaRepository implements PanacheRepository<Cuenta> {

    public boolean existePorNumeroCuenta(String numeroCuenta) {
        return count("numeroCuenta", numeroCuenta) > 0;
    }

    public Optional<Cuenta> buscarPorNumeroParaActualizar(String numeroCuenta) {
        return find("numeroCuenta", numeroCuenta)
                .withLock(LockModeType.PESSIMISTIC_WRITE)
                .firstResultOptional();
    }
}
