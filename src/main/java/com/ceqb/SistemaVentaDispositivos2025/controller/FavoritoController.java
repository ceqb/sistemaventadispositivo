package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.repository.FavoritoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.FavoritoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;
    private final FavoritoRepository favoritoRepository;
    @GetMapping
    public String listarFavoritos(HttpSession session, Model model) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Obtener los productos favoritos del usuario
        List<ProductoDTO> favoritos = favoritoService.obtenerFavoritosUsuario(usuario.getId());

        model.addAttribute("favoritos", favoritos);
        model.addAttribute("usuario", usuario);


        return "tienda/favoritos"; // Vista donde mostrarás los favoritos
    }
    @PostMapping("/toggle/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleFavorito(@PathVariable Long id, HttpSession session) {
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        Map<String, Object> response = new HashMap<>();

        if (usuario == null) {
            response.put("error", "Usuario no autenticado");
            return ResponseEntity.status(401).body(response);
        }

        boolean estado = favoritoService.toggleFavorito(usuario.getId(), id);
        response.put("fav", estado);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/eliminar")
    public String eliminarFavorito(
            @RequestParam("productoId") Long productoId,
            HttpSession session) {

        // 1. Obtener el ID del usuario logueado
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            // Manejar error o redirigir a login si no hay usuario
            return "redirect:/login";
        }
        Long usuarioId = usuario.getId();
        // 🕵️‍♂️ LÍNEAS DE DEBUGGING CLAVE:
        System.out.println("DEBUG ELIMINAR: Producto ID recibido: " + productoId);
        System.out.println("DEBUG ELIMINAR: Usuario ID de la sesión: " + usuario.getUsuario());
        // 2. Llamar al nuevo método del servicio
        favoritoService.eliminarPorProductoYUsuario(productoId, usuarioId);

        // 3. Redirigir a la lista de favoritos actualizada
        return "redirect:/favoritos";
    }
}
