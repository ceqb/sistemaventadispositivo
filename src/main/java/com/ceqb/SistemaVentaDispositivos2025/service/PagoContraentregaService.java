package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;

import java.util.List;

public interface PagoContraentregaService {

    String iniciarPago(Long pedidoId);
    void confirmarPago(Pedido pedido);
    PedidoDTO crearPedidoContraentrega(UsuarioDTO usuarioDTO, String direccionEntrega);
    PedidoDTO crearNuevoPedidoContraEntrega(Usuario usuario, List<Carrito> itemsCarrito, String direccionEntrega);


}
