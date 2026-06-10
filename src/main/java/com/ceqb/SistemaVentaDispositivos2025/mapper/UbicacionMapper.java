package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.UbicacionDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Ubicacion;
import org.springframework.stereotype.Component;

@Component
public class UbicacionMapper {
    public UbicacionDTO toDTO(Ubicacion entity) {

        if (entity == null) {
            return null;
        }

        return UbicacionDTO.builder()
                .departamento(entity.getDepartamento())
                .provincia(entity.getProvincia())
                .distrito(entity.getDistrito())
                .build();
    }

    public void toEntity(UbicacionDTO dto, Ubicacion entity) {
        entity.setDepartamento(dto.getDepartamento());
        entity.setProvincia(dto.getProvincia());
        entity.setDistrito(dto.getDistrito());
    }
}
