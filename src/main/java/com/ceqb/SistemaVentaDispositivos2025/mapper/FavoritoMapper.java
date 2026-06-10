package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.FavoritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Favorito;
import org.springframework.stereotype.Component;

@Component
public class FavoritoMapper {
    public FavoritoDTO toDto(Favorito favorito) {
        if (favorito == null) return null;

        return FavoritoDTO.builder()
                .id(favorito.getId())
                .usuarioId(favorito.getUsuario().getId().longValue())
                .dispositivoId(favorito.getProducto().getId())

                .productoModelo(favorito.getProducto().getModelo_dpc())
                .rutaFoto(favorito.getProducto().getRutaFoto_dpc())

                .fechaRegistro(favorito.getFechaRegistro())
                .build();
    }
}
