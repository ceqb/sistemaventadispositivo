package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.MarcaDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Marca;
import org.springframework.stereotype.Component;

@Component
public class MarcaMapper {

    public MarcaDTO toDTO(Marca marca) {
        MarcaDTO dto = new MarcaDTO();
        dto.setId_marca(marca.getId_marca());
        dto.setNombreMarca(marca.getNombreMarca());
        dto.setEstado(marca.isEstado());
        return dto;
    }

    public Marca toEntity(MarcaDTO dto) {
        Marca marca = new Marca();
        marca.setId_marca(dto.getId_marca());
        marca.setNombreMarca(dto.getNombreMarca());
        marca.setEstado(dto.isEstado());
        return marca;
    }
}
