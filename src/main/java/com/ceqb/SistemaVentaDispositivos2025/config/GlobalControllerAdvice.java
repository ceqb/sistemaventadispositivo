package com.ceqb.SistemaVentaDispositivos2025.config;

import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoDTO;
import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoService;
import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;
import com.ceqb.SistemaVentaDispositivos2025.service.CarritoService;
import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final HttpSession session;
    private final AuditoriaAccesoService auditoriaAccesoService;
    private final CarritoService carritoService;
    @Autowired
    private CategoriaService categoriaService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        // Obtenemos el usuario de la sesión
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        // Agregamos el usuario al modelo si existe
        model.addAttribute("usuarioLogueado", usuario);

        // Si el usuario está logueado, agregamos también el carrito al modelo
        List<CarritoDTO> carrito = Collections.emptyList();
        if (usuario != null) {
            carrito = carritoService.obtenerCarritoPorUsuario((long) usuario.getId());
        }
        model.addAttribute("carrito", carrito);
    }

    @ModelAttribute("categoriasList")
    public List<Categoria> cargarCategorias() {
        return categoriaService.listar();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex) {

        auditoriaAccesoService.registrarAuditoriaAcceso(
                AuditoriaAccesoDTO.builder()
                        .usuario("SISTEMA")
                        .ip("localhost")
                        .navegador("INTERNAL")
                        .fechaHora(LocalDateTime.now())
                        .exitoso(false)
                        .observacion("ERROR GLOBAL: " + ex.getMessage())
                        .build()
        );

        return "error/500"; // página de error
    }
}

