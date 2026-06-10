package com.ceqb.SistemaVentaDispositivos2025.controller;


import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.service.PedidoService;
import com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl.AnaliticaProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class IndexController {
    private final AnaliticaProductoService analiticaProductoService;
    private final PedidoService pedidoService;

    @GetMapping({"/index"})
    @Transactional(readOnly = true)
    public String biblioteca(Model model, HttpSession session) {

        long pendientesCount = pedidoService.contarPedidosDisponibles();
        model.addAttribute("pedidosPendientesCount", pendientesCount);
        // 2. ✅ INICIALIZA EL CONTADOR DE ENTREGAS COMPLETADAS (NUEVO)
        long entregasCompletadasCount = pedidoService.contarPedidosEnEstado(EstadoPedido.ENTREGADO);
        model.addAttribute("entregasCompletadasCount", entregasCompletadasCount);

        model.addAttribute("totalPedidos", analiticaProductoService.obtenerPedidosAprobados());
        model.addAttribute("totalClientes", analiticaProductoService.obtenerTotalClientes());
        model.addAttribute("totalProductos", analiticaProductoService.obtenerTotalProductos());
        model.addAttribute("clienteTop", analiticaProductoService.obtenerClienteTop());
        model.addAttribute("masClickeados", analiticaProductoService.obtenerTop1Clickeado());
        model.addAttribute("masVendido", analiticaProductoService.obtenerProductoMasVendido());



        // Retorna el nombre de la plantilla de Thymeleaf
        return "admin/index";
    }
    @GetMapping("/dashboard")
    public String dashboardAnalitica(Model model, HttpSession session) {
        // Recuperar el usuario de la sesión
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        // ¡IMPORTANTE! Si no lo agregas al model, Thymeleaf no lo ve
        model.addAttribute("usuarioLogueado", usuario);

        model.addAttribute("masClickeados", analiticaProductoService.obtener10Clickeados());
        model.addAttribute("masVendidos", analiticaProductoService.obtener10MasVendidos());
        model.addAttribute("bajaConversion", analiticaProductoService.obtenerTop5BajaConversion());
        return "admin/dashboardAnalitica";

    }





}
