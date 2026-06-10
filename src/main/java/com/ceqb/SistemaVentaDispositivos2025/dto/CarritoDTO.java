package com.ceqb.SistemaVentaDispositivos2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CarritoDTO {
    private Long id;
    private Long usuarioId;
    private Long productoId;
    private String modeloProducto;
    private String imagenProducto; // Add image if needed for the view
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

}
