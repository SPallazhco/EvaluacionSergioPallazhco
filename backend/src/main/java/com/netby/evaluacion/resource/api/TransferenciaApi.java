package com.netby.evaluacion.resource.api;

import com.netby.evaluacion.dto.request.CrearTransferenciaRequest;
import com.netby.evaluacion.dto.request.FiltroTransferenciasRequest;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.dto.response.TransferenciaResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static com.netby.evaluacion.resource.api.EjemplosApi.PROBLEM_JSON;

/**
 * Contrato HTTP de transferencias: rutas, validaciones y documentación OpenAPI. La implementación
 * ({@code TransferenciaResource}) solo delega en la capa de servicio.
 */
@Path("/api/transferencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Transferencias", description = "Ejecución y consulta de transferencias entre cuentas")
public interface TransferenciaApi {

    @POST
    @Operation(summary = "Ejecutar una transferencia",
            description = """
                    Transfiere el monto de la cuenta origen a la cuenta destino de forma atómica: ambos saldos \
                    se actualizan en una sola transacción o ninguno.

                    **Validaciones:** ambas cuentas deben existir y estar activas, la cuenta origen debe tener \
                    saldo suficiente, el monto debe ser mayor a cero y el acumulado del día no puede superar \
                    10,000.00. Si se incumple una regla de negocio la transferencia queda registrada como \
                    RECHAZADA y se responde 422.

                    **Detección de sospechosas:** si el monto supera 5,000.00 o la cuenta origen ya realizó 5 \
                    transferencias en la última hora, la transferencia se ejecuta y queda EN_REVISION.""")
    @RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = CrearTransferenciaRequest.class),
            examples = @ExampleObject(name = "Transferencia", value = EjemplosApi.CREAR_TRANSFERENCIA)))
    @APIResponse(responseCode = "201", description = "Transferencia ejecutada (COMPLETADA o EN_REVISION)",
            headers = @Header(name = "Location", description = "URL de la transferencia creada"),
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TransferenciaResponse.class),
                    examples = {
                            @ExampleObject(name = "Completada", value = EjemplosApi.TRANSFERENCIA_COMPLETADA),
                            @ExampleObject(name = "En revisión", value = EjemplosApi.TRANSFERENCIA_EN_REVISION)
                    }))
    @APIResponse(responseCode = "400", description = "Datos inválidos (monto, formato de cuentas, cuentas iguales)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Validación", value = EjemplosApi.ERROR_VALIDACION)))
    @APIResponse(responseCode = "404", description = "La cuenta origen o destino no existe",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Cuenta inexistente",
                            value = EjemplosApi.ERROR_CUENTA_NO_ENCONTRADA)))
    @APIResponse(responseCode = "422", description = "Regla de negocio incumplida; la transferencia queda RECHAZADA",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = {
                            @ExampleObject(name = "Saldo insuficiente", value = EjemplosApi.ERROR_SALDO_INSUFICIENTE),
                            @ExampleObject(name = "Cuenta inactiva", value = EjemplosApi.ERROR_CUENTA_INACTIVA),
                            @ExampleObject(name = "Límite diario", value = EjemplosApi.ERROR_LIMITE_DIARIO)
                    }))
    @APIResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Error interno", value = EjemplosApi.ERROR_INTERNO)))
    Response ejecutar(@NotNull(message = "El cuerpo de la solicitud es obligatorio")
                      @Valid CrearTransferenciaRequest request,
                      @Context UriInfo uriInfo);

    @GET
    @Operation(summary = "Consultar el historial de transferencias",
            description = "Devuelve las transferencias de la más reciente a la más antigua, de forma paginada. "
                    + "Todos los filtros son opcionales y se pueden combinar. Las fechas son días calendario "
                    + "(yyyy-MM-dd) en America/Guayaquil y ambas son inclusivas.")
    @APIResponse(responseCode = "200", description = "Página de transferencias")
    @APIResponse(responseCode = "400", description = "Filtros inválidos (formato o rango de fechas)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class)))
    PaginaResponse<TransferenciaResponse> consultarHistorial(@Valid @BeanParam FiltroTransferenciasRequest filtro);

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener una transferencia", description = "Devuelve una transferencia por su identificador.")
    @APIResponse(responseCode = "200", description = "Transferencia encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TransferenciaResponse.class),
                    examples = @ExampleObject(name = "Transferencia", value = EjemplosApi.TRANSFERENCIA_COMPLETADA)))
    @APIResponse(responseCode = "404", description = "La transferencia no existe",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "No encontrada",
                            value = EjemplosApi.ERROR_TRANSFERENCIA_NO_ENCONTRADA)))
    TransferenciaResponse obtenerPorId(@Parameter(description = "Identificador de la transferencia", example = "10")
                                       @PathParam("id") Long id);
}
