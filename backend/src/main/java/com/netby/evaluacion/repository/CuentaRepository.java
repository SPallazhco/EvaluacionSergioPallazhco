package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.Cuenta;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CuentaRepository implements PanacheRepository<Cuenta> {

    public boolean existePorNumeroCuenta(String numeroCuenta) {
        return count("numeroCuenta", numeroCuenta) > 0;
    }
}
