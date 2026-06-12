package com.ceqb.SistemaVentaDispositivos2025.controller;

import com.ceqb.SistemaVentaDispositivos2025.dto.MarcaDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Marca;
import com.ceqb.SistemaVentaDispositivos2025.service.MarcaService;
import com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl.AnaliticaProductoService;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService productoService;
    private final ProductoRepository productoRepository;//MODIFICAR NO ES RECOMANDEBLAE
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    //ORIGINAL
    private final AnaliticaProductoService analiticaProductoService;

    @GetMapping("/producto/{id}")
    public String verProducto(@PathVariable Long id, Model model) {

        // Llamamos al servicio (que ya tiene implementada la Solución 3)
        ProductoDTO producto = productoService.obtenerPorId(id);

        if (producto == null) {
            return "redirect:/admin/productos?error=no_encontrado";
        }

        model.addAttribute("producto", producto);
        return "tienda/indexTienda";
    }

    @PostMapping("/comprar/{id}")
    @Transactional
    public String comprarProducto(@PathVariable Long id, @RequestParam int cantidad) {
        productoService.registrarVenta(id, cantidad);
        return "redirect:/carrito";
    }


    @GetMapping
    @Transactional(readOnly = true)
    public String listarProductos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "60") int size,
            @RequestParam(name = "categoria", required = false) List<Long> cat,
            Model model) {

        // 🔥 Convertir página visible (base 1) a base 0
        int pageIndex = Math.max(page - 1, 0);
        Page<ProductoDTO> productosPrincipalesPaginados;

        if (cat != null && !cat.isEmpty()) {
            productosPrincipalesPaginados = productoService.buscar(null, cat, pageIndex, size);
            model.addAttribute("categoriasSeleccionadas", cat);
        } else {
            productosPrincipalesPaginados = productoService.obtenerProductosPaginacionPrincipal(pageIndex, size);
            model.addAttribute("categoriasSeleccionadas", List.of());
        }

        model.addAttribute("productos", productosPrincipalesPaginados.getContent());

        int totalPages = productosPrincipalesPaginados.getTotalPages();
        int currentPage = productosPrincipalesPaginados.getNumber();
        int maxButtons = 5;

        // Calcular rango de páginas a mostrar
        int start = Math.max(0, currentPage - 2);
        int end = Math.min(start + maxButtons - 1, totalPages - 1);
        if (end - start < maxButtons - 1) {
            start = Math.max(0, end - maxButtons + 1);
        }
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", start);
        model.addAttribute("endPage", end);
        // Calcular rango de páginas a mostrar

        return "admin/productos";
    }

    // Mostrar formulario para nuevo producto
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setImagenes(new ArrayList<>());
        // Inicializa el DTO sin generar la serie.
        model.addAttribute("producto", new ProductoDTO());
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("marcas", marcaService.listar());

        return "admin/nuevoProducto";
    }

    // Guardar producto y mostrar el número de serie
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute("producto") ProductoDTO productoDTO,
                                  @RequestParam("archivoFoto") MultipartFile archivoFoto,
                                  @RequestParam(value = "archivosImagenes", required = false) List<MultipartFile> archivosImagenes,
                                  @RequestParam("archivoVideo") MultipartFile archivoVideo,
                                  @RequestParam(required = false, defaultValue = "0") int returnPage,
                                  Model model) { // Importante: usa Model en lugar de RedirectAttributes

        try {
            String nombreLimpio = productoService.normalizarNombre(productoDTO.getModelo_dpc());
            String rutaFisica = "uploads/" + nombreLimpio;
            // Guardar foto
            if (!archivoFoto.isEmpty()) {
                String nombreFoto = productoService.guardarArchivo(archivoFoto, rutaFisica);
                productoDTO.setRutaFoto_dpc(nombreFoto);
            }

            if (!archivoVideo.isEmpty()) {
                String nombreVideo = productoService.guardarArchivo(archivoVideo, "uploads/videos");
                productoDTO.setRutaVideo_dpc(nombreVideo);
            }

            // 3. GUARDAR IMÁGENES SECUNDARIAS (Galería)
            if (archivosImagenes != null && !archivosImagenes.isEmpty()) {
                List<String> nombresGaleria = new ArrayList<>();
                for (MultipartFile img : archivosImagenes) {
                    if (!img.isEmpty()) {
                        // Usamos la MISMA rutaFisica que la principal
                        String nombreImg = productoService.guardarArchivo(img, rutaFisica);
                        nombresGaleria.add(nombreImg); // Solo nombre en la lista para BD
                    }
                }
                productoDTO.setImagenes(nombresGaleria);
            }
            System.out.println("Categorías recibidas en controlador: " + productoDTO.getCategoriaIds());
            // o mejor:
            if (productoDTO.getCategoriaIds() != null) {
                System.out.println("IDs: " + String.join(", ", productoDTO.getCategoriaIds()
                        .stream().map(String::valueOf).toList()));
            } else {
                System.out.println("categoriaIds es NULL");
            }
            List<MarcaDTO> listaMarcas = marcaService.listar(); // O como se llame tu servicio

            // Llama al servicio para guardar el producto.
            // El servicio debe devolver el objeto guardado con la serie ya asignada.
            ProductoDTO productoGuardado = productoService.guardar(productoDTO);

            // 1. Agrega el objeto guardado con la serie al modelo para la vista.
            model.addAttribute("producto", productoGuardado);
            model.addAttribute("marcas", listaMarcas);
            // 2. Agrega las demás dependencias del modelo que tu vista necesite.
            model.addAttribute("categorias", categoriaService.listar());

            model.addAttribute("mensaje", "Producto guardado exitosamente.");

            // 3. Importante: Retorna la misma vista para no perder el modelo.
            return "redirect:/admin/productos?page="+ returnPage;

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/productos/nuevo?returnPage=" + returnPage;
        }
    }


    // Mostrar formulario de edición
    @GetMapping("/editar/{id}")
    public String editarProducto(@PathVariable Long id, Model model,@RequestParam(required = false, defaultValue = "0") int returnPage) {
        ProductoDTO producto = productoService.obtenerPorId(id);
        if (producto == null) {
            return "redirect:/admin/productos?error=producto_no_encontrado";
        }

        if (producto.getCategoriaIds() == null || producto.getCategoriaIds().isEmpty()) {
            // Opcional: cargar desde entidad si el DTO no las trae
            // Pero lo ideal es que el mapper lo haga
        }
        // LOG 1: Verificar que el DTO tenga categorías al llegar al controlador

        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categoriaService.listar());
        model.addAttribute("marcas", marcaService.listar());

        model.addAttribute("returnPage", returnPage);
        // El retorno a 'admin/nuevoProducto' es correcto
        return "admin/nuevoProducto";
    }

    @PostMapping("/actualizar/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            @ModelAttribute("producto") ProductoDTO productoDTO,
            @RequestParam(value = "archivoFoto", required = false) MultipartFile archivoFoto,
            @RequestParam(value = "archivoVideo", required = false) MultipartFile archivoVideo,
            @RequestParam(value = "archivosImagenes", required = false) List<MultipartFile> archivosImagenes,
            @RequestParam(required = false, defaultValue = "0") int returnPage,
            RedirectAttributes redirectAttributes) {

        productoDTO.setId_dpc(id);

        try {
            ProductoDTO productoExistente = productoService.obtenerPorId(id);
            if (productoExistente == null) throw new RuntimeException("El producto no existe.");

            String nombreAntiguo = productoService.normalizarNombre(productoExistente.getModelo_dpc());
            String nombreNuevo = productoService.normalizarNombre(productoDTO.getModelo_dpc());
            String rutaFisica = "uploads/" + nombreNuevo;

            // ✅ Renombrar carpeta ANTES de guardar archivos
            if (!nombreAntiguo.equals(nombreNuevo)) {
                File dirAntiguo = new File("uploads/" + nombreAntiguo);
                File dirNuevo = new File("uploads/" + nombreNuevo);

                if (dirAntiguo.exists()) {
                    boolean ok = dirAntiguo.renameTo(dirNuevo);
                    if (!ok) throw new RuntimeException(
                            "No se pudo renombrar la carpeta del modelo"
                    );
                }
            }

            // Foto principal
            if (archivoFoto != null && !archivoFoto.isEmpty()) {
                String nombreFoto = productoService.guardarArchivo(archivoFoto, rutaFisica);
                productoDTO.setRutaFoto_dpc(nombreFoto);
            } else {
                productoDTO.setRutaFoto_dpc(productoExistente.getRutaFoto_dpc());
            }

            // Video
            if (archivoVideo != null && !archivoVideo.isEmpty()) {
                String nombreVideo = productoService.guardarArchivo(archivoVideo, "uploads/videos");
                productoDTO.setRutaVideo_dpc(nombreVideo);
            } else {
                productoDTO.setRutaVideo_dpc(productoExistente.getRutaVideo_dpc());
            }

            // Imágenes galería
            List<String> imagenesFormulario = productoDTO.getImagenes() != null
                    ? new ArrayList<>(productoDTO.getImagenes())
                    : new ArrayList<>();

            if (archivosImagenes != null) {
                for (MultipartFile file : archivosImagenes) {
                    if (!file.isEmpty()) {
                        String nombreImg = productoService.guardarArchivo(file, rutaFisica);
                        imagenesFormulario.add(nombreImg);
                    }
                }
            }
            productoDTO.setImagenes(imagenesFormulario);

            productoService.actualizar(productoDTO, null);

            redirectAttributes.addFlashAttribute("mensaje", "¡Producto actualizado correctamente!");
            return "redirect:/admin/productos?page=" + returnPage;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return "redirect:/admin/productos/editar/" + id + "?returnPage=" + returnPage;
        }
    }
}