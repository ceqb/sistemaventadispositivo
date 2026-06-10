package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/productos")
@RequiredArgsConstructor
public class RegistroClicController {

    private final ProductoService productoService;
    @PostMapping("/registrar-click")
    public ResponseEntity<Void> registrarClick(@RequestParam("productoId") Long productoId) {
        try {
            // Llama al método de servicio que ya tienes implementado
            productoService.registrarClick(productoId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            // Manejo de errores si algo sale mal
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
