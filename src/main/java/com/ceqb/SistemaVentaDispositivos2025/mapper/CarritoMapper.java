package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CarritoMapper {
    public static CarritoDTO toDto(Carrito carrito) {
        return CarritoDTO.builder()
                .id(carrito.getId())
                .usuarioId((long) carrito.getUsuario().getId())
                .productoId(carrito.getProducto().getId())
                .modeloProducto(carrito.getProducto().getModelo_dpc())
                .imagenProducto(carrito.getProducto().getRutaFoto_dpc()) // Assuming you have this field
                .cantidad(carrito.getCantidad())
                .precioUnitario(carrito.getPrecioUnitario())
                .subtotal(carrito.getSubtotal())
                .build();
    }

    public static List<CarritoDTO> toDtoList(List<Carrito> carritoList) {
        return carritoList.stream()
                .map(CarritoMapper::toDto)
                .collect(Collectors.toList());
    }
}
