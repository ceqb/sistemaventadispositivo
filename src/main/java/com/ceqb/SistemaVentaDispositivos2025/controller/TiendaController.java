package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoDTO;
import com.ceqb.SistemaVentaDispositivos2025.API_AUDITORIA.AuditoriaAccesoService;
import com.ceqb.SistemaVentaDispositivos2025.config.HashidsUtil;
import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;
import com.ceqb.SistemaVentaDispositivos2025.repository.CategoriaRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl.AnaliticaProductoService;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TiendaController {
    private final ProductoService productoService;

    private final CategoriaRepository categoriaRepository;
    private final HashidsUtil hashidsUtil;
    private final AnaliticaProductoService analiticaProductoService;
    private final AuditoriaAccesoService auditoriaAccesoService;

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/tienda")
    @Transactional(readOnly = true)
    public String listarpaginaweb(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "60") int size,
            @RequestParam(required = false) String search,
            @RequestParam(name = "categoria", required = false) String categoriaNombre,
            Model model, HttpSession session,
            HttpServletRequest request) {

        // ... USUARIO LOGUEADO ...
        UsuarioDTO usuario = (UsuarioDTO) session.getAttribute("usuarioLogueado");

        String ip = request.getRemoteAddr();
        String navegador = request.getHeader("User-Agent");

        AuditoriaAccesoDTO auditoria = AuditoriaAccesoDTO.builder()
                .usuario(usuario != null ? usuario.getNombre() : "ANONIMO")
                .ip(ip)
                .navegador(navegador)
                .fechaHora(LocalDateTime.now())
                .exitoso(true)
                .observacion("Ingreso a la tienda")
                .build();

        auditoriaAccesoService.registrarAuditoriaAcceso(auditoria);


        model.addAttribute("usuario", usuario);


        // =========================================================
        // 🔍 BÚSQUEDA / FUENTE DE DATOS PRINCIPAL (PRODUCTOS RECIENTES)
        // =========================================================

        // 🔥 Convertir página visible (base 1) a base 0
        int pageIndex = Math.max(page - 1, 0);
        Page<ProductoDTO> productosPrincipalesPaginados;
        // 1. Cargar todas las categorías para el menú de navegación
        model.addAttribute("categoriasList", categoriaService.listar());

        Long categoriaId = null;
        if (categoriaNombre != null && !categoriaNombre.trim().isEmpty()) {
            Categoria cat = categoriaService.obtenerPorNombre(categoriaNombre.trim());
            if (cat != null) {
                categoriaId = cat.getId();
            }
            model.addAttribute("categoriaSeleccionada", categoriaNombre);  // para mostrar en vista
        }

        if (search != null && !search.trim().isEmpty() && categoriaId != null) {
            // Ambos filtros
            productosPrincipalesPaginados = productoService.buscar(search.trim(), List.of(categoriaId), pageIndex, size);
            model.addAttribute("searchQuery", search.trim());
        } else if (search != null && !search.trim().isEmpty()) {
            productosPrincipalesPaginados = productoService.buscarProductosPaginado(search.trim(), pageIndex, size);
            model.addAttribute("searchQuery", search.trim());
        } else if (categoriaId != null) {
            productosPrincipalesPaginados = productoService.buscar(null, List.of(categoriaId), pageIndex, size);
        } else {
            productosPrincipalesPaginados = productoService.obtenerProductosPaginacionPrincipal(pageIndex, size);
        }


        // =========================================================
        // 🔢 LÓGICA DE PAGINACIÓN (Aplicada a la variable principal)
        // =========================================================
        int totalPages = productosPrincipalesPaginados.getTotalPages();
        int currentPage = productosPrincipalesPaginados.getNumber();
        int maxButtons = 5;

        // Calcular rango de páginas a mostrar
        int start = Math.max(0, currentPage - 2);
        int end = Math.min(start + maxButtons - 1, totalPages - 1);
        if (end - start < maxButtons - 1) {
            start = Math.max(0, end - maxButtons + 1);
        }

        // =========================================================
        // 📦 PASAR DATOS AL MODELO
        // =========================================================


        // ✅ CLAVE: 'dispositivosRecientes' ahora recibe TODOS los productos paginados o los resultados de búsqueda.
        model.addAttribute("dispositivosRecientes", productosPrincipalesPaginados.getContent());

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", start);
        model.addAttribute("endPage", end);


        return "tienda/indexTienda";
    }


    @GetMapping("/tienda/sugerencias")
    @ResponseBody
    public List<String> obtenerSugerencias(@RequestParam("query") String query) {
        return productoService.buscarCoincidencias(query);
    }

    @GetMapping("/tienda/producto/{idHash}/{slug}")
    @Transactional(readOnly = true)
    public String verDetalle(
            @PathVariable String idHash, // ← Ahora es String
            @PathVariable String slug,
            Model model) {

        // 3. Decodificamos el Hash para obtener el ID real
        Long idReal = hashidsUtil.decode(idHash);

        // 4. Validación de seguridad: si el hash es inválido o no existe
        if (idReal == null) {

            return "redirect:/tienda";
        }

        // 5. Buscamos usando el ID real obtenido del hash
        ProductoDTO producto = productoService.obtenerPorId(idReal);

        if (producto == null) {
            return "redirect:/tienda";
        }
        productoService.registrarClick(idReal);

        // Pasar la primera categoría para relacionados
        Long categoriaIdParaRelacionados = null;
        if (producto.getCategoriaIds() != null && !producto.getCategoriaIds().isEmpty()) {
            categoriaIdParaRelacionados = producto.getCategoriaIds().get(0);
        }

        List<ProductoDTO> relacionados;
        if (categoriaIdParaRelacionados != null) {
            relacionados = productoService.obtenerRelacionados(categoriaIdParaRelacionados, idReal);
        } else {
            relacionados = new ArrayList<>();
        }

        model.addAttribute("p", producto);
        model.addAttribute("relacionados", relacionados);

        return "tienda/detalleProducto";
    }
}