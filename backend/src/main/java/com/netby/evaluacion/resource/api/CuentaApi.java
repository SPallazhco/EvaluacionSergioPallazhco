package com.netby.evaluacion.resource.api;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.dto.response.ProblemaResponse;
import com.netby.evaluacion.dto.response.ResumenCuentaResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
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

@Path("/api/cuentas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Cuentas", description = "Registro y consulta de cuentas bancarias")
public interface CuentaApi {

    @POST
    @Operation(summary = "Registrar una cuenta",
            description = "Crea una cuenta bancaria. El número de cuenta debe tener 10 dígitos y ser único; "
                    + "el saldo inicial no puede ser negativo.")
    @RequestBody(required = true, content = @Content(
            schema = @Schema(implementation = CrearCuentaRequest.class),
            examples = @ExampleObject(name = "Cuenta de ahorro", value = EjemplosApi.CREAR_CUENTA)))
    @APIResponse(responseCode = "201", description = "Cuenta creada",
            headers = @Header(name = "Location", description = "URL de la cuenta creada"),
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CuentaResponse.class),
                    examples = @ExampleObject(name = "Cuenta creada", value = EjemplosApi.CUENTA)))
    @APIResponse(responseCode = "400", description = "Datos inválidos (detalle por campo)",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Validación", value = EjemplosApi.ERROR_VALIDACION)))
    @APIResponse(responseCode = "409", description = "Ya existe una cuenta con ese número",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Duplicada", value = EjemplosApi.ERROR_CUENTA_DUPLICADA)))
    @APIResponse(responseCode = "500", description = "Error inesperado",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class),
                    examples = @ExampleObject(name = "Error interno", value = EjemplosApi.ERROR_INTERNO)))
    Response registrar(@NotNull(message = "El cuerpo de la solicitud es obligatorio") @Valid CrearCuentaRequest request,
                       @Context UriInfo uriInfo);

    @GET
    @Operation(summary = "Listar cuentas",
            description = "Devuelve las cuentas registradas de forma paginada, ordenadas por id.")
    @APIResponse(responseCode = "200", description = "Página de cuentas")
    @APIResponse(responseCode = "400", description = "Parámetros de paginación inválidos",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class)))
    PaginaResponse<CuentaResponse> listar(
            @Parameter(description = "Número de página (desde 0)", example = "0")
            @QueryParam("pagina") @DefaultValue("0")
            @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @Parameter(description = "Cantidad de elementos por página (1 a 100)", example = "20")
            @QueryParam("tamanio") @DefaultValue("20")
            @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
            @Max(value = 100, message = "El tamaño de página no puede superar 100") int tamanio);

    @GET
    @Path("/{id}")
    @Operation(summary = "Obtener una cuenta", description = "Devuelve los datos de una cuenta por su identificador.")
    @APIResponse(responseCode = "200", description = "Cuenta encontrada",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = CuentaResponse.class),
                    examples = @ExampleObject(name = "Cuenta", value = EjemplosApi.CUENTA)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class)))
    CuentaResponse obtenerPorId(@Parameter(description = "Identificador de la cuenta", example = "1")
                                @PathParam("id") Long id);

    @GET
    @Path("/{id}/resumen")
    @Operation(summary = "Resumen de una cuenta",
            description = "Calcula en tiempo real el saldo actual y la actividad del día calendario en curso "
                    + "(America/Guayaquil) como cuenta origen: total transferido, monto disponible del límite "
                    + "diario, cantidad de transferencias y de transferencias marcadas como sospechosas. "
                    + "Las transferencias rechazadas no se contabilizan.")
    @APIResponse(responseCode = "200", description = "Resumen de la cuenta",
            content = @Content(mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = ResumenCuentaResponse.class),
                    examples = @ExampleObject(name = "Resumen", value = EjemplosApi.RESUMEN_CUENTA)))
    @APIResponse(responseCode = "404", description = "La cuenta no existe",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemaResponse.class)))
    ResumenCuentaResponse obtenerResumen(@Parameter(description = "Identificador de la cuenta", example = "1")
                                         @PathParam("id") Long id);
}
