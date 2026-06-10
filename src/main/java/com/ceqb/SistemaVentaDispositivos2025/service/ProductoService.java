package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ProductoService {

    // Listado básico
    List<ProductoDTO> listar();

    // Búsqueda por slug/nombre amigable
    ProductoDTO obtenerPorNombre(String slug);

    // Detalle por ID (con imágenes eager fetch recomendado)
    ProductoDTO obtenerPorId(Long id);

    Page<ProductoDTO> buscar(String search, List<Long> categorias, int page, int size);

    // Creación
    ProductoDTO guardar(ProductoDTO productoDTO);

    // Actualización (incluye manejo de nuevas imágenes)
    ProductoDTO actualizar(ProductoDTO productoDTO, List<MultipartFile> nuevasImagenes);

    // Manejo de archivos (usado en controller)
    String guardarArchivo(MultipartFile archivo, String rutaDirectorio);

    // Búsqueda paginada por modelo (para search)
    Page<ProductoDTO> buscarProductosPaginado(String query, int page, int size);

    // Sugerencias autocomplete
    List<String> buscarCoincidencias(String query);

    // Productos relacionados (por categoría, excluyendo el actual)
    List<ProductoDTO> obtenerRelacionados(Long categoriaId, Long productoId);

    // Interacciones
    @Transactional
    void registrarClick(Long productoId);
    @Transactional
    void registrarVenta(Long productoId, int cantidadVendida);

    @Transactional
     void validarStock(Long productoId, int cantidadSolicitada);
    // ────────────────────────────────────────────────
    //               Paginación para tienda
    // ────────────────────────────────────────────────

    /**
     * Productos principales de la página inicial (sin filtro)
     */
    Page<ProductoDTO> obtenerProductosPaginacionPrincipal(int pagina, int size);

    /**
     * Listado genérico para tienda (puedes usarlo como fallback)
     */
    Page<ProductoDTO> listarTienda(Pageable pageable);

    /**
     * Productos de UNA categoría (implementación actual)
     */
    Page<ProductoDTO> obtenerProductosPorCategoriaPaginacion(Long categoriaId, int pagina, int size);

    /**
     * ★★★ NUEVO MÉTODO ★★★
     * Productos que pertenecen a CUALQUIERA de las categorías indicadas
     * (filtro múltiple en la tienda)
     */
    Page<ProductoDTO> buscarPorCategorias(List<Long> categoriaIds, int pagina, int size);

    /**
     * Versión combinada (más flexible): categorías + búsqueda textual
     * Ideal si quieres permitir buscar Y filtrar por categorías al mismo tiempo
     */
    Page<ProductoDTO> buscarPorCategoriasYFiltro(
            List<Long> categoriaIds,
            String query,
            int pagina,
            int size
    );
}