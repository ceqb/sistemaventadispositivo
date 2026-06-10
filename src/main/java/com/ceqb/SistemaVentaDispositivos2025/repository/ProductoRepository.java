package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /*Page<Producto> findByDestacadoTrue(Pageable pageable);

    List<Producto> findTop15ByOrderByFechaCreacionDesc();*/

    /*@Query("SELECT MAX(p.id) FROM Producto p")
    Long findMaxId();*/

    // Producto más clickeado (top 1 paginado)
    /*@Query("SELECT p FROM Producto p ORDER BY p.clics DESC")
    Page<Producto> findTop1ByClics(Pageable pageable);*/

    Producto findTopByOrderByClicsDesc();

    List<Producto> findTop10ByOrderByVentasRecientesDesc();

    // Producto más vendido (top 1)
    Producto findTop1ByOrderByVentasRecientesDesc();

    // Top 10 más clickeados
    List<Producto> findTop10ByOrderByClicsDesc();

    long count();

    // ────────────────────────────────────────────────
    //          CONSULTAS CORRECTAS PARA CATEGORÍAS
    // ────────────────────────────────────────────────

    /**
     * Productos que pertenecen a una categoría específica (excluyendo el producto actual)
     * Útil para "Recomendados en la misma categoría"
     */
    @Query("SELECT p FROM Producto p " +
            "JOIN p.categorias c " +
            "WHERE c.id = :categoriaId AND p.id <> :productoId " +
            "ORDER BY p.ventasRecientes DESC")
    List<Producto> findRecomendadosPorCategoria(
            @Param("categoriaId") Long categoriaId,
            @Param("productoId") Long productoId);

    /**
     * Productos que pertenecen a una categoría específica (con paginación)
     */
    @Query("SELECT DISTINCT p FROM Producto p " +
            "JOIN FETCH p.categorias c " +
            "WHERE c.id = :categoriaId")
    Page<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId, Pageable pageable);

    /**
     * Versión sin paginación (si la necesitas)
     */
    @Query("SELECT DISTINCT p FROM Producto p " +
            "JOIN FETCH p.categorias c " +
            "WHERE c.id = :categoriaId")
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);


    // ProductoRepository.java
    // Listado general (admin o tienda sin filtro)
    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN p.categorias ORDER BY p.id DESC")
    Page<Producto> findAllWithCategorias(Pageable pageable);

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN p.categorias c WHERE c.id IN :ids")
    Page<Producto> findByCategoriasWithCategorias(@Param("ids") Collection<Long> ids, Pageable pageable);
    /**
     * Productos que pertenecen a VARIAS categorías (todas ellas)
     * Ejemplo: productos que tienen tanto "Celulares" como "Ofertas"
     */
    @Query("SELECT DISTINCT p FROM Producto p " +
            "JOIN p.categorias c1 " +
            "JOIN p.categorias c2 " +
            "WHERE c1.id = :cat1 AND c2.id = :cat2")
    List<Producto> findByCategoriasIds(
            @Param("cat1") Long cat1,
            @Param("cat2") Long cat2);

    // Puedes extenderlo a 3 o más categorías agregando más JOINs
    // O mejor: usar Specification o Criteria API para filtros dinámicos

    // ────────────────────────────────────────────────
    //                   BÚSQUEDAS
    // ────────────────────────────────────────────────

    String BUSQUEDA_MODELO = "SELECT p FROM Producto p " +
            "WHERE LOWER(p.modelo_dpc) LIKE LOWER(CONCAT('%', :query, '%'))";

    @Query(BUSQUEDA_MODELO)
    Page<Producto> buscarPorModelo(@Param("query") String query, Pageable pageable);

    @Query(BUSQUEDA_MODELO)
    List<Producto> buscarCoincidencias(@Param("query") String query);

    @Query("SELECT p FROM Producto p LEFT JOIN p.imagenes WHERE p.id = :id")
    Optional<Producto> findByIdWithImagenes(@Param("id") Long id);

    @Query("SELECT p FROM Producto p LEFT JOIN p.imagenes " +
            "WHERE LOWER(REPLACE(p.modelo_dpc, ' ', '-')) = LOWER(:slug)")
    Optional<Producto> findBySlugWithImagenes(@Param("slug") String slug);

    //Page<Producto> buscarPorModelo(@Param("query") String query, Pageable pageable);

    // 2. Solo por categorías múltiples (con DISTINCT para evitar duplicados)
    @Query("SELECT DISTINCT p FROM Producto p JOIN p.categorias c WHERE c.id IN :categoriaIds")
    Page<Producto> findDistinctByCategoriasIdIn(
            @Param("categoriaIds") Collection<Long> categoriaIds,
            Pageable pageable
    );

    // 3. Combinado: categorías + búsqueda por modelo (case-insensitive)
    @Query("""
    SELECT DISTINCT p 
    FROM Producto p 
    JOIN p.categorias c 
    WHERE c.id IN :categoriaIds 
    AND LOWER(p.modelo_dpc) LIKE LOWER(CONCAT('%', :query, '%'))
""")
    Page<Producto> findDistinctByCategoriasIdInAndModeloContainingIgnoreCase(
            @Param("categoriaIds") Collection<Long> categoriaIds,
            @Param("query") String query,
            Pageable pageable
    );

    @Query("SELECT DISTINCT p FROM Producto p LEFT JOIN FETCH p.categorias " +
            "WHERE LOWER(p.modelo_dpc) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY p.id DESC")
    Page<Producto> buscarPorModeloWithCategorias(@Param("query") String query, Pageable pageable);

    @Query("""
    SELECT p FROM Producto p
    LEFT JOIN p.imagenes
    LEFT JOIN p.categorias
    WHERE LOWER(CONCAT(REPLACE(p.modelo_dpc, ' ', '-'), '-', p.id)) = LOWER(:slug)
""")
    Optional<Producto> findBySlugWithCategoriasAndImagenes(@Param("slug") String slug);

    @Query("""
    SELECT p FROM Producto p
    WHERE p.clics >= 20
    ORDER BY (p.ventasRecientes * 1.0 / p.clics) ASC
    """)
    List<Producto> findProductosConBajaConversion(Pageable pageable);
}