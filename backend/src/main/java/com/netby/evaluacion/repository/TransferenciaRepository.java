package com.netby.evaluacion.repository;

import com.netby.evaluacion.entity.EstadoTransferencia;
import com.netby.evaluacion.entity.Transferencia;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.StringJoiner;

@ApplicationScoped
public class TransferenciaRepository implements PanacheRepository<Transferencia> {

    public Optional<Transferencia> buscarPorIdConCuentas(Long id) {
        return find("""
                from Transferencia t
                join fetch t.cuentaOrigen
                join fetch t.cuentaDestino
                where t.id = ?1
                """, id).firstResultOptional();
    }

    public PanacheQuery<Transferencia> buscarHistorial(CriteriosHistorial criterios) {
        StringJoiner condiciones = new StringJoiner(" and ", " where ", "").setEmptyValue("");
        Parameters parametros = new Parameters();

        if (criterios.numeroCuentaOrigen() != null) {
            condiciones.add("origen.numeroCuenta = :origen");
            parametros.and("origen", criterios.numeroCuentaOrigen());
        }
        if (criterios.numeroCuentaDestino() != null) {
            condiciones.add("destino.numeroCuenta = :destino");
            parametros.and("destino", criterios.numeroCuentaDestino());
        }
        if (criterios.desde() != null) {
            condiciones.add("t.fecha >= :desde");
            parametros.and("desde", criterios.desde());
        }
        if (criterios.hasta() != null) {
            condiciones.add("t.fecha < :hasta");
            parametros.and("hasta", criterios.hasta());
        }
        if (criterios.estado() != null) {
            condiciones.add("t.estado = :estado");
            parametros.and("estado", criterios.estado());
        }

        String consulta = "from Transferencia t"
                + " join fetch t.cuentaOrigen origen"
                + " join fetch t.cuentaDestino destino"
                + condiciones
                + " order by t.fecha desc, t.id desc";
        return find(consulta, parametros);
    }

    public long contarEjecutadasDesde(Long cuentaOrigenId, Instant desde) {
        return count("cuentaOrigen.id = :cuentaId and fecha >= :desde and estado <> :rechazada",
                Parameters.with("cuentaId", cuentaOrigenId)
                        .and("desde", desde)
                        .and("rechazada", EstadoTransferencia.RECHAZADA));
    }

    public long contarEjecutadasEntre(Long cuentaOrigenId, Instant desde, Instant hasta) {
        return count("cuentaOrigen.id = :cuentaId and fecha >= :desde and fecha < :hasta and estado <> :rechazada",
                Parameters.with("cuentaId", cuentaOrigenId)
                        .and("desde", desde)
                        .and("hasta", hasta)
                        .and("rechazada", EstadoTransferencia.RECHAZADA));
    }

    public long contarPorEstadoEntre(Long cuentaOrigenId, EstadoTransferencia estado, Instant desde, Instant hasta) {
        return count("cuentaOrigen.id = :cuentaId and estado = :estado and fecha >= :desde and fecha < :hasta",
                Parameters.with("cuentaId", cuentaOrigenId)
                        .and("estado", estado)
                        .and("desde", desde)
                        .and("hasta", hasta));
    }

    public long contarPorEstado(Long cuentaOrigenId, EstadoTransferencia estado) {
        return count("cuentaOrigen.id = :cuentaId and estado = :estado",
                Parameters.with("cuentaId", cuentaOrigenId).and("estado", estado));
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
