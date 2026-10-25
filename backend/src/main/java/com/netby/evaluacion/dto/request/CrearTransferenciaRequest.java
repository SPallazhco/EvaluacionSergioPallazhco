package com.netby.evaluacion.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Datos para ejecutar una transferencia")
public record CrearTransferenciaRequest(

        @NotBlank(message = "La cuenta origen es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta origen debe tener exactamente 10 dígitos")
        @Schema(description = "Número de la cuenta que envía el dinero", example = "1234567890")
        String numeroCuentaOrigen,

        @NotBlank(message = "La cuenta destino es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cuenta destino debe tener exactamente 10 dígitos")
        @Schema(description = "Número de la cuenta que recibe el dinero; distinta de la origen", example = "0987654321")
        String numeroCuentaDestino,

        @NotNull(message = "El monto es obligatorio")
        @Positive(message = "El monto debe ser mayor a cero")
        @Digits(integer = 13, fraction = 2, message = "El monto admite hasta 13 enteros y 2 decimales")
        @Schema(description = "Monto a transferir; mayor a cero y con hasta 2 decimales", example = "250.00")
        BigDecimal monto
) {

    @JsonIgnore
    @AssertTrue(message = "La cuenta origen y la cuenta destino deben ser diferentes")
    public boolean isCuentasDistintas() {
        return numeroCuentaOrigen == null || !numeroCuentaOrigen.equals(numeroCuentaDestino);
    }
}
