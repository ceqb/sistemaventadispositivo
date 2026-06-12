package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.ProductoImagenMapper;
import com.ceqb.SistemaVentaDispositivos2025.mapper.ProductoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;
import com.ceqb.SistemaVentaDispositivos2025.model.Marca;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.ProductoImagen;
import com.ceqb.SistemaVentaDispositivos2025.repository.CategoriaRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.MarcaRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.ProductoService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {
    //ORIGINAL
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final CategoriaRepository categoriaRepository;
    private final ProductoImagenMapper imagenProductoMapper;
    private final MarcaRepository marcaRepository;


    @Override
    public List<ProductoDTO> listar() {
        // Para listado completo en admin (sin paginación)
        Page<Producto> page = productoRepository.findAllWithCategorias(Pageable.unpaged());
        return page.map(productoMapper::toDTO).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorNombre(String slug) {
        System.out.println("Buscando producto por slug: " + slug);
        Optional<Producto> opt = productoRepository.findBySlugWithImagenes(slug);
        if (opt.isEmpty()) {
            System.out.println("No se encontró producto con slug: " + slug);
            return null;
        }
        Producto p = opt.get();
        System.out.println("Producto encontrado: " + p.getModelo_dpc() + " (ID: " + p.getId() + ")");
        return productoMapper.toDTO(p);
    }

    /******************************************************************************
     obtenerVentasRecientes
     ******************************************************************************/


    @Override
    public Page<ProductoDTO> obtenerProductosPaginacionPrincipal(int pagina, int size) {
        Pageable pageable = PageRequest.of(pagina, size, Sort.by("id").descending());
        Page<Producto> page = productoRepository.findAllWithCategorias(pageable);  // ← usa esta
        return page.map(productoMapper::toDTO);
    }

    @Override
    public List<ProductoDTO> obtenerRelacionados(Long categoriaId, Long productoId) {
        if (categoriaId == null) return new ArrayList<>();
        return productoRepository.findRecomendadosPorCategoria(categoriaId, productoId)
                .stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    /******************************************************************************
     ******************************************************************************/
    @Override
    public Page<ProductoDTO> listarTienda(Pageable pageable) {
        // Llama al método findAll del repositorio, que ya sabe cómo manejar Pageable
        Page<Producto> productosPage = productoRepository.findAll(pageable);

        // Aquí puedes mapear la página de entidades a una página de DTOs
        return productosPage.map(producto -> new ProductoDTO(/* ... */));


    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> obtenerProductosPorCategoriaPaginacion(Long categoriaId, int pagina, int size) {
        Pageable pageable = PageRequest.of(pagina, size, Sort.by("id").descending());

        Page<Producto> paginaProductos = productoRepository.findByCategoriaId(categoriaId, pageable);

        return paginaProductos.map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorCategorias(List<Long> categoriaIds, int pagina, int size) {
        Pageable pageable = PageRequest.of(pagina, size, Sort.by("id").descending());
        Page<Producto> page = productoRepository.findByCategoriasWithCategorias(categoriaIds, pageable);
        return page.map(productoMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductoDTO> buscarPorCategoriasYFiltro(
            List<Long> categoriaIds,
            String query,
            int pagina,
            int size) {

        Pageable pageable = PageRequest.of(pagina, size, Sort.by("id").descending());

        if ((categoriaIds == null || categoriaIds.isEmpty()) && (query == null || query.isBlank())) {
            return obtenerProductosPaginacionPrincipal(pagina, size);
        }

        Page<Producto> page;

        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            if (query != null && !query.isBlank()) {
                page = productoRepository.findDistinctByCategoriasIdInAndModeloContainingIgnoreCase(
                        categoriaIds, query.trim(), pageable);
            } else {
                page = productoRepository.findDistinctByCategoriasIdIn(categoriaIds, pageable);
            }
        } else {
            // Solo búsqueda
            page = productoRepository.buscarPorModelo(query.trim(), pageable);
        }

        return page.map(productoMapper::toDTO);
    }

    @Override
    public List<String> buscarCoincidencias(String query) {
        return productoRepository.buscarCoincidencias(query)
                .stream()
                .map(Producto::getModelo_dpc)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDTO obtenerPorId(Long id) {
        Producto producto = productoRepository.findByIdWithImagenes(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        ProductoDTO dto = productoMapper.toDTO(producto);

        // Cargar categoría principal de forma segura
        dto.setNombresCategorias(
                producto.getCategorias().stream()
                        .map(Categoria::getNombreCategoria)
                        .collect(Collectors.toList())
        );
        return dto;
    }

    @Override
    public Page<ProductoDTO> buscar(String search, List<Long> categorias, int page, int size) {
        // Configuración de paginación + orden (puedes cambiar el criterio de orden)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Caso 1: Ni búsqueda ni categorías → devolver todos (página principal)
        if ((search == null || search.trim().isBlank()) &&
                (categorias == null || categorias.isEmpty())) {
            return obtenerProductosPaginacionPrincipal(page, size);
        }

        Page<Producto> resultPage;

        // Caso 2: Solo búsqueda (sin categorías)
        if ((categorias == null || categorias.isEmpty()) &&
                (search != null && !search.trim().isBlank())) {
            resultPage = productoRepository.buscarPorModelo(search.trim(), pageable);
        }

        // Caso 3: Solo categorías (sin búsqueda)
        else if ((search == null || search.trim().isBlank()) &&
                (categorias != null && !categorias.isEmpty())) {
            resultPage = productoRepository.findDistinctByCategoriasIdIn(categorias, pageable);
        }

        // Caso 4: Ambos filtros (búsqueda + categorías múltiples)
        else {
            resultPage = productoRepository.findDistinctByCategoriasIdInAndModeloContainingIgnoreCase(
                    categorias,
                    search.trim(),
                    pageable
            );
        }

        // Convertir a DTO
        return resultPage.map(productoMapper::toDTO);
    }

    @Override
    @Transactional
    public ProductoDTO guardar(ProductoDTO productoDTO) {
        validarModelo(productoDTO.getModelo_dpc());
        System.out.println("=== INICIO guardar ===");
        System.out.println("Categorías recibidas en SERVICE: " + productoDTO.getCategoriaIds());

        Producto producto = productoMapper.toEntity(productoDTO);

        if (productoDTO.getId_marca() != null) {
            System.out.println("Buscando marca ID: " + productoDTO.getId_marca());
            Marca marca = marcaRepository.findById(productoDTO.getId_marca())
                    .orElseThrow(() -> new RuntimeException("Marca no encontrada con ID: " + productoDTO.getId_marca()));

            producto.setMarca(marca); // <-- Esto es lo que falta para que id_marca no sea null
            System.out.println("Marca asignada: " + marca.getNombreMarca());
        } else {
            // Si la marca es obligatoria, lanzamos error antes de llegar a la BD
            throw new RuntimeException("La marca del producto es obligatoria.");
        }

        System.out.println("Categorías antes de limpiar: " + producto.getCategorias().size());

        // Limpieza obligatoria en actualización/guardar
        producto.getCategorias().clear();
        System.out.println("Categorías después de clear: " + producto.getCategorias().size());

        if (productoDTO.getCategoriaIds() != null && !productoDTO.getCategoriaIds().isEmpty()) {
            System.out.println("Intentando agregar " + productoDTO.getCategoriaIds().size() + " categorías");

            for (Long catId : productoDTO.getCategoriaIds()) {
                System.out.println("Buscando categoría ID: " + catId);
                Categoria categoria = categoriaRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catId));

                System.out.println("Agregando categoría: " + categoria.getNombreCategoria());

                producto.addCategoria(categoria);  // ← este es el punto crítico
            }

            System.out.println("Categorías después de agregar: " + producto.getCategorias().size());
        } else {
            System.out.println("No se recibieron categorías en el DTO");
        }

        // Serie única
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmssSSS"));
        producto.setSerie_dpc("SERIE-" + timestamp);

        // Imágenes (tu lógica actual está bien, pero se puede encapsular mejor)
        if (productoDTO.getImagenes() != null && !productoDTO.getImagenes().isEmpty()) {
            int orden = 1;
            for (String ruta : productoDTO.getImagenes()) {
                ProductoImagen img = imagenProductoMapper.toEntity(ruta, producto, orden++);
                producto.getImagenes().add(img);
            }
        }

        System.out.println("Guardando producto en BD...");
        Producto guardado = productoRepository.save(producto);
        System.out.println("Producto guardado con ID: " + guardado.getId());

        // Verificación final post-save
        System.out.println("Categorías persistidas: " + guardado.getCategorias().size());

        return productoMapper.toDTO(guardado);

    }

    private void validarModelo(String modelo) {
        if (modelo == null || modelo.isBlank()) {
            throw new RuntimeException("El modelo es obligatorio");
        }
        if (modelo.contains("/")) {
            throw new RuntimeException("El modelo no puede contener '/'");
        }
    }

    @Override
    @Transactional
    public ProductoDTO actualizar(ProductoDTO productoDTO, List<MultipartFile> nuevasImagenes) {
        Producto producto = productoRepository.findById(productoDTO.getId_dpc())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        // ✅ Agregar al inicio de guardar() y actualizar()
        validarModelo(productoDTO.getModelo_dpc());

        // Campos básicos
        producto.setRutaFoto_dpc(productoDTO.getRutaFoto_dpc());
        producto.setRutaVideo_dpc(productoDTO.getRutaVideo_dpc());
        producto.setModelo_dpc(productoDTO.getModelo_dpc());
        producto.setDescripcion_dpc(productoDTO.getDescripcion_dpc());
        producto.setPrecio_dpc(productoDTO.getPrecio_dpc());
        producto.setInventarioTotal(productoDTO.getInventarioTotal());
        // ────────────────────────────────────────────────
        // ★★★ Gestión inteligente de MARCA ★★★
        // ────────────────────────────────────────────────
        if (productoDTO.getId_marca() != null) {
            // Solo actualizamos si la marca es diferente a la actual para optimizar
            if (producto.getMarca() == null || !producto.getMarca().getId_marca().equals(productoDTO.getId_marca())) {
                Marca nuevaMarca = marcaRepository.findById(productoDTO.getId_marca())
                        .orElseThrow(() -> new RuntimeException("Marca no encontrada: " + productoDTO.getId_marca()));
                producto.setMarca(nuevaMarca);
                System.out.println("Marca actualizada a: " + nuevaMarca.getNombreMarca());
            }
        }
        // ────────────────────────────────────────────────
        // ★★★ Gestión inteligente de CATEGORÍAS ★★★
        // ────────────────────────────────────────────────
        // Obtener los IDs que llegan del formulario (pueden ser nuevos o los mismos)
        Set<Long> idsDelFormulario = new HashSet<>(productoDTO.getCategoriaIds() != null
                ? productoDTO.getCategoriaIds()
                : Collections.emptyList());

        // 3. Eliminar SOLO las categorías que ya no están en el formulario
        producto.getCategorias().removeIf(cat -> !idsDelFormulario.contains(cat.getId()));

        // 4. Agregar SOLO las que faltan (las nuevas)
        for (Long catId : idsDelFormulario) {
            // Evitar duplicados: solo agregar si no la tiene ya
            if (producto.getCategorias().stream().noneMatch(c -> c.getId().equals(catId))) {
                Categoria categoria = categoriaRepository.findById(catId)
                        .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + catId));
                producto.addCategoria(categoria);
            }
        }
        // ────────────────────────────────────────────────
        // ★★★ Gestión inteligente de IMÁGENES ★★★
        // Imágenes nuevas (tu lógica + evitar duplicados)
        // ────────────────────────────────────────────────

        // 1️⃣ Obtener rutas que vienen del formulario (las que el usuario mantuvo)
        Set<String> rutasDelFormulario = new HashSet<>(
                productoDTO.getImagenes() != null
                        ? productoDTO.getImagenes()
                        : Collections.emptyList()
        );

        // 2️⃣ Eliminar imágenes que ya no están en el formulario
        producto.getImagenes().removeIf(img ->
                !rutasDelFormulario.contains(img.getRutaImagen())
        );

        // 3️⃣ Obtener rutas actuales luego de eliminar
        Set<String> rutasActuales = producto.getImagenes().stream()
                .map(ProductoImagen::getRutaImagen)
                .collect(Collectors.toSet());

        // 4️⃣ Agregar solo nuevas que no existan
        int orden = producto.getImagenes().size() + 1;

        for (String ruta : rutasDelFormulario) {
            if (!rutasActuales.contains(ruta)) {
                producto.getImagenes().add(
                        imagenProductoMapper.toEntity(ruta, producto, orden++)
                );
            }
        }

        Producto actualizado = productoRepository.save(producto);
        return productoMapper.toDTO(actualizado);
    }

    public String guardarArchivo(MultipartFile archivo, String rutaDirectorio) {
        String nombreOriginalCompleto = archivo.getOriginalFilename();
        String extension = "";
        int indicePunto = nombreOriginalCompleto.lastIndexOf('.');

        if (indicePunto > 0) {
            extension = nombreOriginalCompleto.substring(indicePunto);
        }

        // Normalizamos el nombre base antes de añadir el UUID
        String nombreBase = (indicePunto > 0) ? nombreOriginalCompleto.substring(0, indicePunto) : nombreOriginalCompleto;
        String nombreLimpio = normalizarNombre(nombreBase);

        String nombreArchivoFinal = nombreLimpio + "_" + UUID.randomUUID() + extension;

        File directorio = new File(rutaDirectorio);
        if (!directorio.exists()) directorio.mkdirs();

        Path rutaArchivo = Paths.get(rutaDirectorio, nombreArchivoFinal);
        try {
            Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar archivo: " + e.getMessage());
        }
        return nombreArchivoFinal;
    }

    public String normalizarNombre(String nombre) {
        if (nombre == null) return "sin_nombre";
        // 1. Elimina tildes y normaliza a caracteres básicos
        String normalizado = Normalizer.normalize(nombre, Normalizer.Form.NFD);
        // 2. Quita caracteres de tilde, reemplaza ñ por n, convierte a minúsculas, espacios a guiones
        return normalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .replace("ñ", "n").replace("Ñ", "n")
                .replaceAll("[^a-zA-Z0-9]", "_")
                .toLowerCase();
    }
    @Override
    public Page<ProductoDTO> buscarProductosPaginado(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Producto> page2 = productoRepository.buscarPorModeloWithCategorias(query, pageable); // agrega este método si no lo tienes
        return page2.map(productoMapper::toDTO);
    }
    @Transactional
    public void registrarClick(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // inicializar si está en null (por productos viejos en la BD)
        if (producto.getClics() == null) {
            producto.setClics(0);
        }

        producto.setClics(producto.getClics() + 1);
        productoRepository.save(producto);
    }
    @Override
    @Transactional
    public void registrarVenta(Long productoId, int cantidadVendida) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        if (cantidadVendida <= 0) {
            throw new IllegalArgumentException("La cantidad vendida debe ser mayor a cero");
        }
        // 🔒 Calcular disponible
        int reservadoActual = producto.getInventarioReservado() != null
                ? producto.getInventarioReservado()
                : 0;

        int total = producto.getInventarioTotal() != null
                ? producto.getInventarioTotal()
                : 0;

        int disponible = total - reservadoActual;

        // 🚨 VALIDACIÓN CRÍTICA
        if (cantidadVendida > disponible) {
            throw new IllegalStateException(
                    "Stock insuficiente para el producto: " + producto.getModelo_dpc()
            );
        }

        // ✅ RESERVAR INVENTARIO
        producto.setInventarioReservado(reservadoActual + cantidadVendida);

        // 📊 MÉTRICAS
        producto.setVentasRecientes(
                (producto.getVentasRecientes() != null ? producto.getVentasRecientes() : 0) + cantidadVendida
        );

        productoRepository.save(producto);
    }

    @Override
    public void validarStock(Long productoId, int cantidadSolicitada) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int total = producto.getInventarioTotal() != null ? producto.getInventarioTotal() : 0;
        int reservado = producto.getInventarioReservado() != null ? producto.getInventarioReservado() : 0;

        int disponible = total - reservado;

        if (disponible <= 0) {
            throw new RuntimeException("Producto sin stock disponible");
        }

        if (cantidadSolicitada > disponible) {
            throw new RuntimeException(
                    "Stock insuficiente. Disponible: " + disponible
            );
        }
    }


}