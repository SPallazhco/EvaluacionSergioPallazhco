package com.netby.evaluacion.resource;

import com.netby.evaluacion.dto.request.CrearTransferenciaRequest;
import com.netby.evaluacion.dto.request.FiltroTransferenciasRequest;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.dto.response.TransferenciaResponse;
import com.netby.evaluacion.resource.api.TransferenciaApi;
import com.netby.evaluacion.service.TransferenciaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

public class TransferenciaResource implements TransferenciaApi {

    private final TransferenciaService transferenciaService;

    @Inject
    public TransferenciaResource(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @Override
    public Response ejecutar(CrearTransferenciaRequest request, UriInfo uriInfo) {
        TransferenciaResponse transferencia = transferenciaService.ejecutar(request);
        URI ubicacion = uriInfo.getAbsolutePathBuilder().path(String.valueOf(transferencia.id())).build();
        return Response.created(ubicacion).entity(transferencia).build();
    }

    @Override
    public PaginaResponse<TransferenciaResponse> consultarHistorial(FiltroTransferenciasRequest filtro) {
        return transferenciaService.consultarHistorial(filtro);
    }

    @Override
    public TransferenciaResponse obtenerPorId(Long id) {
        return transferenciaService.obtenerPorId(id);
    }
}
