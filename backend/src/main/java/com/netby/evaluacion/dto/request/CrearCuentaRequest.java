package com.netby.evaluacion.dto.request;

import com.netby.evaluacion.entity.EstadoCuenta;
import com.netby.evaluacion.entity.TipoCuenta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos para registrar una cuenta bancaria")
public record CrearCuentaRequest(

        @NotBlank(message = "El número de cuenta es obligatorio")
        @Pattern(regexp = "\\d{10}", message = "El número de cuenta debe tener exactamente 10 dígitos")
        @Schema(description = "Número único de 10 dígitos", example = "1234567890")
        String numeroCuenta,

        @NotBlank(message = "El titular es obligatorio")
        @Size(max = 100, message = "El titular no puede superar los 100 caracteres")
        @Schema(description = "Nombre del titular", example = "Ana Pérez")
        String titular,

        @NotNull(message = "El tipo de cuenta es obligatorio")
        @Schema(description = "Tipo de cuenta", example = "AHORRO")
        TipoCuenta tipo,

        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El saldo inicial admite hasta 13 enteros y 2 decimales")
        @Schema(description = "Saldo inicial; no puede ser negativo", example = "1500.00")
        BigDecimal saldoInicial,

        @NotNull(message = "El estado de la cuenta es obligatorio")
        @Schema(description = "Estado inicial de la cuenta", example = "ACTIVA")
        EstadoCuenta estado
) {
}
