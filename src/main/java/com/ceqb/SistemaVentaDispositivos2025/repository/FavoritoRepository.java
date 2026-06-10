package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.Favorito;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoritoRepository extends JpaRepository<Favorito, Long> {

    Optional<Favorito> findByUsuario_IdAndProducto_Id(Long usuarioId, Long productoId);

    @Query("SELECT f.producto FROM Favorito f WHERE f.usuario.id = :usuarioId")
    List<Producto> findProductosFavoritos(@Param("usuarioId") Long usuarioId);

    @Query("SELECT f FROM Favorito f " +
            "JOIN FETCH f.producto p " +           // Trae el producto de golpe
            "LEFT JOIN FETCH p.categorias " +      // Trae las categorías de golpe
            "WHERE f.usuario.id = :usuarioId")
    List<Favorito> findAllByUsuarioIdWithProductAndCategories(@Param("usuarioId") Long usuarioId);


    int countByUsuarioId(Long usuarioId);

}
