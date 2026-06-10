package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.config.HashidsUtil;
import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.ProductoImagen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductoMapper {

    private final HashidsUtil hashidsUtil;
    @Autowired
    public ProductoMapper(HashidsUtil hashidsUtil) {
        this.hashidsUtil = hashidsUtil;
    }
    //ORINGIAL
    // De entidad a DTO
    public  ProductoDTO toDTO(Producto producto) {
        if (producto == null) {
            return null;
        }
        ProductoDTO dto = new ProductoDTO();
        dto.setId_dpc(producto.getId());
        dto.setIdHash(hashidsUtil.encode(producto.getId()));
        dto.setRutaFoto_dpc(producto.getRutaFoto_dpc());
        dto.setRutaVideo_dpc(producto.getRutaVideo_dpc());
        dto.setSerie_dpc(producto.getSerie_dpc());
        dto.setModelo_dpc(producto.getModelo_dpc());
        dto.setDescripcion_dpc(producto.getDescripcion_dpc());
        dto.setDestacado(producto.isDestacado());
        dto.setPrecio_dpc(producto.getPrecio_dpc());
        dto.setPrecioDinamico(producto.getPrecioDinamico());
        dto.setInventarioTotal(producto.getInventarioTotal());
        dto.setInventarioReservado(producto.getInventarioReservado());
        int disponible = 0;
        if (producto.getInventarioTotal() != null) {
            int reservado = producto.getInventarioReservado() != null
                    ? producto.getInventarioReservado()
                    : 0;
            disponible = producto.getInventarioTotal() - reservado;
        }
        dto.setInventarioDisponible(disponible);
        dto.setVentasRecientes(producto.getVentasRecientes());
        dto.setClics(producto.getClics());
        dto.setDiasEnStock(producto.getDiasEnStock());
        dto.setId_marca(producto.getMarca() != null ? producto.getMarca().getId_marca() : null);
        dto.setNombreMarca(producto.getMarca() != null ? producto.getMarca().getNombreMarca() : null);

        // ID DE LA CATEGORÍA
        if (producto.getCategorias() != null) {
            List<Long> ids = producto.getCategorias().stream()
                    .map(Categoria::getId)
                    .collect(Collectors.toList());
            dto.setCategoriaIds(ids);

            List<String> nombres = producto.getCategorias().stream()
                    .map(Categoria::getNombreCategoria)
                    .collect(Collectors.toList());
            dto.setNombresCategorias(nombres);   // ← campo renombrado
        }

        if (producto.getImagenes() != null && !producto.getImagenes().isEmpty()) {
            dto.setImagenes(
                    producto.getImagenes().stream()
                            .sorted(Comparator.comparingInt(ProductoImagen::getOrden))
                            .map(ProductoImagen::getRutaImagen)
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }

    // De DTO a entidad
    // Convertir de DTO a entidad usando la entidad Categoria ya existente
    public  Producto toEntity(ProductoDTO dto) {
        if (dto == null) {
            return null;
        }
        Producto producto = new Producto();
        producto.setId(dto.getId_dpc());
        producto.setRutaFoto_dpc(dto.getRutaFoto_dpc());
        producto.setRutaVideo_dpc(dto.getRutaVideo_dpc());
        producto.setSerie_dpc(dto.getSerie_dpc());
        producto.setModelo_dpc(dto.getModelo_dpc());
        producto.setDescripcion_dpc(dto.getDescripcion_dpc());
        producto.setDestacado(dto.isDestacado());
        producto.setPrecio_dpc(dto.getPrecio_dpc());

        // 🧱 MODELO C
        producto.setInventarioTotal(dto.getInventarioTotal());
        producto.setInventarioReservado(dto.getInventarioReservado());
        producto.setClics(dto.getClics());
        return producto;
    }
}