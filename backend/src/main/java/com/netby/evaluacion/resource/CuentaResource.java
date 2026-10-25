package com.netby.evaluacion.resource;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.dto.response.PaginaResponse;
import com.netby.evaluacion.dto.response.ResumenCuentaResponse;
import com.netby.evaluacion.resource.api.CuentaApi;
import com.netby.evaluacion.service.CuentaService;
import com.netby.evaluacion.service.ResumenCuentaService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;

/**
 * Rutas, validaciones y documentación se declaran en {@link CuentaApi}; aquí solo se delega.
 */
public class CuentaResource implements CuentaApi {

    private final CuentaService cuentaService;
    private final ResumenCuentaService resumenCuentaService;

    @Inject
    public CuentaResource(CuentaService cuentaService, ResumenCuentaService resumenCuentaService) {
        this.cuentaService = cuentaService;
        this.resumenCuentaService = resumenCuentaService;
    }

    @Override
    public Response registrar(CrearCuentaRequest request, UriInfo uriInfo) {
        CuentaResponse cuenta = cuentaService.registrar(request);
        URI ubicacion = uriInfo.getAbsolutePathBuilder().path(String.valueOf(cuenta.id())).build();
        return Response.created(ubicacion).entity(cuenta).build();
    }

    @Override
    public PaginaResponse<CuentaResponse> listar(int pagina, int tamanio) {
        return cuentaService.listar(pagina, tamanio);
    }

    @Override
    public CuentaResponse obtenerPorId(Long id) {
        return cuentaService.obtenerPorId(id);
    }

    @Override
    public ResumenCuentaResponse obtenerResumen(Long id) {
        return resumenCuentaService.obtener(id);
    }
}
