package com.ceqb.SistemaVentaDispositivos2025.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VistasController {
    @GetMapping("/pedido-confirmado")
    public String pedidoConfirmado() {
        return "tienda/vistasDePago/pedido-confirmado"; // Nombre de tu archivo HTML (e.g., pedido-confirmado.html)
    }

    @GetMapping("/pedido-pendiente")
    public String pedidoPendiente() {
        return "tienda/vistasDePago/pedido-pendiente"; // Nombre de tu archivo HTML (e.g., pedido-pendiente.html)
    }

    @GetMapping("/pedido-fallo")
    public String pedidoFallo() {
        return "tienda/vistasDePago/pedido-fallo"; // Nombre de tu archivo HTML (e.g., pedido-fallo.html)
    }
}
