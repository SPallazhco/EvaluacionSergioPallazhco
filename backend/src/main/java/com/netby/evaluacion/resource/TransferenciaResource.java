package com.netby.evaluacion.resource;

import com.netby.evaluacion.dto.request.CrearTransferenciaRequest;
import com.netby.evaluacion.dto.response.TransferenciaResponse;
import com.netby.evaluacion.service.TransferenciaService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

import java.net.URI;

@Path("/api/transferencias")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransferenciaResource {

    private final TransferenciaService transferenciaService;

    @Inject
    public TransferenciaResource(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @POST
    public Response ejecutar(@NotNull(message = "El cuerpo de la solicitud es obligatorio")
                             @Valid CrearTransferenciaRequest request,
                             @Context UriInfo uriInfo) {
        TransferenciaResponse transferencia = transferenciaService.ejecutar(request);
        URI ubicacion = uriInfo.getAbsolutePathBuilder().path(String.valueOf(transferencia.id())).build();
        return Response.created(ubicacion).entity(transferencia).build();
    }

    @GET
    @Path("/{id}")
    public TransferenciaResponse obtenerPorId(@PathParam("id") Long id) {
        return transferenciaService.obtenerPorId(id);
    }
}
