package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.ProductoImagen;
import org.springframework.stereotype.Component;

@Component
public class ProductoImagenMapper {

    public ProductoImagen toEntity(String ruta, Producto producto, int orden) {
        return ProductoImagen.builder()
                .producto(producto)
                .rutaImagen(ruta)
                .orden(orden)
                .build();
    }
}
