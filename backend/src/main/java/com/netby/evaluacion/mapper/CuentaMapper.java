package com.netby.evaluacion.mapper;

import com.netby.evaluacion.dto.request.CrearCuentaRequest;
import com.netby.evaluacion.dto.response.CuentaResponse;
import com.netby.evaluacion.entity.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CuentaMapper {

    Cuenta toEntity(CrearCuentaRequest request);

    CuentaResponse toResponse(Cuenta cuenta);
}
