package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.CargoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Cargo;

public class CargoMapper {

    public static CargoDTO toDTO(Cargo cargo) {
        if (cargo == null) {
            return null;
        }

        return CargoDTO.builder()
                .idcargo(cargo.getIdcargo())
                .nombreCargo(cargo.getNombrecargo())
                .build();
    }

    public static Cargo toEntity(CargoDTO dto) {
        if (dto == null) {
            return null;
        }

        Cargo cargo = new Cargo();
        cargo.setIdcargo(dto.getIdcargo());
        cargo.setNombrecargo(dto.getNombreCargo());
        return cargo;
    }
}
