package com.netby.evaluacion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "cuenta")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_cuenta", nullable = false, unique = true, length = 10)
    private String numeroCuenta;

    @Column(name = "titular", nullable = false, length = 100)
    private String titular;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoCuenta tipo;

    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoCuenta estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    // Requerido por JPA
    protected Cuenta() {
    }

    public Cuenta(String numeroCuenta, String titular, TipoCuenta tipo, BigDecimal saldoInicial, EstadoCuenta estado) {
        this.numeroCuenta = numeroCuenta;
        this.titular = titular;
        this.tipo = tipo;
        this.saldo = saldoInicial;
        this.estado = estado;
    }

    public boolean estaActiva() {
        return estado == EstadoCuenta.ACTIVA;
    }

    public boolean tieneSaldoSuficiente(BigDecimal monto) {
        return saldo.compareTo(monto) >= 0;
    }

    public void debitar(BigDecimal monto) {
        validarMontoPositivo(monto);
        // Respaldo: el servicio ya valida el saldo antes de debitar
        if (!tieneSaldoSuficiente(monto)) {
            throw new IllegalStateException("El saldo de la cuenta no puede quedar negativo");
        }
        saldo = saldo.subtract(monto);
    }

    public void acreditar(BigDecimal monto) {
        validarMontoPositivo(monto);
        saldo = saldo.add(monto);
    }

    private static void validarMontoPositivo(BigDecimal monto) {
        Objects.requireNonNull(monto, "El monto es obligatorio");
        if (monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public String getTitular() {
        return titular;
    }

    public TipoCuenta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public EstadoCuenta getEstado() {
        return estado;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Cuenta otra = (Cuenta) o;
        return id != null && id.equals(otra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
