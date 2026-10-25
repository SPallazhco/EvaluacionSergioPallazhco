package com.netby.evaluacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transferencia")
public class Transferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    private Cuenta cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cuenta_destino_id", nullable = false)
    private Cuenta cuentaDestino;

    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoTransferencia estado;

    @Column(name = "motivo")
    private String motivo;

    // Requerido por JPA
    protected Transferencia() {
    }

    private Transferencia(Cuenta cuentaOrigen, Cuenta cuentaDestino, BigDecimal monto, Instant fecha,
                          EstadoTransferencia estado, String motivo) {
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.fecha = fecha;
        this.estado = estado;
        this.motivo = motivo;
    }

    public static Transferencia completada(Cuenta origen, Cuenta destino, BigDecimal monto, Instant fecha) {
        return new Transferencia(origen, destino, monto, fecha, EstadoTransferencia.COMPLETADA, null);
    }

    public static Transferencia enRevision(Cuenta origen, Cuenta destino, BigDecimal monto, Instant fecha,
                                           String motivo) {
        return new Transferencia(origen, destino, monto, fecha, EstadoTransferencia.EN_REVISION, motivo);
    }

    public static Transferencia rechazada(Cuenta origen, Cuenta destino, BigDecimal monto, Instant fecha,
                                          String motivo) {
        return new Transferencia(origen, destino, monto, fecha, EstadoTransferencia.RECHAZADA, motivo);
    }

    public boolean esSospechosa() {
        return estado == EstadoTransferencia.EN_REVISION;
    }

    public Long getId() {
        return id;
    }

    public Cuenta getCuentaOrigen() {
        return cuentaOrigen;
    }

    public Cuenta getCuentaDestino() {
        return cuentaDestino;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoTransferencia getEstado() {
        return estado;
    }

    public String getMotivo() {
        return motivo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Transferencia otra = (Transferencia) o;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
