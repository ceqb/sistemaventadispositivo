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
        // 🔥 OBTENER EL NOMBRE DE USUARIO PARA LA EXCEPCIÓN
        // NOTA: Si 'getNombreUsuario()' no existe, usa el getter correcto de tu entidad Usuario (ej: getUsername())
        String nombreUsuario = usuario.getUsuario();
        // ============================================================
        // 1. VALIDAR SI TIENE PEDIDO BLOQUEADO
        // ============================================================
        Optional<Pedido> pedidoActivoOpt = pedidoRepository.findTopByUsuarioOrderByIdDesc(usuario);

        if (pedidoActivoOpt.isPresent()) {
            Pedido p = pedidoActivoOpt.get();

            boolean pedidoEnProceso = p.getEstadoPedido() == EstadoPedido.EN_CAMINO ||
                    (p.getEstadoPago().equals(EstadoPago.PENDIENTE.name()) && p.getEstadoPedido() == EstadoPedido.CONFIRMADO);

            boolean pedidoBloqueado =
                    //p.getEstadoPedido() == EstadoPedido.EN_CAMINO ||
                            (p.getEstadoPago().equals(EstadoPago.APROBADO.name()) &&
                                    !p.getEstadoPedido().esFinal());

            if (pedidoBloqueado) {
                // 🚨 APLICAR EXCEPCIÓN: Si NO es "cquiroz", lanza la prohibición.
                if (!nombreUsuario.equals("cquiroz")) {
                    throw new IllegalStateException(
                            "Tu pedido anterior está en camino o ya fue pagado. No puedes generar una nueva compra."
                    );
                }
            }
        }
        // ============================================================
        // 2. BUSCAR PEDIDO PENDIENTE (POSIBLE REUSO)
        // ============================================================
        Optional<Pedido> pedidoPendienteOpt =
                pedidoRepository.findTopByUsuarioAndEstadoPagoOrderByIdDesc(usuario, EstadoPago.PENDIENTE.name());

        if (pedidoPendienteOpt.isPresent()) {
            Pedido pedidoPendiente = pedidoPendienteOpt.get();
            // ⏱️ VALIDACIÓN DE TIEMPO (1 MINUTO TEMPORAL)
            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.PENDIENTE.name())
                    && pedidoPendiente.getEstadoPedido() == EstadoPedido.CREADO
                    && superoTiempoConfirmacion(pedidoPendiente, 1)) {

                pedidoPendiente.setEstadoPedido(EstadoPedido.CONFIRMADO);
                pedidoRepository.save(pedidoPendiente);

                throw new IllegalStateException(
                        "Tienes un pedido anterior que esta siendo creado. No puedes generar una nueva compra por unos minutos."
                );
            }
            // ❌ 1. Si el pago ya fue aprobado en ese pedido → no se cancela NUNCA
            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.APROBADO.name())) {
                throw new IllegalStateException("No puedes generar una nueva compra. El pago del pedido anterior ya fue aprobado.");
            }

            // ❌ 2. Si el pedido está CONFIRMADO → bloquea creación
            if (pedidoPendiente.getEstadoPedido() == EstadoPedido.CONFIRMADO) {
                throw new IllegalStateException("No puedes generar una nueva compra. El pago del pedido anterior ya fue confirmado.");
            }

            if (pedidoPendiente.getEstadoPago().equals(EstadoPago.PENDIENTE.name())&&pedidoPendiente.getEstadoPedido().equals(EstadoPedido.ENTREGADO)){
                throw new IllegalStateException("No puedes generar una nueva compra. El pago se esta procesando.");
            }

            // ❌ 2. Si el pedido ya está en camino → no se cancela
            if (pedidoPendiente.getEstadoPedido().equals(EstadoPedido.EN_CAMINO)) {
                // 🚨 CORRECCIÓN CLAVE AQUÍ
                if (!nombreUsuario.equals("cquiroz")) {
                    throw new IllegalStateException("No puedes generar una nueva compra. Tu pedido anterior está en camino.");
                }
            }
            // ❌ 3. Si ya está entregado → No debería considerarse pendiente pero igual aseguramos
            if (pedidoPendiente.getEstadoPedido().equals(EstadoPedido.ENTREGADO)) {
                // No cancelamos ni interactuamos con pedidos entregados
                pedidoPendienteOpt = Optional.empty();
            }
            // ✅ Comparamos carrito actual con detalles del pedido pendiente
            List<DetallePedido> detalles = detallePedidoRepository.findByPedido(pedidoPendiente);

            boolean carritoIgual = detalles.size() == carritoActual.size()
                    && detalles.stream().allMatch(d ->
                    carritoActual.stream().anyMatch(c ->
                            Objects.equals(c.getProducto().getId(), d.getProducto().getId())
                                    && c.getCantidad() == d.getCantidad()
                    )
            );
            if (carritoIgual) {
                // ✅ REUTILIZAR EL PEDIDO
                pedidoPendiente.setDireccionEntrega(direccionEntrega);
                pedidoRepository.save(pedidoPendiente);

                PedidoDTO dto = pedidoMapper.toDto(pedidoPendiente);
                dto.setMensaje("Se reutilizó tu pedido anterior porque tienes uno igual en proceso.");

                // 🔥 CORRECCIÓN: Vaciar el carrito aquí.
                carritoRepository.deleteByUsuario(usuario);
                return dto;
            }else {
                // 🚨 CORRECCIÓN CLAVE: Solo abandonar si NO es cquiroz
                if (!nombreUsuario.equals("cquiroz")) {
                    // 🔥 Abandonamos pedido viejo
                    pedidoService.liberarReserva(pedidoPendiente);
                    pedidoPendiente.setEstadoPago(EstadoPago.ABANDONADO.name());
                    pedidoPendiente.setEstadoPedido(EstadoPedido.ABANDONADO);
                    pedidoRepository.save(pedidoPendiente);
                }

                // 🚀 Creamos nuevo pedido
                return crearNuevoPedidoContraEntrega(usuario, carritoActual, direccionEntrega);
            }
            //pedidoPendiente.setDireccionEntrega(direccionEntrega);
            // ⚡ Si el carrito no cambió → devolvemos DTO del pedido pendiente
           // return pedidoMapper.toDto(pedidoPendiente);
        }
        // ============================================================
        // 3. SI NO EXISTE PENDIENTE → crear nuevo pedido
        // ============================================================

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
