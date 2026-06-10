package com.ceqb.SistemaVentaDispositivos2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetallePedidoDTO implements Serializable {
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private String imagenProducto; // Agrega la imagen si la necesitas en el historial
    private int cantidad;
    private BigDecimal precioUnitario;

    // Getter calculado para el subtotal
    public BigDecimal getSubtotal() {
        if (precioUnitario != null) {
            return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
        return BigDecimal.ZERO;
    }
}
