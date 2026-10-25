package com.netby.evaluacion.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CrearTransferenciaRequest(

        @NotBlank(message = "La cuenta origen es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta origen debe tener exactamente 10 dígitos")
        String numeroCuentaOrigen,

        @NotBlank(message = "La cuenta destino es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta destino debe tener exactamente 10 dígitos")
        String numeroCuentaDestino,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        @Digits(integer = 13, fraction = 2, message = "El monto admite hasta 13 enteros y 2 decimales")
        BigDecimal monto
) {

    @JsonIgnore
    @AssertTrue(message = "La cuenta origen y la cuenta destino deben ser diferentes")
    public boolean isCuentasDistintas() {
        return numeroCuentaOrigen == null || !numeroCuentaOrigen.equals(numeroCuentaDestino);
    }
}
