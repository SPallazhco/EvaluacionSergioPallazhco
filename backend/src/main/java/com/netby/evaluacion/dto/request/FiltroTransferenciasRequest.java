package com.netby.evaluacion.dto.request;

import com.netby.evaluacion.entity.EstadoTransferencia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.time.LocalDate;

/**
 * Parámetros de consulta del historial. Todos los filtros son opcionales; las fechas son días
 * calendario (formato {@code yyyy-MM-dd}) en la zona horaria de negocio y ambas son inclusivas.
 */
public record FiltroTransferenciasRequest(

        @Parameter(description = "Número de la cuenta origen (10 dígitos)", example = "1234567890")
        @QueryParam("cuentaOrigen")
        @Pattern(regexp = "\\d{10}", message = "La cuenta origen debe tener exactamente 10 dígitos")
        String cuentaOrigen,

        @Parameter(description = "Número de la cuenta destino (10 dígitos)")
        @QueryParam("cuentaDestino")
        @Pattern(regexp = "\\d{10}", message = "La cuenta destino debe tener exactamente 10 dígitos")
        String cuentaDestino,

        @Parameter(description = "Día inicial, inclusivo (yyyy-MM-dd)", example = "2026-10-01")
        @QueryParam("fechaDesde")
        LocalDate fechaDesde,

        @Parameter(description = "Día final, inclusivo (yyyy-MM-dd)", example = "2026-10-24")
        @QueryParam("fechaHasta")
        LocalDate fechaHasta,

        @Parameter(description = "Estado de la transferencia")
        @QueryParam("estado")
        EstadoTransferencia estado,

        @Parameter(description = "Número de página (desde 0)", example = "0")
        @QueryParam("pagina") @DefaultValue("0")
        @Min(value = 0, message = "La página no puede ser negativa")
        int pagina,

        @Parameter(description = "Elementos por página (1 a 100)", example = "20")
        @QueryParam("tamanio") @DefaultValue("20")
        @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
        @Max(value = 100, message = "El tamaño de página no puede superar 100")
        int tamanio
) {

    @AssertTrue(message = "La fecha desde no puede ser posterior a la fecha hasta")
    public boolean isRangoFechasValido() {
        return fechaDesde == null || fechaHasta == null || !fechaDesde.isAfter(fechaHasta);
    }
}
