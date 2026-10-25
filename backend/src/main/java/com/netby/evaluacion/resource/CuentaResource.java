package com.netby.evaluacion.resource;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.dto.response.ResumenCuentaResponse;
import com.netby.evaluacion.service.CuentaService;
import com.netby.evaluacion.service.ResumenCuentaService;
import jakarta.inject.Inject;
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

import java.net.URI;

@Path("/api/cuentas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CuentaResource {

    private final CuentaService cuentaService;
    private final ResumenCuentaService resumenCuentaService;

    @Inject
    public CuentaResource(CuentaService cuentaService, ResumenCuentaService resumenCuentaService) {
        this.cuentaService = cuentaService;
        this.resumenCuentaService = resumenCuentaService;
    }

    @POST
    public Response registrar(@NotNull(message = "El cuerpo de la solicitud es obligatorio")
                              @Valid CrearCuentaRequest request,
                              @Context UriInfo uriInfo) {
        CuentaResponse cuenta = cuentaService.registrar(request);
        URI ubicacion = uriInfo.getAbsolutePathBuilder().path(String.valueOf(cuenta.id())).build();
        return Response.created(ubicacion).entity(cuenta).build();
    }

    @GET
    public PaginaResponse<CuentaResponse> listar(
            @QueryParam("pagina") @DefaultValue("0")
            @Min(value = 0, message = "La página no puede ser negativa") int pagina,
            @QueryParam("tamanio") @DefaultValue("20")
            @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
            @Max(value = 100, message = "El tamaño de página no puede superar 100") int tamanio) {
        return cuentaService.listar(pagina, tamanio);
    }

    @GET
    @Path("/{id}")
    public CuentaResponse obtenerPorId(@PathParam("id") Long id) {
        return cuentaService.obtenerPorId(id);
    }

    @GET
    @Path("/{id}/resumen")
    public ResumenCuentaResponse obtenerResumen(@PathParam("id") Long id) {
        return resumenCuentaService.obtener(id);
    }
}
