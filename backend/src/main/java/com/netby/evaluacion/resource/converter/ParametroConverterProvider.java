package com.netby.evaluacion.resource.converter;

import com.netby.evaluacion.exception.ParametroInvalidoException;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.stream.Collectors;

// Responde 400 en lugar del 404 que JAX-RS devuelve si un parámetro no se puede convertir
@Provider
public class ParametroConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> ParamConverter<T> getConverter(Class<T> tipo, Type tipoGenerico, Annotation[] anotaciones) {
        String parametro = nombreParametro(anotaciones);
        if (tipo == LocalDate.class) {
            return (ParamConverter<T>) new FechaConverter(parametro);
        }
        if (tipo.isEnum()) {
            return new EnumConverter(tipo, parametro);
        }
        return null;
    }

    private static String nombreParametro(Annotation[] anotaciones) {
        for (Annotation anotacion : anotaciones) {
            if (anotacion instanceof QueryParam queryParam) {
                return queryParam.value();
            }
            if (anotacion instanceof PathParam pathParam) {
                return pathParam.value();
            }
        }
        return "parámetro";
    }

    private record FechaConverter(String parametro) implements ParamConverter<LocalDate> {

        @Override
        public LocalDate fromString(String valor) {
            if (valor == null || valor.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(valor);
            } catch (DateTimeParseException e) {
                throw new ParametroInvalidoException(parametro, "Debe ser una fecha con formato yyyy-MM-dd");
            }
        }

        @Override
        public String toString(LocalDate fecha) {
            return fecha == null ? null : fecha.toString();
        }
    }

    private record EnumConverter<E extends Enum<E>>(Class<E> tipo, String parametro) implements ParamConverter<E> {

        @Override
        public E fromString(String valor) {
            if (valor == null || valor.isBlank()) {
                return null;
            }
            try {
                return Enum.valueOf(tipo, valor.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                String permitidos = Arrays.stream(tipo.getEnumConstants())
                        .map(Enum::name)
                        .collect(Collectors.joining(", "));
                throw new ParametroInvalidoException(parametro, "Valor no permitido. Valores aceptados: " + permitidos);
            }
        }

        @Override
        public String toString(E valor) {
            return valor == null ? null : valor.name();
        }
    }
}
