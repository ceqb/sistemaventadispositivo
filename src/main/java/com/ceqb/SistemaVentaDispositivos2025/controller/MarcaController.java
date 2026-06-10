package com.ceqb.SistemaVentaDispositivos2025.controller;


import com.ceqb.SistemaVentaDispositivos2025.dto.MarcaDTO;
import com.ceqb.SistemaVentaDispositivos2025.service.MarcaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor

@RequestMapping("/admin/marcas")
public class MarcaController {
    private final MarcaService marcaService;

    @GetMapping
    public String listarMarcas(Model model, HttpSession session) {
        /*// Usuario logueado
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        // Si no hay usuario en sesión, redirige al login
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuarioLogueado", usuario);
        */
        model.addAttribute("marcas", marcaService.listar());

        return "admin/marcas";
    }
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaMarca(Model model, HttpSession session) {
        model.addAttribute("marca", new MarcaDTO());
        /*// Usuario logueado
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        model.addAttribute("usuarioLogueado", usuario);*/
        return "admin/nuevaMarca";
    }
    @PostMapping
    public String guardarMarca(@ModelAttribute("marca") MarcaDTO marcaDTO) {
        marcaService.guardar(marcaDTO);
        return "redirect:/marcas";
    }
    @GetMapping("/editar/{id}")
    public String editarMarca(@PathVariable Long id, Model model) {
        model.addAttribute("marca", marcaService.buscarPorId(id));
        return "admin/nuevaMarca";
    }
    @GetMapping("/eliminar/{id}")
    public String eliminarMarca(@PathVariable Long id) {
        marcaService.eliminar(id);
        return "redirect:/marcas";
    }

    // Cambiar estado (activar/desactivar)
    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Long id) {
        marcaService.cambiarEstado(id);
        return "redirect:/marcas";
    }

}
