package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;

import java.util.List;

public interface FavoritoService {

    boolean toggleFavorito(Long usuarioId, Long productoId);
    boolean esFavorito(Long usuarioId, Long productoId);
    List<ProductoDTO> obtenerFavoritosUsuario(Long usuarioId);

    void eliminarPorProductoYUsuario(Long productoId, Long usuarioId);
    Integer contarFavoritosPorUsuario(Long usuarioId);
}
