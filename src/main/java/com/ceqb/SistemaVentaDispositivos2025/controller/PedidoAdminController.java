package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.PedidoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.ceqb.SistemaVentaDispositivos2025.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pedidosAdmin")
public class PedidoAdminController {
    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;

    /**
     *********************************✅ADMINISTRADOR*********************************
                                                                                    **/

    @GetMapping("/detalle")
    public String verDetallePedido(@RequestParam Long id, Model model) {
        PedidoDTO pedido = pedidoService.obtenerPedidoPorId(id);
        model.addAttribute("pedido", pedido);
        //model.addAttribute("estados", EstadoPedido.values()); // 👈 importante
        model.addAttribute("estados", pedidoService.obtenerEstadosValidos(pedido));
        model.addAttribute("puedeAvanzar", pedidoService.puedeAvanzarEstado(pedido));


        return "admin/detallePedido";
    }
    // ✅ Nuevo endpoint para la cancelación desde la vista del administrador
    @PostMapping("/{id}/cancelar")
    public String cancelarPedidoAdmin(@PathVariable("id") Long pedidoId, RedirectAttributes redirectAttributes) {
        try {
            // El administrador no necesita un objeto de usuario para cancelar.
            // Se asume que si llega a este endpoint, tiene los permisos necesarios.
            pedidoService.cancelarPedidoPorAdmin(pedidoId); // Llama al nuevo método del servicio
            redirectAttributes.addFlashAttribute("success", "El pedido ha sido cancelado por el administrador.");
        } catch (RuntimeException e) {
            System.err.println("Error al cancelar el pedido: " + e.getMessage()); // Log del error para depuración
            redirectAttributes.addFlashAttribute("error", "Error al cancelar el pedido: " + e.getMessage());
        }
        return "redirect:/pedidosAdmin/historialAdmin";
    }
    @GetMapping("/historialAdmin")
    public String verPedidosAdmin(Model model) {
        List<PedidoDTO> pedidos = pedidoService.obtenerTodosLosPedidos();

        long pendientesCount = pedidos.stream()
                .filter(p -> p.getEstadoPedido().equals(EstadoPedido.CREADO))
                .count();
        model.addAttribute("pedidosPendientesCount", pendientesCount);
        if (pendientesCount > 0) {
            model.addAttribute("pendientesMsg", "Tienes " + pendientesCount + " pedidos pendientes por revisar.");
        }
// 2. ✅ NUEVO: Contador de Entregas Completadas (Para inicializar el sidebar en el index)
        // Asumimos que tienes un método contarPedidosEnEstado(EstadoPedido) en tu PedidoService
        long entregasCompletadasCount = pedidoService.contarPedidosEnEstado(EstadoPedido.ENTREGADO);
        model.addAttribute("entregasCompletadasCount", entregasCompletadasCount);

        model.addAttribute("pedidos", pedidos);
        List<UsuarioDTO> repartidores = usuarioService.obtenerRepartidores();
        model.addAttribute("repartidores", repartidores);
        return "/admin/historial"; // Asume que la vista se llama "admin_pedidos.html" y está en la carpeta "admin"
    }
    // ✅ 1. ENDPOINT: Muestra la lista de entregas pendientes de revisión
    @GetMapping("/entregasCompletadas")
    public String verEntregasCompletadas(Model model) {
        // Obtenemos solo los pedidos que el Repartidor marcó como entregados y están pendientes de revisión (alerta).
        List<PedidoDTO> entregas = pedidoService.obtenerPedidosEnEstado(EstadoPedido.ENTREGADO);

        model.addAttribute("entregas", entregas);
        model.addAttribute("titulo", "Entregas Completadas Recientemente");

        // Necesitamos recalcular ambos contadores para que el sidebar se vea bien en esta vista
        long entregasCompletadasCount = pedidoService.contarPedidosEnEstado(EstadoPedido.ENTREGADO);
        model.addAttribute("entregasCompletadasCount", entregasCompletadasCount);

        long pedidosPendientesCount = pedidoService.contarPedidosEnEstado(EstadoPedido.CREADO);
        model.addAttribute("pedidosPendientesCount", pedidosPendientesCount);

        // Este es el nombre de la plantilla HTML que genera el error si no existe
        return "admin/listaEntregasCompletadas";
    }

    // ✅ 2. ENDPOINT: Procesa la acción de "Marcar como Revisado"
    @PostMapping("/entregasCompletadas/marcarRevisado")
    public String marcarEntregaComoRevisada(
            @RequestParam("pedidoId") Long pedidoId,
            RedirectAttributes redirectAttributes) {
        System.out.println(">>> BOTÓN MARCAR REVISADO PRESIONADO para pedido: " + pedidoId);
        try {
            // 🔒 DOBLE SEGURIDAD: Validar que el pago esté aprobado
            PedidoDTO pedido = pedidoService.obtenerPedidoPorId(pedidoId);
            if (!pedido.getEstadoPago().equals("APROBADO")) {
                redirectAttributes.addFlashAttribute("error",
                "No se puede revisar el pedido porque el pago no está aprobado.");
                return "redirect:/pedidosAdmin/entregasCompletadas";
            }

            // Este método en el servicio cambia el estado a 'ENTREGADO' (final) y envía el WS de decremento.
            pedidoService.marcarEntregadoYPagado(pedidoId, EstadoPedido.ENTREGADO_REVISADO);

            redirectAttributes.addFlashAttribute("success",
                    "Entrega marcada como revisada, pago confirmado y STOCK DESCONTADO exitosamente.");

            System.out.println(">>> Llamada a marcarEntregadoYPagado COMPLETADA para pedido " + pedidoId);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al marcar la entrega como revisada: " + e.getMessage());
        }

        return "redirect:/pedidosAdmin/entregasCompletadas";
    }
    @PostMapping("/{id}/estadoPago")
    public String actualizarEstadoPago(
            @PathVariable Long id,
            @RequestParam String nuevoEstadoPago
    ) {
        pedidoService.actualizarEstadoPago(id, nuevoEstadoPago);
        return "redirect:/pedidosAdmin/detalle?id=" + id;
    }
    @PostMapping("/{id}/estado")
    public String actualizarEstadoPedido(
            @PathVariable Long id,
            @RequestParam EstadoPedido nuevoEstado
    ) {
        pedidoService.actualizarEstadoPedido(id, nuevoEstado);
        // Redirige al detalle actualizado
        return "redirect:/pedidosAdmin/detalle?id=" + id;
    }

    @PostMapping("/{id}/avanzar")
    public String avanzarEstadoPedido(@PathVariable Long id) {
        pedidoService.avanzarEstadoPedido(id);
        // Redirige al detalle actualizado
        return "redirect:/pedidosAdmin/detalle?id=" + id;
    }
    /*******************************************************************/
    @PostMapping("/{pedidoId}/asignar")
    public String asignarPedido(
            @PathVariable Long pedidoId,
            @RequestParam Long repartidorId,
            RedirectAttributes redirectAttributes
    ) {

        try {
            pedidoService.asignarPedidoARepartidor(pedidoId, repartidorId);
            redirectAttributes.addFlashAttribute("success", "Pedido asignado correctamente al repartidor.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al asignar el pedido: " + e.getMessage());
        }
        return "redirect:/pedidosAdmin/historialAdmin";
    }
    @GetMapping("/disponibles")
    public String verPedidosDisponibles(Model model) {
        List<PedidoDTO> pedidos = pedidoService.obtenerPedidosDisponibles();
        model.addAttribute("pedidos", pedidos);

        // 👉 Aquí deberías tener un UsuarioService para traer solo repartidores
         List<UsuarioDTO> repartidores = usuarioService.obtenerRepartidores();
         model.addAttribute("repartidores", repartidores);

        return "admin/listaPedidosDisponibles";
    }

    @GetMapping("/pedidosRetrasados")
    public String pedidosRetrasados(Model model) {

        List<PedidoDTO> retrasados = pedidoService.obtenerPedidosRetrasados();

        model.addAttribute("pedidosRetrasados", retrasados);
        model.addAttribute("retrasadosCount", retrasados.size());

        return "admin/pedidosRetrasados";
    }

}
