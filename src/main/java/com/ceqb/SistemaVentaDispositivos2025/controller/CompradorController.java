package com.ceqb.SistemaVentaDispositivos2025.controller;


import com.ceqb.SistemaVentaDispositivos2025.dto.CompradorDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl.CompradorProductoServiceImpl;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comprador")
public class CompradorController {

    private final ProductoService productoService;
    private final CompradorProductoServiceImpl compradorProductoService;


    @GetMapping("/lista")
    public String listarCompradores(Model model) {
        List<CompradorDTO> lista = compradorProductoService.listar();
        model.addAttribute("compradores", lista);
        return "listarCompradorProductos"; // nombre del html
    }

    @GetMapping("/nuevo/{id}")
    public String mostrarFormularioComprador(@PathVariable Long id, Model model) {

        ProductoDTO producto = productoService.obtenerPorId(id);

        CompradorDTO comprador = new CompradorDTO();
        comprador.setProductoId(id); // 🔥 SOLO EL ID

        model.addAttribute("comprador", comprador);
        model.addAttribute("producto", producto);

        return "compradoProducto";
    }
    @PostMapping("/guardar")
    public String guardarComprador(@ModelAttribute CompradorDTO comprador) {

        compradorProductoService.guardar(comprador);

        return "redirect:/tienda";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormEditar(@PathVariable Long id, Model model) {
        CompradorDTO dto = compradorProductoService.obtenerPorId(id);
        model.addAttribute("comprador", dto);
        // Necesitamos los datos del producto original para mostrar la imagen/nombre en el formulario
        model.addAttribute("producto", productoService.obtenerPorId(dto.getProductoId()));
        return "compradoProducto"; // El nombre de tu HTML
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarComprador(@PathVariable Long id, @ModelAttribute("comprador") CompradorDTO dto) {
        compradorProductoService.actualizar(id, dto);
        return "redirect:/comprador/lista";
    }

}
