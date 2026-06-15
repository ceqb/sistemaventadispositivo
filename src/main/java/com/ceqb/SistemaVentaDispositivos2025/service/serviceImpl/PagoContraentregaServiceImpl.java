package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.PedidoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.*;
import com.ceqb.SistemaVentaDispositivos2025.repository.CarritoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.DetallePedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.PagoContraentregaService;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoContraentregaServiceImpl implements PagoContraentregaService {

    private final UsuarioRepository usuarioRepository;
    private final CarritoRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoMapper pedidoMapper;
    private final PedidoService pedidoService;
private final ProductoService productoService;

    @Override
    @Transactional
    public String iniciarPago(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        pedido.setEstadoPago(EstadoPago.PENDIENTE.name());
        //pedido.setEstadoPedido(EstadoPedido.PROCESANDO);

        pedido.setMetodoPago(MetodoPago.CONTRAENTREGA);
        pedidoRepository.save(pedido);

        return "El pago sera realizado en la entrega del pedido.";
    }

    @Override
    @Transactional
    public void confirmarPago(Pedido pedido) {
        pedido.setEstadoPago(EstadoPago.APROBADO.name());    // cliente pagó al repartidor
        pedido.setEstadoPedido(EstadoPedido.ENTREGADO);
        pedidoService.confirmarReserva(pedido);// pedido entregado
        pedidoRepository.save(pedido);                       // persistir cambios
    }

    @Override
    @Transactional
    public PedidoDTO crearPedidoContraentrega(UsuarioDTO usuarioDTO, String direccionEntrega) {

        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Carrito> carritoActual = carritoRepository.findByUsuario(usuario);

        if (carritoActual.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        // ✅ Usamos el flag del DTO, no el nombre de usuario
        boolean tieneBypass = Boolean.TRUE.equals(usuarioDTO.getBypassValidaciones());

        // ============================================================
        // 1. VALIDAR SI TIENE PEDIDO BLOQUEADO
        // ============================================================
        Optional<Pedido> pedidoActivoOpt = pedidoRepository.findTopByUsuarioOrderByIdDesc(usuario);

        if (pedidoActivoOpt.isPresent()) {
            Pedido p = pedidoActivoOpt.get();

            boolean pedidoBloqueado = p.getEstadoPago().equals(EstadoPago.APROBADO.name())
                    && !p.getEstadoPedido().esFinal();

            if (pedidoBloqueado && !tieneBypass) {
                throw new IllegalStateException(
                        "Tu pedido anterior está en camino o ya fue pagado. No puedes generar una nueva compra."
                );
            }
        }

        // ============================================================
        // 2. BUSCAR PEDIDO PENDIENTE (POSIBLE REUSO)
        // ============================================================
        Optional<Pedido> pedidoPendienteOpt =
                pedidoRepository.findTopByUsuarioAndEstadoPagoOrderByIdDesc(
                        usuario, EstadoPago.PENDIENTE.name());

        if (pedidoPendienteOpt.isPresent()) {
            Pedido pedidoPendiente = pedidoPendienteOpt.get();

            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.PENDIENTE.name())
                    && pedidoPendiente.getEstadoPedido() == EstadoPedido.CREADO
                    && superoTiempoConfirmacion(pedidoPendiente, 1)) {

                pedidoPendiente.setEstadoPedido(EstadoPedido.CONFIRMADO);
                pedidoRepository.save(pedidoPendiente);

                throw new IllegalStateException(
                        "Tienes un pedido anterior que está siendo creado. Espera unos minutos."
                );
            }

            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.APROBADO.name())) {
                throw new IllegalStateException(
                        "No puedes generar una nueva compra. El pago del pedido anterior ya fue aprobado.");
            }

            if (pedidoPendiente.getEstadoPedido() == EstadoPedido.CONFIRMADO) {
                throw new IllegalStateException(
                        "No puedes generar una nueva compra. El pago del pedido anterior ya fue confirmado.");
            }

            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.PENDIENTE.name())
                    && pedidoPendiente.getEstadoPedido().equals(EstadoPedido.ENTREGADO)) {
                throw new IllegalStateException(
                        "No puedes generar una nueva compra. El pago se está procesando.");
            }

            if (pedidoPendiente.getEstadoPedido().equals(EstadoPedido.EN_CAMINO) && !tieneBypass) {
                throw new IllegalStateException(
                        "No puedes generar una nueva compra. Tu pedido anterior está en camino.");
            }

            if (pedidoPendiente.getEstadoPedido().equals(EstadoPedido.ENTREGADO)) {
                pedidoPendienteOpt = Optional.empty();
            } else {
                List<DetallePedido> detalles = detallePedidoRepository.findByPedido(pedidoPendiente);

                boolean carritoIgual = detalles.size() == carritoActual.size()
                        && detalles.stream().allMatch(d ->
                        carritoActual.stream().anyMatch(c ->
                                Objects.equals(c.getProducto().getId(), d.getProducto().getId())
                                        && c.getCantidad() == d.getCantidad()
                        )
                );

                if (carritoIgual) {
                    pedidoPendiente.setDireccionEntrega(direccionEntrega);
                    pedidoRepository.save(pedidoPendiente);

                    carritoRepository.deleteByUsuario(usuario);

                    PedidoDTO dto = pedidoMapper.toDto(pedidoPendiente);
                    dto.setMensaje("Se reutilizó tu pedido anterior porque tienes uno igual en proceso.");
                    return dto;
                } else {
                    if (!tieneBypass) {
                        pedidoService.liberarReserva(pedidoPendiente);
                        pedidoPendiente.setEstadoPago(EstadoPago.ABANDONADO.name());
                        pedidoPendiente.setEstadoPedido(EstadoPedido.ABANDONADO);
                        pedidoRepository.save(pedidoPendiente);
                    }
                    return crearNuevoPedidoContraEntrega(usuario, carritoActual, direccionEntrega);
                }
            }
        }

        return crearNuevoPedidoContraEntrega(usuario, carritoActual, direccionEntrega);
    }

    /**
     * 🔹 Crea un nuevo pedido con método CONTRAENTREGA
     */
    @Override
    public PedidoDTO crearNuevoPedidoContraEntrega(Usuario usuario, List<Carrito> itemsCarrito, String direccionEntrega) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccionEntrega(direccionEntrega);
        pedido.setEstadoPago(EstadoPago.PENDIENTE.name());  // Sigue siendo PENDIENTE hasta que el repartidor confirme
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        pedido.setMetodoPago(MetodoPago.CONTRAENTREGA);
        pedido.setNumeroPedido("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        pedido.setFechaPedido(LocalDateTime.now());

// 🔑 GENERACIÓN CONSISTENTE DEL QR
        String token = pedidoService.generarQrToken();
        pedido.setQrToken(token);
        pedido.setQrUsado(false);


        BigDecimal total = itemsCarrito.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        for (Carrito item : itemsCarrito) {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detallePedidoRepository.save(detalle);
            productoService.registrarVenta(item.getProducto().getId(), item.getCantidad());
        }

        // 🧹 Vaciar carrito
        carritoRepository.deleteByUsuario(usuario);

        // 🔹 Recargar pedido con todos los detalles
        Pedido pedidoCompleto = pedidoRepository.findById(pedido.getId())
                .orElseThrow(() -> new RuntimeException("No se pudo recargar el pedido."));

        // 🔹 Mapear a DTO solo después de persistir todo
        PedidoDTO pedidoDto = pedidoMapper.toDto(pedidoCompleto);
        return pedidoDto;
    }

    private boolean superoTiempoConfirmacion(Pedido pedido, long minutos) {
        LocalDateTime inicio = pedido.getFechaProcesando() != null
                ? pedido.getFechaProcesando()
                : pedido.getFechaPedido();

        return inicio != null &&
                inicio.plusMinutes(minutos).isBefore(LocalDateTime.now());
    }
}
