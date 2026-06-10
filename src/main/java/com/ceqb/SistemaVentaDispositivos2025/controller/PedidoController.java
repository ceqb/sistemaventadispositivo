package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.PedidoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.repository.PedidoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CarritoService;
import com.ceqb.SistemaVentaDispositivos2025.service.PagoContraentregaService;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final CarritoService carritoService;
    private final PedidoRepository pedidoRepository;
    private final PagoContraentregaService pagoContraentregaService;
    private final PedidoMapper pedidoMapper;
    // ✅ Crear preferencia de pago (MercadoPago)
    @PostMapping("/create-payment-preference")
    public ResponseEntity<Map<String, Object>> createPaymentPreference(HttpSession session) {
        UsuarioDTO usuarioDTO = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuarioDTO == null) {
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");

            if (carritoAnonimo == null || carritoAnonimo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Carrito vacío, inicia sesión para pagar."));
            }

            return ResponseEntity.status(401)
                    .body(Map.of("error", "Debes iniciar sesión para completar la compra."));
        }

        try {
            // 🔄 Fusionar carrito anónimo si existe
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
            if (carritoAnonimo != null && !carritoAnonimo.isEmpty()) {
                carritoService.fusionarCarrito(carritoAnonimo, usuarioDTO);
                session.removeAttribute("carritoAnonimo");
            }

            // 🛒 Crear Pedido + Preferencia en MercadoPago
            Map<String, String> result = pedidoService.crearPedidoYPreferencia(usuarioDTO);

            return ResponseEntity.ok(Map.of(
                    "url_pago", result.get("url_pago"),
                    "pedidoId", Long.valueOf(result.get("pedidoId")),
                    "numeroPedido", result.get("numeroPedido")
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error al crear la preferencia de pago."));
        }
    }

    // ✅ Endpoint alternativo para finalizar checkout
    @PostMapping("/checkout")
    @ResponseBody
    public Map<String, Object> checkoutPedido(HttpSession session) throws MPException, MPApiException {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            throw new RuntimeException("Usuario no logueado");
        }

        Map<String, String> pagoData = pedidoService.crearPedidoYPreferencia(usuario);

        Map<String, Object> response = new HashMap<>();
        response.put("url_pago", pagoData.get("url_pago"));
        response.put("pedidoId", Long.parseLong(pagoData.get("pedidoId")));
        response.put("numeroPedido", pagoData.get("numeroPedido"));
        return response;
    }

    // ✅ Consultar estado del pedido en tiempo real
    @GetMapping("/estado/{pedidoId}")
    @ResponseBody
    public Map<String, String> estadoPedido(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        return Map.of("estadoPago", pedido.getEstadoPago());
    }
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @PostMapping("/test-notificacion")
    @ResponseBody
    public String testNotificacion() {
        Map<String, Object> data = new HashMap<>();
        data.put("pedidoId", 999);
        data.put("total", 123.45);
        messagingTemplate.convertAndSend("/topic/notificaciones", data);
        return "Notificación enviada!";
    }
    @PostMapping("/createPagoContraEntrega")
    public ResponseEntity<?> createPagoContraEntrega(@RequestBody Map<String, String> body, HttpSession session, RedirectAttributes redirectAttributes ) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        /*if (usuario == null) {
            return "redirect:/login"; // 🔒 si no está logueado
        }*/
        if (usuario == null) {
            // Guardamos el destino para después del login
            session.setAttribute("redirectAfterLogin", "/carrito?finalizar=pagoContraentrega");
            // Enviamos mensaje de advertencia
            //return "redirect:/login"; // 🔒 si no está logueado
            return ResponseEntity.status(401)
                    .body(Map.of("error", "Debes iniciar sesión para completar la compra."));
        }

        // ⬅️ Extraer dirección enviada desde el frontend
        String direccion = body.get("direccion");

        // 🚫 Validación de dirección vacía
        if (direccion == null || direccion.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La dirección de entrega es obligatoria."));
        }

        try {
        // Crear pedido con contraentrega
        PedidoDTO pedido = pagoContraentregaService.crearPedidoContraentrega(usuario, direccion);

        // ➤ ENVIAR NOTIFICACIÓN usando el service
        pedidoService.notify(pedido, usuario);

        // Iniciar flujo de pago directamente con el ID
        String mensaje = pagoContraentregaService.iniciarPago(pedido.getId());

        // Redirigir a confirmación
        //return "redirect:/pedidos/confirmacion?pedidoId=" + pedido.getId() + "&mensaje=" + mensaje;

        // ✅ Retornar toda la información relevante
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("redirectUrl", "/pedidos/confirmacion?pedidoId=" + pedido.getId()+ "&mensaje= " + mensaje);
        respuesta.put("pedidoId", pedido.getId());
        respuesta.put("total", pedido.getTotal());
        respuesta.put("fecha", pedido.getFechaPedido());
        respuesta.put("direccion", direccion);

            // 🔥 CORRECCIÓN CLAVE: Incluir el mensaje del DTO si existe
            if (pedido.getMensaje() != null && !pedido.getMensaje().isEmpty()) {
                respuesta.put("mensaje", pedido.getMensaje());
            }

        return ResponseEntity.ok(respuesta);
        } catch (IllegalStateException e) {
            // ⚠️ Si tu service detecta un pedido en proceso, lo captura aquí
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Error inesperado al generar el pedido."));
        }
    }
    @GetMapping("/auth/check")
    @ResponseBody
    public ResponseEntity<?> checkLogin(HttpSession session) {
        Object usuario = session.getAttribute("usuarioLogueado");
        if (usuario != null) {
            return ResponseEntity.ok(Map.of("loggedIn", true));
        } else {
            return ResponseEntity.ok(Map.of("loggedIn", false));
        }
    }
    @GetMapping("/confirmacion")
    public String mostrarConfirmacion(@RequestParam("pedidoId") Long pedidoId,
                                      @RequestParam("mensaje") String mensaje,
                                      HttpSession session,
                                      Model model) {

        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(pedidoId);

        model.addAttribute("pedido", pedido);
        model.addAttribute("mensaje", mensaje);

        return "tienda/confirmacion"; // Thymeleaf template confirmacion.html
    }
}
