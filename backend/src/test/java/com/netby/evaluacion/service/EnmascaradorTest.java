package com.netby.evaluacion.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class EnmascaradorTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "1234567890, ******7890",
            "12345,      *2345",
            "1234,       ****",
            "12,         ****"
    })
    void debeDejarVisiblesSoloLosUltimosCuatroDigitos(String numero, String esperado) {
        assertThat(Enmascarador.numeroCuenta(numero)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void valoresVaciosDebenEnmascararseSinFallar(String numero) {
        assertThat(Enmascarador.numeroCuenta(numero)).isEqualTo("****");
    }
}
