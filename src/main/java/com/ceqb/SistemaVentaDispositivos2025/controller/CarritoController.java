package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.service.CarritoService;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/carrito")
public class CarritoController {

    private final CarritoService carritoService;
    private final ProductoService productoService;
    //------------------------- VER CARRITO ----------------------------//

    @GetMapping
    public String verCarrito(Model model, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        List<CarritoDTO> carrito = new ArrayList<>();

        if (usuario != null) {
            // ✅ Usuario logueado → carrito persistente
            carrito = carritoService.obtenerCarritoPorUsuario(usuario.getId());
        } else {
            // ✅ Usuario anónimo → carrito en sesión
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
            if (carritoAnonimo != null) {
                carritoAnonimo.forEach(carritoService::asignarPrecioUnitario);
                carrito = carritoAnonimo;
            }
        }

        model.addAttribute("carrito", carrito);
        model.addAttribute("usuario", usuario);

        double total = carrito.stream()
                .mapToDouble(item -> item.getCantidad() *
                        (item.getPrecioUnitario() != null ? item.getPrecioUnitario().doubleValue() : 0))
                .sum();
        model.addAttribute("total", total);

        return "tienda/verCarrito";
    }

    //------------------------- AUMENTAR / DISMINUIR ----------------------------//

    @GetMapping("/disminuir")
    public String disminuir(@RequestParam("id") Long productoId, HttpSession session) {
        carritoService.disminuirCantidad(productoId, session);
        return "redirect:/carrito";
    }

    @GetMapping("/aumentar")
    public String aumentar(@RequestParam("id") Long productoId, HttpSession session) {
        carritoService.aumentarCantidad(productoId, session);
        return "redirect:/carrito";
    }

    //------------------------- AGREGAR ----------------------------//

    @PostMapping("/agregar")
    public String agregarProducto(@RequestParam Long id,
                                  @RequestParam(defaultValue = "1") int cantidad,
                                  HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario != null) {
            carritoService.agregarProducto(usuario, id, cantidad);
        } else {
            carritoService.agregarProductoAnonimo(id, cantidad, session);
        }
        return "redirect:/carrito";
    }

    //------------------------- ELIMINAR ----------------------------//

    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam Long productoId, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario != null) {
            carritoService.eliminarProducto(usuario.getId(), productoId);
        } else {
            @SuppressWarnings("unchecked")
            List<CarritoDTO> carritoAnonimo = (List<CarritoDTO>) session.getAttribute("carritoAnonimo");
            if (carritoAnonimo != null) {
                carritoAnonimo.removeIf(item -> item.getProductoId().equals(productoId));
            }
        }
        return "redirect:/carrito";
    }

    //------------------------- VACIAR ----------------------------//

    @PostMapping("/vaciar")
    public String vaciarCarrito(HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario != null) {
            carritoService.vaciarCarrito(usuario.getId());
        } else {
            carritoService.vaciarCarritoAnonimo(session);
        }
        return "redirect:/carrito";
    }
}
