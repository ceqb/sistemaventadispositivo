package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.DetallePedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.DetallePedido;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DetallePedidoMapper {
    public DetallePedidoDTO toDto(DetallePedido detallePedido) {
        if (detallePedido == null) {
            return null;
        }

        return DetallePedidoDTO.builder()
                .id(detallePedido.getId())
                .productoId(detallePedido.getProducto().getId())
                .nombreProducto(detallePedido.getProducto().getModelo_dpc())
                .imagenProducto(detallePedido.getProducto().getRutaFoto_dpc()) // Asumiendo que esta es la propiedad de la imagen
                .cantidad(detallePedido.getCantidad())
                .precioUnitario(detallePedido.getPrecioUnitario())
                .build();
    }

    public List<DetallePedidoDTO> toDtoList(List<DetallePedido> detalles) {
        if (detalles == null) {
            return null;
        }
        return detalles.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
