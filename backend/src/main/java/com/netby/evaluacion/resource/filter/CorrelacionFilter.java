package com.netby.evaluacion.resource.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

@Provider
public class CorrelacionFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public static final String CABECERA = "X-Request-Id";
    static final String CLAVE_MDC = "requestId";

    // Evita inyección de logs con valores enviados por el cliente
    private static final Pattern ID_VALIDO = Pattern.compile("[A-Za-z0-9-]{1,64}");

    @Override
    public void filter(ContainerRequestContext solicitud) {
        String recibido = solicitud.getHeaderString(CABECERA);
        String requestId = recibido != null && ID_VALIDO.matcher(recibido).matches()
                ? recibido
                : UUID.randomUUID().toString();

        solicitud.setProperty(CLAVE_MDC, requestId);
        MDC.put(CLAVE_MDC, requestId);
    }

    @Override
    public void filter(ContainerRequestContext solicitud, ContainerResponseContext respuesta) {
        Object requestId = solicitud.getProperty(CLAVE_MDC);
        if (requestId != null) {
            respuesta.getHeaders().putSingle(CABECERA, requestId);
        }
        MDC.remove(CLAVE_MDC);
    }
}
