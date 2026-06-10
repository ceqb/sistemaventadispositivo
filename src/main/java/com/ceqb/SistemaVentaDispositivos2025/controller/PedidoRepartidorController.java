package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidosRepartidor")
public class PedidoRepartidorController {
    private final PedidoService pedidoService;
    private final SimpMessagingTemplate messagingTemplate;
    /*ORIGINAL*/

    @PostMapping("/{numero}/avanzarEstadoPedidoRepartidor")
    public String avanzarEstadoPedido(@PathVariable String numero, HttpSession session, RedirectAttributes redirectAttrs) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorNumero(numero);
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        String rol = usuario != null ? usuario.getNombreCargo() : "";
        try {
            // Avanzar el estado del pedido y obtener el pedido actualizado
            PedidoDTO pedidoActualizado = pedidoService.avanzarEstadoPedido(pedido.getId());

            // 🔔 Notificar entrega solo si el pedido está ENTREGADO
            if (pedidoActualizado.getEstadoPedido() == EstadoPedido.ENTREGADO) {
                pedidoService.notificarEntrega(
                        pedidoActualizado.getId(),
                        pedidoActualizado.getTotal(),
                        pedidoActualizado.getRepartidorNombre() // asegurar que se envía el nombre del repartidor
                );
                System.out.println("🚀 Enviando notificación WS del pedido ENTREGADO: " + pedidoActualizado.getId());
            }

            // Obtener rol del usuario logueado

        } catch (IllegalStateException e) {
            // 🔹 Enviar mensaje amigable a la vista
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/pedidosRepartidor/detalleRepartidor?numero=" + numero;
        }
        return getRedirectDetalle(numero, rol);
    }

    @PostMapping("/{numero}/entregadoPagado")
    public String entregarYPagar(@PathVariable String numero, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            PedidoDTO pedido = pedidoService.obtenerPedidoPorNumero(numero);
            pedidoService.marcarEntregadoYPagado(pedido.getId(), EstadoPedido.ENTREGADO);
            redirectAttributes.addFlashAttribute("success", "Pedido entregado y pago registrado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el pedido: " + e.getMessage());
        }

        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        String rol = usuario != null ? usuario.getNombreCargo() : "";

        return getRedirectDetalle(numero, rol);
    }


    @GetMapping("/detalleRepartidor")
    public String verDetallePedidoRepartidor(@RequestParam String numero, Model model) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorNumero(numero);
        model.addAttribute("pedido", pedido);
        return "repartidor/detallePedidoRepartidor";
    }


    // 🔹 Método utilitario para decidir la redirección según rol
    private String getRedirectDetalle(String numero, String rol) {
        if ("Administrador".equalsIgnoreCase(rol)) {
            return "redirect:/pedidosAdmin/detalle?numero=" + numero;
        } else if ("Repartidor".equalsIgnoreCase(rol)) {
            return "redirect:/pedidosRepartidor/detalleRepartidor?numero=" + numero;
        }
        return "/";
    }

    @GetMapping("/pedidos")
    public String listarPedidosRepartidor(Model model, HttpSession session) {
        UsuarioDTO repartidor = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosPorRepartidor(repartidor.getId());
        List<PedidoDTO> pedidosDisponibles = pedidoService.obtenerPedidosDisponibles();

        long pendientes = pedidosDisponibles.stream()
                .filter(p -> p.getEstadoPedido().equals(EstadoPedido.CREADO))
                .count();

        if (pendientes > 0) {
            model.addAttribute("pendientesMsg", "Tienes " + pendientes + " pedidos disponibles por revisar.");
        }
        model.addAttribute("pedidos", pedidos);

        return "repartidor/pedidosRepartidor"; // archivo .html
    }

    @GetMapping("/pedido/{id}")
    public String verDetallePedido(@PathVariable Long id, Model model) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(id);
        model.addAttribute("pedido", pedido);
        return "repartidor/detallePedidoRepartidor";
    }

    // 📌 Listar pedidos disponibles (libres)
    @GetMapping("/pedidos-disponibles")
    public String verPedidosDisponibles(Model model) {
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosDisponibles();
        model.addAttribute("pedidos", pedidos);
        return "repartidor/listaPedidosDisponiblesRepartidor";
    }

    // 📌 Tomar un pedido
    @PostMapping("/{id}/tomar")
    public String tomarPedido(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO repartidor = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (repartidor == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión como repartidor.");
            return "redirect:/login";
        }

        try {
            pedidoService.asignarPedidoARepartidor(id, repartidor.getId());
            redirectAttributes.addFlashAttribute("success", "Pedido asignado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se pudo asignar el pedido: " + e.getMessage());
        }

        return "redirect:/pedidosRepartidor/pedidos-disponibles";
    }

    @PostMapping("/confirmar-entrega")
    @Transactional
    public ResponseEntity<?> confirmarEntrega(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        // 1️⃣ Obtener repartidor de sesión
        UsuarioDTO repartidor = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (repartidor == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Sesión no válida");
        }

        // 2️⃣ Validar rol
        if (!"Repartidor".equalsIgnoreCase(repartidor.getNombreCargo())) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: Se requiere rol de Repartidor");
        }

        // 3️⃣ Obtener QR desde el BODY (JSON)
        String qrToken = body.get("qrToken");
        System.out.println("Body recibido: " + body);
        if (qrToken == null || qrToken.isBlank()) {
            return ResponseEntity
                    .badRequest()
                    .body("El qrToken es obligatorio");
        }

        String tokenLimpio = qrToken.trim();

        try {
            System.out.println("========== DEPURACIÓN QR ==========");
            System.out.println("TOKEN RECIBIDO: [" + tokenLimpio + "]");
            System.out.println("LONGITUD: " + tokenLimpio.length());
            System.out.println("===================================");

            // 4️⃣ Confirmar entrega
            pedidoService.confirmarEntregaPorQr(tokenLimpio, repartidor);

            return ResponseEntity.ok("Entrega confirmada correctamente");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno");
        }
    }
    @GetMapping("/entrega")
    public String mostrarConfirmacion(@RequestParam String token,
                                      HttpSession session,
                                      Model model) {

        UsuarioDTO repartidor = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (repartidor == null || !"Repartidor".equals(repartidor.getNombreCargo())) {
            return "redirect:/login";
        }

        model.addAttribute("qrToken", token);
        return "repartidor/confirmarEntrega";
    }
}
