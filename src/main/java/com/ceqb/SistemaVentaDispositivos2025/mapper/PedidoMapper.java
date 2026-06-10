package com.ceqb.SistemaVentaDispositivos2025.mapper;


import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PedidoMapper {

    private final DetallePedidoMapper detallePedidoMapper;

    public PedidoDTO toDto(Pedido pedido) {
        if (pedido == null) {
            return null;
        }

        return PedidoDTO.builder()
                .id(pedido.getId())
                .usuarioId(pedido.getUsuario() != null ? pedido.getUsuario().getId() : null)
                .usuarioNombre(pedido.getUsuario() != null ? pedido.getUsuario().getNombre() : null)
                .direccionEntrega(pedido.getDireccionEntrega())
                .numeroPedido(pedido.getNumeroPedido())
                .fechaPedido(pedido.getFechaPedido())
                .total(pedido.getTotal())
                .preferenciaId(pedido.getPreferenciaId())
                .paymentId(pedido.getPaymentId())
                .metodoPago(pedido.getMetodoPago())
                .estadoPago(pedido.getEstadoPago())
                .reservaLiberada(pedido.isReservaLiberada())
                .qrToken(pedido.getQrToken())
                .qrUsado(pedido.getQrUsado())
                .qrUsadoEn(pedido.getQrUsadoEn())

                .fechaEntrega(pedido.getFechaEntrega())
                .fechaProcesando(pedido.getFechaProcesando())
                .fechaAsignacion(pedido.getFechaAsignacion())
                .repartidorId(pedido.getRepartidor() != null ? pedido.getRepartidor().getId() : null)
                .repartidorNombre(pedido.getRepartidor() != null ? pedido.getRepartidor().getNombre() : null)

                .estadoPedido(pedido.getEstadoPedido())
                //.repartidor(pedido.getRepartidor())
                .detalles(detallePedidoMapper.toDtoList(pedido.getDetalles())) // Convierte los detalles

                .build();
    }

    public List<PedidoDTO> toDtoList(List<Pedido> pedidos) {
        if (pedidos == null) {
            return null;
        }
        return pedidos.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
