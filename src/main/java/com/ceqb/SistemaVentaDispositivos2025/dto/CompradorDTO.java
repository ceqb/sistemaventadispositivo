package com.ceqb.SistemaVentaDispositivos2025.dto;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompradorDTO {

    private Long id;
    private String nombre;
    private String telefono;
    private String direccion;
    private Long cantidad;
    private Long productoId;
    private boolean pagado;
    private Double total;

    private UbicacionDTO ubicacion;
    private Long distritoId;
    // El objeto que contiene la info del producto
    private ProductoResumenDTO producto;

    @Data
    @Builder // Agregamos Builder para facilitar el mapeo
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductoResumenDTO { // <--- DEBE SER STATIC
        private String nombre;
        private Double precio;
        private String foto;
        private String descripcion;
        private Integer stock;

    }


}
