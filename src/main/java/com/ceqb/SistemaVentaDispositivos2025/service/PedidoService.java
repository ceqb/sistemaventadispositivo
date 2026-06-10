package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface PedidoService {


    /* Métodos para administrador */
    List<PedidoDTO> obtenerTodosLosPedidos();
    PedidoDTO obtenerPedidoPorId(Long pedidoId);
    void aprobarPedido(Long pedidoId);
    void cancelarPedidoPorAdmin(Long pedidoId);
    void actualizarEstado(Long pedidoId, String estadoPago, String paymentId);
    void actualizarEstadoPago(Long id, String nuevoEstadoPago);
    PedidoDTO avanzarEstadoPedido(Long pedidoId);
    PedidoDTO actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado);
    public List<EstadoPedido> obtenerEstadosValidos(PedidoDTO pedido);
    boolean puedeAvanzarEstado(PedidoDTO pedido);
    //boolean puedeAvanzarEstadoRepartidor(PedidoDTO pedido, UsuarioDTO usuarioActual, boolean esAdmin);
    /* Métodos para clientes */
    List<PedidoDTO> obtenerHistorialPedidosCliente(UsuarioDTO usuarioDTO);
    PedidoDTO obtenerPedidoPorNumeroYUsuario(String numeroPedido, UsuarioDTO usuarioDTO);
    void cancelarPedidoPorUsuario(Long pedidoId, UsuarioDTO usuarioDTO);
    BigDecimal obtenerTotalCarrito(UsuarioDTO usuarioDTO);

    /* Métodos para Repartidor */
    void marcarEntregadoYPagado(Long pedidoId, EstadoPedido estadoNotificacion);
    PedidoDTO obtenerPedidoPorNumero(String numeroPedido);
    void confirmarEntregaPorQr(String qrToken, UsuarioDTO repartidor);

    List<PedidoDTO> obtenerPedidosRetrasados();
    List<PedidoDTO> obtenerPedidosPorRepartidor(Long repartidorId);

    List<PedidoDTO> obtenerPedidosDisponibles();

    void asignarPedidoARepartidor(Long pedidoId, Long repartidorId);

    void liberarPedido(Long pedidoId, Long repartidorId);


    /*Métodos relacionados con Mercado Pago webhook*/
    Map<String, String> crearPedidoYPreferencia(UsuarioDTO usuarioDTO) throws MPException, MPApiException;
    void procesarWebhookDePago(Map<String, Object> payload);
    Map<String, String> crearNuevoPedido(Usuario usuario, List<Carrito> itemsCarrito) throws MPException, MPApiException;


    // Nuevo método
    //PedidoDTO obtenerPedidoPorIdYUsuario(Long pedidoId, UsuarioDTO usuarioDTO);
    // Nuevo método para obtener el total del carrito
    void cancelarPedidoPendiente(UsuarioDTO usuarioDTO);
    void liberarReserva(Pedido pedido);
    boolean debeLiberarReserva(Pedido pedido);
    void confirmarReserva(Pedido pedido);
    void notify(PedidoDTO pedido, UsuarioDTO usuario);
    void notificarPedidoAsignado();
    long contarPedidosDisponibles();
    void notificarEntrega(Long pedidoId, BigDecimal total, String repartidorNombre);
    long contarPedidosEnEstado(EstadoPedido estado);

    List<PedidoDTO> obtenerPedidosEnEstado(EstadoPedido estado);
    void marcarEntregaComoRevisada(Long pedidoId);
    String generarQrToken();
    byte[] generarQrImagen(String texto);

    void enviarCorreoConfirmacionEntrega(Pedido pedido);
}
