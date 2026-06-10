package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.CargoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.CompradorDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Cargo;
import com.ceqb.SistemaVentaDispositivos2025.model.Comprador;
import com.ceqb.SistemaVentaDispositivos2025.repository.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompradorMapper {

    private final UbicacionMapper ubicacionMapper;

    public CompradorDTO toDTO(Comprador comprador) {
        if (comprador == null) {
            return null;
        }

        return CompradorDTO.builder()
                .id(comprador.getId())
                .nombre(comprador.getNombre())
                .direccion(comprador.getDireccion())
                .telefono(comprador.getTelefono())
                .cantidad(comprador.getCantidad())
                .productoId(
                        comprador.getProducto() != null
                                ? comprador.getProducto().getId()
                                : null
                )
                .pagado(comprador.isPagado())
                .total(comprador.getTotal())
                .distritoId(
                        comprador.getDistrito() != null
                                ? comprador.getDistrito().getId()
                                : null
                )
                .ubicacion(
                        comprador.getDistrito() != null
                                ? ubicacionMapper.toDTO(comprador.getDistrito())
                                : null
                )
                .build();
    }

    public Comprador toEntity(CompradorDTO dto) {
        if (dto == null) {
            return null;
        }

        Comprador comprador = new Comprador();
        comprador.setId(dto.getId());
        comprador.setNombre(dto.getNombre());
        comprador.setDireccion(dto.getDireccion());
        comprador.setTelefono(dto.getTelefono());
        comprador.setCantidad(dto.getCantidad());
        comprador.setPagado(dto.isPagado());
        comprador.setTotal(dto.getTotal());

        // ⚠ NO seteamos distrito aquí
        // Eso lo hace el Service

        return comprador;
    }
}