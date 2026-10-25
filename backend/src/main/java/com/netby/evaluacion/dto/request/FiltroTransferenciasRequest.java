package com.netby.evaluacion.dto.request;

import com.netby.evaluacion.entity.EstadoTransferencia;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.QueryParam;

import java.time.LocalDate;

/**
 * Parámetros de consulta del historial. Todos los filtros son opcionales; las fechas son días
 * calendario (formato {@code yyyy-MM-dd}) en la zona horaria de negocio y ambas son inclusivas.
 */
public record FiltroTransferenciasRequest(

        @QueryParam("cuentaOrigen")
        @Pattern(regexp = "\\d{10}", message = "La cuenta origen debe tener exactamente 10 dígitos")
        String cuentaOrigen,

        @QueryParam("cuentaDestino")
        @Pattern(regexp = "\\d{10}", message = "La cuenta destino debe tener exactamente 10 dígitos")
        String cuentaDestino,

        @QueryParam("fechaDesde")
        LocalDate fechaDesde,

        @QueryParam("fechaHasta")
        LocalDate fechaHasta,

        @QueryParam("estado")
        EstadoTransferencia estado,

        @QueryParam("pagina") @DefaultValue("0")
        @Min(value = 0, message = "La página no puede ser negativa")
        int pagina,

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
