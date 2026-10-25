package com.netby.evaluacion.mapper;

import com.netby.evaluacion.dto.response.TransferenciaResponse;
import com.netby.evaluacion.entity.Transferencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TransferenciaMapper {

    @Mapping(target = "numeroCuentaOrigen", source = "cuentaOrigen.numeroCuenta")
    @Mapping(target = "numeroCuentaDestino", source = "cuentaDestino.numeroCuenta")
    TransferenciaResponse toResponse(Transferencia transferencia);
}
