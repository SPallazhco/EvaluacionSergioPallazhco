package com.netby.evaluacion.dto.request;

import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearCuentaRequest(

        @NotBlank(message = "El número de cuenta es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El número de cuenta debe tener exactamente 10 dígitos")
        String numeroCuenta,

        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 100, message = "El titular no puede superar los 100 caracteres")
        String titular,

        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipo,

        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El saldo inicial admite hasta 13 enteros y 2 decimales")
        BigDecimal saldoInicial,

        @NotNull(message = "El estado de la cuenta es obligatorio")
        EstadoCuenta estado
) {
}
