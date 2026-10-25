package com.netby.evaluacion.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CuentaTest {

    private static Cuenta cuentaConSaldo(String saldo, EstadoCuenta estado) {
        return new Cuenta("1234567890", "Ana Pérez", TipoCuenta.AHORRO, new BigDecimal(saldo), estado);
    }

    @Test
    void debitarDebeRestarElMontoDelSaldo() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);

        cuenta.debitar(new BigDecimal("40.50"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("59.50");
    }

    @Test
    void debitarTodoElSaldoDebeDejarlaEnCero() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);

        cuenta.debitar(new BigDecimal("100.00"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void debitarMasDelSaldoDebeFallarSinModificarlo() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);
        BigDecimal monto = new BigDecimal("100.01");

        assertThatThrownBy(() -> cuenta.debitar(monto))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void acreditarDebeSumarElMontoAlSaldo() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);

        cuenta.acreditar(new BigDecimal("0.01"));

        assertThat(cuenta.getSaldo()).isEqualByComparingTo("100.01");
    }

    @Test
    void operarConMontoCeroONegativoDebeFallar() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);
        BigDecimal cero = BigDecimal.ZERO;
        BigDecimal negativo = new BigDecimal("-1");

        assertThatThrownBy(() -> cuenta.acreditar(cero)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cuenta.debitar(negativo)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void operarConMontoNuloDebeFallar() {
        Cuenta cuenta = cuentaConSaldo("100.00", EstadoCuenta.ACTIVA);

        assertThatThrownBy(() -> cuenta.acreditar(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void estaActivaDebeReflejarElEstado() {
        assertThat(cuentaConSaldo("0", EstadoCuenta.ACTIVA).estaActiva()).isTrue();
        assertThat(cuentaConSaldo("0", EstadoCuenta.INACTIVA).estaActiva()).isFalse();
    }

    @Test
    void tieneSaldoSuficienteDebeCompararPorValorYNoPorEscala() {
        Cuenta cuenta = cuentaConSaldo("50.00", EstadoCuenta.ACTIVA);

        assertThat(cuenta.tieneSaldoSuficiente(new BigDecimal("50"))).isTrue();
        assertThat(cuenta.tieneSaldoSuficiente(new BigDecimal("50.01"))).isFalse();
    }

    @Test
    void cuentasSinPersistirSoloSonIgualesASiMismas() {
        Cuenta cuenta = cuentaConSaldo("10.00", EstadoCuenta.ACTIVA);
        Cuenta otra = cuentaConSaldo("10.00", EstadoCuenta.ACTIVA);

        assertThat(cuenta).isEqualTo(cuenta)
                .isNotEqualTo(otra)
                .isNotEqualTo(null)
                .isNotEqualTo("1234567890")
                .hasSameHashCodeAs(otra);
    }
}
