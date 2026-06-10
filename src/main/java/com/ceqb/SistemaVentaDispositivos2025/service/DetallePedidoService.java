package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.DetallePedidoDTO;

import java.util.List;

public interface DetallePedidoService {
    // Método para obtener los detalles de un pedido específico
    List<DetallePedidoDTO> findByPedidoId(Long pedidoId);

}
