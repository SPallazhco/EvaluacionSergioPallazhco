package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.entity.Transferencia;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Las consultas de acumulados excluyen las transferencias rechazadas: no movieron dinero,
 * por lo que no cuentan para el límite diario ni para la detección por frecuencia.
 */
@ApplicationScoped
public class TransferenciaRepository implements PanacheRepository<Transferencia> {

    public long contarEjecutadasDesde(Long cuentaOrigenId, Instant desde) {
        return count("cuentaOrigen.id = :cuentaId and fecha >= :desde and estado <> :rechazada",
                Parameters.with("cuentaId", cuentaOrigenId)
                        .and("desde", desde)
                        .and("rechazada", EstadoTransferencia.RECHAZADA));
    }

    public BigDecimal sumarMontoEjecutadoEntre(Long cuentaOrigenId, Instant desde, Instant hasta) {
        return getEntityManager()
                .createQuery("""
                        select coalesce(sum(t.monto), 0)
                        from Transferencia t
                        where t.cuentaOrigen.id = :cuentaId
                          and t.fecha >= :desde and t.fecha < :hasta
                          and t.estado <> :rechazada
                        """, BigDecimal.class)
                .setParameter("cuentaId", cuentaOrigenId)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("rechazada", EstadoTransferencia.RECHAZADA)
                .getSingleResult();
    }
}
