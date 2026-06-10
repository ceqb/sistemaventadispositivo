package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidosCliente")
public class PedidoClienteController {
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final ObjectMapper objectMapper;
    private UsuarioDTO getLoggedInUser(HttpSession session) {
        return (UsuarioDTO) session.getAttribute("usuarioLogueado");
    }

    @GetMapping("/pedido/{id}/qr")
    public ResponseEntity<byte[]> verQrPedido(@PathVariable Long id, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        if (usuario == null) return ResponseEntity.status(401).build();

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            return ResponseEntity.status(403).build();
        }

        if (Boolean.TRUE.equals(pedido.getQrUsado())) {
            return ResponseEntity.status(410).build();
        }

        try {
            // SOLUCIÓN: Enviar el token PURO.
            // No envíes JSON si tu receptor espera un String de validación.
            // Esto hará que el QR sea mucho más fácil de leer y procesar.
            String contenidoQr = pedido.getQrToken();

            byte[] qrImagen = pedidoService.generarQrImagen(contenidoQr);

            return ResponseEntity.ok()
                    .header("Content-Type", "image/png")
                    .body(qrImagen);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/estado/{pedidoId}")
    @ResponseBody
    public Map<String, String> estadoPedido(@PathVariable Long pedidoId) {
        // The original logic is correct, no changes needed here.
        String estadoPago = pedidoRepository.findById(pedidoId)
                .map(pedido -> pedido.getEstadoPago())
                .orElse("No Encontrado");
        return Collections.singletonMap("estadoPago", estadoPago);
    }
    @GetMapping("/historialCliente")
    public String verHistorialPedidos(Model model, HttpSession session) {
        UsuarioDTO usuario = getLoggedInUser(session);
        if (usuario == null) {
            return "redirect:/login";
        }
        List<PedidoDTO> pedidos = pedidoService.obtenerHistorialPedidosCliente(usuario);
        model.addAttribute("pedidos", pedidos);
        return "cliente/historialCliente";
    }
    // Opcional: Endpoint para ver los detalles de un pedido específico
    @GetMapping("/detalle")
    public String verDetallePedido(@RequestParam("numeroPedido") String numeroPedido, Model model, HttpSession session) {
        UsuarioDTO usuario = getLoggedInUser(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Buscar por numeroPedido y usuario
        PedidoDTO pedido = pedidoService.obtenerPedidoPorNumeroYUsuario(numeroPedido, usuario);
        model.addAttribute("pedido", pedido);

        // 🔹 Estados filtrados según el actual
        List<EstadoPedido> estadosDisponibles = obtenerEstadosDisponibles(pedido.getEstadoPedido());
        model.addAttribute("estados", estadosDisponibles);

        return "cliente/detallePedidoCliente";
    }

    private List<EstadoPedido> obtenerEstadosDisponibles(EstadoPedido actual) {
        switch (actual) {
            case PROCESANDO:
                return List.of(EstadoPedido.EN_CAMINO, EstadoPedido.ENTREGADO);
            case EN_CAMINO:
                return List.of(EstadoPedido.ENTREGADO);
            case ENTREGADO:
                return List.of(); // no hay más
            default:
                return List.of();
        }
    }
    @GetMapping("/seguimiento")
    public String seguimientoPedido(@RequestParam String numeroPedido, Model model, HttpSession session) {
        UsuarioDTO usuario = getLoggedInUser(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener el pedido usando el servicio para asegurar que pertenezca al usuario
        PedidoDTO pedido = pedidoService.obtenerPedidoPorNumeroYUsuario(numeroPedido, usuario);

        if (pedido != null) {
            model.addAttribute("pedido", pedido);
            return "cliente/seguimientoPedido";
        } else {
            // Si el pedido no existe o no pertenece al usuario, redirigir
            return "redirect:/cliente/historial";
        }
    }
    @PostMapping("/cancelar")
    public String cancelarPedido(@RequestParam Long pedidoId,HttpSession session, RedirectAttributes redirectAttributes) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para cancelar un pedido.");
            return "redirect:/login";
        }

        try {
            pedidoService.cancelarPedidoPorUsuario(pedidoId, usuario);
            redirectAttributes.addFlashAttribute("success", "El pedido ha sido cancelado exitosamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/pedidosCliente/historialCliente";
    }
}
