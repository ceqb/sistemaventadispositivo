package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    public String mostrarCategorias(Model model) {
        // La validación de la sesión y roles la maneja el LoginInterceptor.
        // Aquí solo nos enfocamos en la lógica de negocio.
        model.addAttribute("categorias", categoriaService.listar());
        return "admin/categorias";
    }
}