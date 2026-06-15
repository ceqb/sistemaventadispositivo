package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.PedidoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.*;
import com.ceqb.SistemaVentaDispositivos2025.repository.*;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.ceqb.SistemaVentaDispositivos2025.service.TelegramService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProductoRepository productoRepository;
    private final CarritoRepository carritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoMapper pedidoMapper;
    private final MercadoPagoService mercadoPagoService;
    private final JavaMailSender mailSender;
    private final Environment env;

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    // ✅ Versión corregida de crearPedidoYPreferencia: siempre cancela el pedido anterior y crea uno nuevo.
    @Override
    public Map<String, String> crearPedidoYPreferencia(UsuarioDTO usuarioDTO)
            throws MPException, MPApiException {

        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado."));

        List<Carrito> carritoActual = carritoRepository.findByUsuario(usuario);

        // 🚫 Si no hay productos en el carrito, error
        if (carritoActual.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        Optional<Pedido> pedidoPendienteOpt =
                pedidoRepository.findTopByUsuarioAndEstadoPagoOrderByIdDesc(usuario, EstadoPago.PENDIENTE.name());

        if (pedidoPendienteOpt.isPresent()) {
            Pedido pedidoPendiente = pedidoPendienteOpt.get();

            // ✅ Comparamos carrito actual con detalles del pedido pendiente
            List<DetallePedido> detalles = detallePedidoRepository.findByPedido(pedidoPendiente);

            boolean carritoIgual = detalles.size() == carritoActual.size()
                    && detalles.stream().allMatch(d ->
                    carritoActual.stream().anyMatch(c ->
                            Objects.equals(c.getProducto().getId(), d.getProducto().getId()) // compara ids
                                    && c.getCantidad() == d.getCantidad()                           // compara cantidades
                    )
            );

            if (!carritoIgual) {
                // 🔥 Si cambió el carrito → marcamos el pedido viejo como ABANDONADO
                pedidoPendiente.setEstadoPago(EstadoPago.ABANDONADO.name());
                pedidoPendiente.setEstadoPedido(EstadoPedido.ABANDONADO);
                pedidoRepository.save(pedidoPendiente);

                // 🚀 Creamos nuevo pedido con el carrito actualizado
                return crearNuevoPedido(usuario, carritoActual);
            }

            // ⚡ Si el carrito no cambió, devolvemos la preferencia existente
            if (pedidoPendiente.getPreferenciaId() != null) {
                Preference preference = mercadoPagoService.getPreference(pedidoPendiente.getPreferenciaId());
                return Map.of(
                        "url_pago", preference.getInitPoint(),
                        "pedidoId", String.valueOf(pedidoPendiente.getId()),
                        "numeroPedido", pedidoPendiente.getNumeroPedido()
                );
            }
        }

        // 🚀 Si no existe pedido pendiente → flujo normal
        return crearNuevoPedido(usuario, carritoActual);
    }

    /**
     * 🔹 Crea un nuevo pedido con el carrito actual y genera la preferencia de pago.
     */
    @Override
    public Map<String, String> crearNuevoPedido(Usuario usuario, List<Carrito> itemsCarrito)
            throws MPException, MPApiException {

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstadoPago(EstadoPago.PENDIENTE.name());
        pedido.setEstadoPedido(EstadoPedido.CREADO);
        pedido.setMetodoPago(MetodoPago.MERCADOPAGO);

        String numeroPedido = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        pedido.setNumeroPedido(numeroPedido);

        pedido.setFechaPedido(LocalDateTime.now());

        BigDecimal total = itemsCarrito.stream()
                .map(item -> item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        pedido.setTotal(total);
        pedido = pedidoRepository.save(pedido);

        for (Carrito item : itemsCarrito) {

            Producto producto = item.getProducto();
            int disponible = producto.getInventarioTotal() - producto.getInventarioReservado();
            if (disponible < item.getCantidad()) {
                throw new RuntimeException(
                        "Stock insuficiente para " + producto.getModelo_dpc()
                );
            }

            // 🔒 RESERVAR STOCK
            int reservado = Optional.ofNullable(producto.getInventarioReservado()).orElse(0);
            producto.setInventarioReservado(reservado + item.getCantidad());
            productoRepository.save(producto);

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProducto(item.getProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detallePedidoRepository.save(detalle);
        }

        Preference preference = mercadoPagoService.createPreference(itemsCarrito, usuario.getId(), pedido.getId());
        pedido.setPreferenciaId(preference.getId());
        pedidoRepository.save(pedido);

        return Map.of(
                "url_pago", preference.getInitPoint(),
                "pedidoId", String.valueOf(pedido.getId()),
                "numeroPedido", pedido.getNumeroPedido()
        );
    }
    /*ORIGINAL*/

    // ----------------------------------------------------------------------------------------------------

    // ✅ Métodos de la API: aquí también usamos los enums para los estados
    @Override
    @Transactional
    public void cancelarPedidoPendiente(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        pedidoRepository.findTopByUsuarioAndEstadoPagoOrderByIdDesc(usuario, EstadoPago.PENDIENTE.name())
                .ifPresent(pedido -> {
                    liberarReserva(pedido);
                    pedido.setEstadoPago(EstadoPago.ABANDONADO.name());
                    pedido.setEstadoPedido(EstadoPedido.ABANDONADO);
                    pedidoRepository.save(pedido);
                    System.out.println("🚫 Pedido abandonado: " + pedido.getId());
                });
    }

    @Override
    @Transactional
    public void liberarReserva(Pedido pedido) {
        if (pedido.isReservaLiberada()) {
            System.out.println("⚠️ Reserva ya liberada, se ignora");
            return;
        }

        for (DetallePedido detalle : pedido.getDetalles()) {

            Producto producto = productoRepository.findById(
                    detalle.getProducto().getId()
            ).orElseThrow();

            int reservadoActual = Optional
                    .ofNullable(producto.getInventarioReservado())
                    .orElse(0);

            int nuevoReservado = reservadoActual - detalle.getCantidad();

            producto.setInventarioReservado(Math.max(0, nuevoReservado));

            productoRepository.save(producto);

            System.out.println("↩️ Reserva liberada -> "
                    + producto.getModelo_dpc()
                    + " | Reservado ahora: "
                    + producto.getInventarioReservado());
        }

        pedido.setReservaLiberada(true);
    }

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private TelegramService telegramService;

    @Override
    public void notify(PedidoDTO pedido , UsuarioDTO usuario) {
        // 1. Notificación por WebSocket
        Map<String, Object> data = new HashMap<>();
        data.put("mensaje", "Nuevo pedido contraentrega #" + pedido.getId()
                + " realizado por " + usuario.getNombre());
        data.put("pedidoId", pedido.getId());
        data.put("total", pedido.getTotal());
        data.put("usuario", usuario.getNombre());

        messagingTemplate.convertAndSend("/topic/notificaciones", data);
       /* // 2. Notificación a Telegram
        telegramService.enviarMensaje(
                "🔔 *Nuevo Pedido Disponible*\n" +
                        "📦 Pedido #" + pedido.getId() + "\n" +
                        "💰 Total: S/ " + pedido.getTotal() + "\n" +
                        "👉 Ir a asignar: /pedidosAdmin/disponibles"
        );*/
    }
    @Override
    public void notificarEntrega(Long pedidoId, BigDecimal total, String repartidorNombre) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pedidoId", pedidoId);
        payload.put("total", total);
        payload.put("repartidor", repartidorNombre);
        messagingTemplate.convertAndSend("/topic/entregas-completadas", payload);
        System.out.println("🚀 Notificación enviada WS: " + payload);
    }
    @Override
    // ✅ Método para notificar que la cuenta debe bajar
    public void notificarPedidoAsignado() {
        // Usamos un Map simple para enviar la acción.
        Map<String, String> data = Map.of("action", "DECREMENTAR_PEDIDO");
        // Usamos un topic diferente al de nuevos pedidos para separar las responsabilidades
        messagingTemplate.convertAndSend("/topic/control-notificaciones", data);
    }

    @Override
    public long contarPedidosDisponibles() {
        return pedidoRepository.contarPedidosDisponibles();
    }

    @Override
    public long contarPedidosEnEstado(EstadoPedido estado) {
        return pedidoRepository.countByEstadoPedido(estado);
    }

    @Override
    public List<PedidoDTO> obtenerPedidosEnEstado(EstadoPedido estado) {
        // 1. Obtener la lista de Pedido (entidades) del repositorio
        List<Pedido> pedidos = pedidoRepository.findByEstadoPedido(estado);

        // 2. Mapear la lista de entidades a PedidoDTO
        // Usamos el PedidoMapper que ya tienes inyectado
        return pedidos.stream()
                .map(pedidoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void marcarEntregaComoRevisada(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // 1. Validar y cambiar el estado
        if (pedido.getEstadoPedido() != EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("El pedido no está en el estado de notificación pendiente para ser revisado.");
        }

        // 2. Cambiar el estado a ENTREGADO (Estado final y revisado)
        pedido.setEstadoPedido(EstadoPedido.ENTREGADO_REVISADO);
        pedidoRepository.save(pedido);

        // Enviar notificación WebSocket para disminuir contador
        messagingTemplate.convertAndSend(
                "/topic/control-entregas",
                Map.of("action", "DECREMENTAR_ENTREGAS")
        );
        // 3. ✅ CORRECCIÓN CRÍTICA: ENVÍO DE NOTIFICACIÓN DE DECREMENTO
        try {
            // Usamos el Map y el topic que decrece el contador en el Frontend
            Map<String, String> data = Map.of("action", "DECREMENTAR_ENTREGAS", "pedidoId", String.valueOf(pedidoId));

            // El topic de control es /topic/control-entregas (como lo definimos en el index.html)
            messagingTemplate.convertAndSend("/topic/control-entregas", data);
        } catch (Exception e) {
        }
    }
    @Override
    public String generarQrToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public byte[] generarQrImagen(String texto) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(texto, BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando QR", e);
        }
    }

    @Override
    public void enviarCorreoConfirmacionEntrega(Pedido pedido) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(env.getProperty("spring.mail.username")); // tu correo from
        message.setTo(pedido.getUsuario().getCorreo()); // email del cliente
        message.setSubject("¡Tu pedido #" + pedido.getNumeroPedido() + " ha sido entregado y pagado!");

        String cuerpo = """
                Estimado %s,

                ¡Gracias por tu compra! 

                Tu pedido #%s por S/ %s ha sido entregado exitosamente y el pago ha sido confirmado.

                Detalles:
                - Fecha de entrega: %s
                - Total pagado: S/ %s
                - Método de pago: Contraentrega

                Si tienes alguna duda, contáctanos.

                ¡Esperamos verte pronto!
                Equipo CEQBQB
                """.formatted(
                pedido.getUsuario().getNombre(),
                pedido.getNumeroPedido(),
                pedido.getTotal(),
                pedido.getFechaEntrega() != null ? pedido.getFechaEntrega().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Confirmada",
                pedido.getTotal()
        );

        message.setText(cuerpo);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("No se pudo enviar correo de confirmación al pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage());
            // No relanzamos porque el correo es secundario al flujo principal
        }
    }

    @Override
    @Transactional
    public void cancelarPedidoPorUsuario(Long pedidoId, UsuarioDTO usuarioDTO) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado."));

        if (!pedido.getUsuario().getId().equals(usuarioDTO.getId())) {
            throw new RuntimeException("No tienes permiso para cancelar este pedido.");
        }

        // ❌ CAMBIO CLAVE: Permite cancelar pedidos si el estado del pedido es CREADO o PROCESANDO.
        // También se valida que el estado de pago no sea RECHAZADO o ABANDONADO.
        if (pedido.getEstadoPago().equals(EstadoPago.RECHAZADO.name()) || pedido.getEstadoPago().equals(EstadoPago.ABANDONADO.name())) {
            throw new RuntimeException("No se puede cancelar un pedido con estado de pago rechazado o abandonado.");
        }

        if (pedido.getEstadoPedido() != EstadoPedido.CREADO && pedido.getEstadoPedido() != EstadoPedido.PROCESANDO) {
            throw new RuntimeException("Solo puedes cancelar pedidos en estado CREADO o PROCESANDO.");
        }
        liberarReserva(pedido);
        pedido.setEstadoPago(EstadoPago.CANCELADO.name());
        pedido.setEstadoPedido(EstadoPedido.CANCELADO); // Actualizamos también el estado del pedido
        pedidoRepository.save(pedido);

    }

    @Override
    @Transactional
    public void procesarWebhookDePago(Map<String, Object> payload) {
        try {

            // Solo procesar eventos de tipo "payment"
            if (!"payment".equals(payload.get("type"))) {
                System.out.println("Evento ignorado: " + payload.get("type"));
                return;
            }

            // Extraer paymentId
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data == null || data.get("id") == null) {
                System.err.println("❌ Payload sin 'data' o sin paymentId");
                return;
            }
            String paymentId = data.get("id").toString();
            System.out.println("PaymentId extraído: " + paymentId);

            // Obtener detalles del pago desde Mercado Pago
            Map<String, Object> paymentInfo = mercadoPagoService.getPaymentDetails(paymentId);
            if (paymentInfo == null || paymentInfo.isEmpty()) {
                System.err.println("❌ No se pudo obtener detalles del pago para ID: " + paymentId);
                return;
            }

            String status = (String) paymentInfo.get("status");
            String externalReference = (String) paymentInfo.get("external_reference");
            if (externalReference == null || externalReference.isEmpty()) {
                System.err.println("❌ External reference nulo, no se puede encontrar el pedido");
                return;
            }

            Long pedidoId = Long.parseLong(externalReference);
            Pedido pedido = pedidoRepository.findById(pedidoId)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));

            // Actualizar estado y paymentId
            pedido.setEstadoPago(mapEstadoPago(status));
            pedido.setPaymentId(paymentId);

            // ✅ Si el pago fue aprobado
            if (EstadoPago.APROBADO.name().equalsIgnoreCase(pedido.getEstadoPago())) {
                pedido.setEstadoPedido(EstadoPedido.PROCESANDO);
                pedido.setFechaProcesando(LocalDateTime.now());
                confirmarReserva(pedido);

                carritoRepository.deleteByUsuario(pedido.getUsuario());
            }

// 🚫 Si el pago fue rechazado, cancelado, expirado o abandonado → restaurar stock
            else if (EstadoPago.RECHAZADO.name().equalsIgnoreCase(pedido.getEstadoPago())
                    || EstadoPago.EXPIRADO.name().equalsIgnoreCase(pedido.getEstadoPago())
                    || EstadoPago.CANCELADO.name().equalsIgnoreCase(pedido.getEstadoPago())
                    || EstadoPago.ABANDONADO.name().equalsIgnoreCase(pedido.getEstadoPago())) {

                pedido.setEstadoPedido(EstadoPedido.CANCELADO);//ORIGUAL

                liberarReserva(pedido);

            }

            pedidoRepository.saveAndFlush(pedido);

        } catch (Exception e) {
            log.error("Error procesando webhook de MercadoPago. Payload: {}. Error: {}",
                    payload, e.getMessage(), e);
        }
    }

    private String mapEstadoPago(String mpStatus) {
        if (mpStatus == null) return EstadoPago.PENDIENTE.name();

        return switch (mpStatus.toLowerCase()) {
            case "approved"   -> EstadoPago.APROBADO.name();
            case "rejected"   -> EstadoPago.RECHAZADO.name();
            case "pending"    -> EstadoPago.PENDIENTE.name();
            case "expired"    -> EstadoPago.EXPIRADO.name();
            case "cancelled"  -> EstadoPago.CANCELADO.name();
            case "abandoned"  -> EstadoPago.ABANDONADO.name();
            default           -> mpStatus.toUpperCase(); // fallback por si MercadoPago agrega nuevos estados
        };
    }

    @Override
    @Transactional
    public PedidoDTO actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        // 🚫 Bloquear transición manual CREADO → PROCESANDO
        if (pedido.getEstadoPedido() == EstadoPedido.CREADO
                && nuevoEstado == EstadoPedido.PROCESANDO) {

            throw new IllegalStateException(
                    "⛔ El pedido pasa a PROCESANDO automáticamente. No se puede forzar manualmente."
            );
        }

        // 🔒 VALIDACIÓN DE 4 HORAS
        if (pedido.getEstadoPedido() == EstadoPedido.PROCESANDO
                && nuevoEstado == EstadoPedido.CONFIRMADO) {

            if (pedido.getFechaProcesando() == null) {
                throw new IllegalStateException("El pedido no tiene fecha de procesamiento");
            }

            LocalDateTime limite = pedido.getFechaProcesando().plusMinutes(1);

            if (LocalDateTime.now().isBefore(limite)) {
                throw new IllegalStateException(
                        "⏳ No se puede confirmar el pedido hasta cumplir 1 horas en PROCESANDO"
                );
            }
        }

        // 🔁 Validación normal de transición
        if (!esTransicionValida(pedido.getEstadoPedido(), nuevoEstado)) {
            throw new IllegalStateException(
                    "❌ No se puede cambiar el estado de "
                            + pedido.getEstadoPedido() + " a " + nuevoEstado
            );
        }
// ==============================
        // 🕒 SETEO DE FECHAS CLAVE
        // ==============================
        // 🕒 SETEO DE FECHAS (solo una vez)
        if (nuevoEstado == EstadoPedido.PROCESANDO && pedido.getFechaProcesando() == null) {
            pedido.setFechaProcesando(LocalDateTime.now());
        }

        if (nuevoEstado == EstadoPedido.ENTREGADO && pedido.getFechaEntrega() == null) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }
        pedido.setEstadoPedido(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);

        return pedidoMapper.toDto(actualizado);
    }

    @Override
    public List<EstadoPedido> obtenerEstadosValidos(PedidoDTO pedido) {
        return Arrays.stream(EstadoPedido.values())
                .filter(estado -> esTransicionValida(pedido.getEstadoPedido(), estado))
                .filter(estado -> {
                    if (pedido.getEstadoPedido() == EstadoPedido.PROCESANDO
                            && estado == EstadoPedido.CONFIRMADO
                            && pedido.getFechaProcesando() != null) {
                        return pedido.getFechaProcesando().plusHours(1).isBefore(LocalDateTime.now());
                    }
                    return true;
                })
                .toList();
    }

    @Override
    public boolean puedeAvanzarEstado(PedidoDTO pedido) {
        if (pedido == null || pedido.getEstadoPedido() == null) {
            return false; // 🔹 si no hay pedido o estado, no puede avanzar
        }

        if (pedido.getEstadoPedido() == EstadoPedido.PROCESANDO && pedido.getFechaProcesando() != null) {
            if (pedido.getFechaProcesando().plusMinutes(1).isAfter(LocalDateTime.now())) return false;
        }
        return !(pedido.getEstadoPedido() == EstadoPedido.ENTREGADO
                || pedido.getEstadoPedido() == EstadoPedido.CANCELADO
                || pedido.getEstadoPedido() == EstadoPedido.ABANDONADO
                || pedido.getEstadoPedido() == EstadoPedido.ENTREGADO_REVISADO);
    }

    public boolean esTransicionValida(EstadoPedido actual, EstadoPedido nuevo) {

        return switch (actual) {

            case CREADO ->
                    nuevo == EstadoPedido.PROCESANDO
                            || nuevo == EstadoPedido.CANCELADO;

            case PROCESANDO ->
                    nuevo == EstadoPedido.CONFIRMADO
                            || nuevo == EstadoPedido.CANCELADO;

            case CONFIRMADO ->
                    nuevo == EstadoPedido.EN_CAMINO;

            case EN_CAMINO ->
                    nuevo == EstadoPedido.ENTREGADO;

            // Estados finales → no permiten cambios
            case ENTREGADO,
                 CANCELADO,
                 ABANDONADO,
                 ENTREGADO_REVISADO ->
                    false;

            default -> false;
        };
    }

    @Override
    public PedidoDTO obtenerPedidoPorNumero(String numeroPedido) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + numeroPedido));
        return pedidoMapper.toDto(pedido);
    }

    @Override
    @Transactional
    public void confirmarEntregaPorQr(String qrToken, UsuarioDTO repartidor) {



        if (!"Repartidor".equalsIgnoreCase(repartidor.getNombreCargo())) {
            throw new RuntimeException("Acceso denegado");
        }
        // ESTA ES LA PARTE CLAVE PARA DEPURAR:

        Pedido pedido = pedidoRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new RuntimeException("QR inválido"));

        if (Boolean.TRUE.equals(pedido.getQrUsado())) {
            throw new RuntimeException("Este QR ya fue utilizado");
        }

        if (pedido.getEstadoPedido() != EstadoPedido.EN_CAMINO) {
            throw new RuntimeException("El pedido no está en estado EN CAMINO");
        }

        // 🔐 Validar que el pedido pertenece a este repartidor
        if (!pedido.getRepartidor().getId().equals(repartidor.getId())) {
            throw new RuntimeException("Este pedido no está asignado a ti");
        }
        // Confirmación
        pedido.setQrUsado(true);
        pedido.setQrUsadoEn(LocalDateTime.now());
        pedido.setEstadoPedido(EstadoPedido.ENTREGADO);

        if (pedido.getFechaEntrega() == null) {
            pedido.setFechaEntrega(LocalDateTime.now());
        }
        pedidoRepository.save(pedido);
        // ← NUEVO: Notificación en tiempo real al cliente vía WebSocket privado
        try {
            String clienteUsername = pedido.getUsuario().getNombre();  // o .getUsername() si usas eso
            messagingTemplate.convertAndSendToUser(
                    clienteUsername,
                    "/queue/pedidos",   // ✅ CORRECTO
                    Map.of(
                            "tipo", "QR_USADO",
                            "pedidoId", pedido.getId(),
                            "numeroPedido", pedido.getNumeroPedido(),
                            "mensaje", "¡Tu pedido #" + pedido.getNumeroPedido() + " ya fue entregado!",
                            "fechaEntrega", pedido.getFechaEntrega().toString(),
                            "repartidor", repartidor.getNombre()
                    )
            );
        } catch (Exception e) {
            log.warn("No se pudo enviar notificación WebSocket al cliente para pedido {}: {}",
                    pedido.getNumeroPedido(), e.getMessage());
        }

    }

    @Override
    public List<PedidoDTO> obtenerPedidosRetrasados() {
        LocalDateTime limite = LocalDateTime.now().minusDays(1);

        List<Pedido> pedidos = pedidoRepository.findPedidosRetrasados(limite);

        return pedidoMapper.toDtoList(pedidos);
    }

    @Override
    public List<PedidoDTO> obtenerPedidosPorRepartidor(Long repartidorId) {
        return pedidoRepository.findByRepartidorId(repartidorId)
                .stream()
                .map(pedidoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PedidoDTO> obtenerPedidosDisponibles() {
        return pedidoRepository.findPedidosDisponibles()
                .stream()
                .map(pedidoMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void asignarPedidoARepartidor(Long pedidoId, Long repartidorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getRepartidor() != null) {
            throw new IllegalStateException("Este pedido ya tiene un repartidor asignado");
        }

        // Verificar si el pedido está en el estado CONFIRMADO
        if (pedido.getEstadoPedido() != EstadoPedido.CONFIRMADO) {
            throw new IllegalStateException("El pedido no está listo para ser asignado. Estado actual: " + pedido.getEstadoPedido());
        }

        Usuario repartidor = usuarioRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado"));

        pedido.setRepartidor(repartidor);
        pedido.setEstadoPedido(EstadoPedido.EN_CAMINO);
        pedido.setFechaAsignacion(LocalDateTime.now());


        //pedido.setEstadoPedido(EstadoPedido.ASIGNADO); // 👈 más claro en BD
        pedidoRepository.save(pedido);

        // 3. ✅ ENVIAR NOTIFICACIÓN DE DECREMENTO EN TIEMPO REAL
        notificarPedidoAsignado();

    }

    @Override
    @Transactional
    public void liberarPedido(Long pedidoId, Long repartidorId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getRepartidor() != null && pedido.getRepartidor().getId().equals(repartidorId)) {
            pedido.setRepartidor(null);
            pedidoRepository.save(pedido);
        } else {
            throw new RuntimeException("El pedido no está asignado a este repartidor");
        }
    }

    @Override
    @Transactional
    public PedidoDTO avanzarEstadoPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        // Determinar el siguiente estado según la transición natural
        EstadoPedido siguienteEstado = switch (pedido.getEstadoPedido()) {
            case CREADO -> EstadoPedido.PROCESANDO;
            case PROCESANDO -> EstadoPedido.CONFIRMADO;
            case CONFIRMADO -> EstadoPedido.EN_CAMINO;
            case EN_CAMINO -> EstadoPedido.ENTREGADO;
            case ENTREGADO,
                 CANCELADO,
                 ABANDONADO,
                 ENTREGADO_REVISADO -> null; // Estados finales
            default -> null;
        };

        if (siguienteEstado == null) {
            throw new IllegalStateException("El pedido ya está en estado final: " + pedido.getEstadoPedido());
        }

        // ✅ Reutilizar el método que ya valida transiciones y restricciones de tiempo
        return actualizarEstadoPedido(pedidoId, siguienteEstado);
    }

    // ----------------------------------------------------------------------------------------------------
    // ✅ Métodos que no necesitan cambios
    @Override
    public PedidoDTO obtenerPedidoPorId(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado."));
        return pedidoMapper.toDto(pedido);
    }
    @Override
    public List<PedidoDTO> obtenerTodosLosPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        return pedidoMapper.toDtoList(pedidos);
    }
    @Override
    public List<PedidoDTO> obtenerHistorialPedidosCliente(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Pedido> pedidos = pedidoRepository.findByUsuarioOrderByFechaPedidoDesc(usuario);

        return pedidos == null || pedidos.isEmpty() ? Collections.emptyList() : pedidoMapper.toDtoList(pedidos);
    }
    @Override
    public PedidoDTO obtenerPedidoPorNumeroYUsuario(String numeroPedido, UsuarioDTO usuarioDTO) {
        Pedido pedido = pedidoRepository.findByNumeroPedido(numeroPedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + numeroPedido));

        // Verificar que el pedido pertenece al usuario logueado
        if (!pedido.getUsuario().getId().equals(usuarioDTO.getId())) {
            throw new RuntimeException("Acceso denegado a este pedido");
        }

        return pedidoMapper.toDto(pedido);
    }

    @Override
    public BigDecimal obtenerTotalCarrito(UsuarioDTO usuarioDTO) {
        Usuario usuario = usuarioRepository.findById(usuarioDTO.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        List<Carrito> itemsCarrito = carritoRepository.findByUsuario(usuario);
        return itemsCarrito.stream()
                .map(Carrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean debeLiberarReserva(Pedido pedido) {
        // Ya se liberó → nunca más
        if (pedido.isReservaLiberada()) {
            return false;
        }

        EstadoPedido estadoPedido = pedido.getEstadoPedido();
        String estadoPago = pedido.getEstadoPago();

        // ✅ EXPIRADO
        if (EstadoPago.EXPIRADO.name().equals(estadoPago)) {
            return true;
        }

        // ✅ CANCELADO
        if (EstadoPago.CANCELADO.name().equals(estadoPago)) {
            return true;
        }
        if (EstadoPago.RECHAZADO.name().equals(estadoPago)) {
            return true;
        }
        // ✅ ABANDONADO solo si estaba en CREADO
        if (EstadoPago.ABANDONADO.name().equals(estadoPago)
                && estadoPedido == EstadoPedido.CREADO) {
            return true;
        }

        return false;
    }

    @Override
    @Transactional
    public void confirmarReserva(Pedido pedido) {
        if (pedido.isReservaConfirmada()) {
            return;
        }
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();

            producto.setInventarioTotal(
                    producto.getInventarioTotal() - detalle.getCantidad()
            );

            producto.setInventarioReservado(
                    Math.max(0, producto.getInventarioReservado() - detalle.getCantidad())
            );
            productoRepository.save(producto);
        }
        pedido.setReservaConfirmada(true);
        pedido.setFechaConfirmacionReserva(LocalDateTime.now());
        pedidoRepository.save(pedido);
    }

    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }
    @Override
    @Transactional
    public void aprobarPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstadoPedido() != EstadoPedido.ENTREGADO) {
            throw new IllegalStateException(
                    "No se puede aprobar el pago. El pedido aún no ha sido marcado como ENTREGADO por el repartidor."
            );
        }

        pedido.setEstadoPago(EstadoPago.APROBADO.name()); // Usamos el enum
        pedidoRepository.save(pedido);

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int nuevasVentas = producto.getVentasRecientes() + detalle.getCantidad();
            producto.setVentasRecientes(nuevasVentas);
            productoRepository.save(producto);
        }
    }

    // ✅ Este método cancela un pedido (cambia su estado), no lo elimina de la base de datos.
    @Transactional
    public void cancelarPedidoPorAdmin(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado."));

        // Se asume que el administrador puede cancelar cualquier pedido.
        // La validación del estado del pedido es una buena práctica.
        if (pedido.getEstadoPedido() != EstadoPedido.CREADO && pedido.getEstadoPedido() != EstadoPedido.PROCESANDO) {
            System.out.println("❌ Fallo al cancelar. El pedido ya no está en estado CREADO o PROCESANDO.");
            throw new RuntimeException("No se puede cancelar el pedido " + pedidoId + ". El estado actual es " + pedido.getEstadoPedido().name() + ".");
        }
        liberarReserva(pedido);
        pedido.setEstadoPago(EstadoPago.CANCELADO.name());
        pedido.setEstadoPedido(EstadoPedido.CANCELADO);
        pedidoRepository.save(pedido);

        System.out.println("❌ Pedido cancelado por el administrador: " + pedido.getId());
    }

    @Override
    @Transactional
    public void actualizarEstado(Long pedidoId, String estadoPago, String paymentId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + pedidoId));

        pedido.setEstadoPago(estadoPago);
        pedido.setPaymentId(paymentId);

        pedidoRepository.save(pedido);

        System.out.println("💾 Pedido actualizado -> ID: " + pedidoId + " | Estado: " + estadoPago + " | PaymentID: " + paymentId);
    }

    @Override
    public void actualizarEstadoPago(Long id, String nuevoEstadoPago) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Solo permitir cambiar si es CONTRAENTREGA
        if (pedido.getMetodoPago() == MetodoPago.CONTRAENTREGA) {
            pedido.setEstadoPago(nuevoEstadoPago);
            pedidoRepository.save(pedido);
        }
    }

    @Transactional
    public void marcarEntregadoYPagado(Long pedidoId, EstadoPedido estadoNotificacion) {
        System.out.println(">>> marcarEntregadoYPagado() INICIADO para pedido: " + pedidoId);
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Solo permitir para pedidos CONTRAENTREGA
        if (pedido.getMetodoPago() != MetodoPago.CONTRAENTREGA) {
            throw new RuntimeException("Este botón solo se puede usar para pedidos contraentrega");
        }

        // Opcional: Validar que ya esté en ENTREGADO (por el repartidor)
        if (pedido.getEstadoPedido() != EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("El pedido debe estar marcado como ENTREGADO por el repartidor antes de confirmarlo como pagado.");
        }
        // Avanzar estado hasta ENTREGADO
        while (pedido.getEstadoPedido() != EstadoPedido.ENTREGADO) {
            EstadoPedido siguienteEstado = switch (pedido.getEstadoPedido()) {
                case CREADO -> EstadoPedido.PROCESANDO;
                case PROCESANDO -> EstadoPedido.EN_CAMINO;
                case EN_CAMINO -> EstadoPedido.ENTREGADO;
                default -> pedido.getEstadoPedido(); // si ya está ENTREGADO, se queda igual
            };
            pedido.setEstadoPedido(siguienteEstado);
        }

        // Marcar el estado final como ENTREGADO
        pedido.setEstadoPedido(estadoNotificacion);
        pedido.setFechaEntrega(LocalDateTime.now());
        //pedido.setEstadoPago(EstadoPago.APROBADO.name()); // Marcar pago como aprobado
        // Confirmar la reserva definitivamente (descuento real del stock)
        confirmarReserva(pedido);
        pedidoRepository.save(pedido);
        enviarCorreoConfirmacionEntrega(pedido);
        // Actualizar ventas recientes de los productos
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setVentasRecientes(producto.getVentasRecientes() + detalle.getCantidad());
            productoRepository.save(producto);
        }

        // 🔹 Enviar notificaciones **fuera de la transacción** a todos los listeners
        try {
            Map<String, Object> data = Map.of(
                    "pedidoId", pedidoId,
                    "total", pedido.getTotal(),
                    "action", "DECREMENTAR_ENTREGAS"
            );

            // Topic que escucha el admin para decrementar contador
            messagingTemplate.convertAndSend("/topic/control-entregas", data);

            // Para repartidor (si existe)
            if (pedido.getRepartidor() != null) {
                String repTopic = "/user/" + pedido.getRepartidor().getId() + "/notificaciones";
                messagingTemplate.convertAndSend(repTopic, Map.of(
                        "tipo", "ENTREGA_REVISADA",
                        "pedidoId", pedidoId,
                        "mensaje", "Tu entrega del pedido #" + pedido.getNumeroPedido() + " fue revisada y aprobada."
                ));
            }

            // Para el cliente
            String clienteTopic = "/user/" + pedido.getUsuario().getId() + "/notificaciones";
            messagingTemplate.convertAndSend(clienteTopic, Map.of(
                    "tipo", "PEDIDO_CERRADO",
                    "pedidoId", pedidoId,
                    "numeroPedido", pedido.getNumeroPedido(),
                    "total", pedido.getTotal(),
                    "mensaje", "¡Gracias por tu compra! Tu pedido #" + pedido.getNumeroPedido() +
                            " por S/ " + pedido.getTotal() + " ha sido entregado y pagado."
            ));

        } catch (Exception e) {
            log.error("Error enviando notificaciones WebSocket para pedido {}: {}",
                    pedidoId, e.getMessage(), e);
        }
    }


}
