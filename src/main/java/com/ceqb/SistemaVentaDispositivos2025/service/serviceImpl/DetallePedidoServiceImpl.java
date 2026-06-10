package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.DetallePedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.DetallePedidoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.repository.DetallePedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.DetallePedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DetallePedidoServiceImpl implements DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoMapper detallePedidoMapper;

    @Override
    public List<DetallePedidoDTO> findByPedidoId(Long pedidoId) {
        // 1. Encontrar el pedido
        Optional<Pedido> pedidoOptional = pedidoRepository.findById(pedidoId);

        // 2. Si el pedido existe, obtener sus detalles y mapearlos a DTO
        if (pedidoOptional.isPresent()) {
            Pedido pedido = pedidoOptional.get();
            // Accede a la lista de detalles directamente desde la entidad Pedido
            return detallePedidoMapper.toDtoList(pedido.getDetalles());
        }

        // 3. Si el pedido no existe, regresa una lista vacía o maneja el error
        return null;
    }
}
