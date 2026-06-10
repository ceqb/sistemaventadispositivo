package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;


import com.ceqb.SistemaVentaDispositivos2025.dto.CompradorDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UbicacionDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.CompradorMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Comprador;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.Ubicacion;
import com.ceqb.SistemaVentaDispositivos2025.repository.CompradorRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UbicacionRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import com.ceqb.SistemaVentaDispositivos2025.service.CompradorProductoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CompradorProductoServiceImpl implements CompradorProductoService {

    private final ProductoRepository productoRepository;
    private final CompradorRepository compradorRepository;
    private final UbicacionRepository ubicacionRepository;
    private final CompradorMapper compradorMapper;

    public void guardar(CompradorDTO compradorDTO) {

        // 1️⃣ Buscar producto PRIMERO (para tener el precio fresco de la BD)
        Producto producto = productoRepository.findById(compradorDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + compradorDTO.getProductoId()));
        // 2️⃣ Buscar Ubicacion
        Ubicacion ubicacion = ubicacionRepository.findById(compradorDTO.getDistritoId())
                .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));

        // 2️⃣ Convertir DTO → Entity
        Comprador comprador = compradorMapper.toEntity(compradorDTO);

        // 3️⃣ CÁLCULO MANUAL (Aseguramos que no sea nulo)
        double precio = producto.getPrecio_dpc();
        int cantidad = (compradorDTO.getCantidad() != null) ? compradorDTO.getCantidad().intValue() : 1;
        double totalCalculado = precio * cantidad;

        comprador.setProducto(producto);
        comprador.setDistrito(ubicacion);
        comprador.setTotal(totalCalculado); // 👈 Esta es la línea más importante


        // 5️⃣ Guardar
        compradorRepository.save(comprador);
    }

    public List<CompradorDTO> listar() {
        return compradorRepository.findAll().stream().map(comp -> {
            CompradorDTO dto = compradorMapper.toDTO(comp);

            if (comp.getProducto() != null) {
                // 1. Obtenemos el precio y la cantidad
                double precio = comp.getProducto().getPrecio_dpc();
                long cantidad = (comp.getCantidad() != null) ? comp.getCantidad() : 0;

                // 2. HACEMOS EL CÁLCULO REAL
                dto.setTotal(precio * cantidad);

                // 3. Construimos el resumen del producto
                CompradorDTO.ProductoResumenDTO pResumen = CompradorDTO.ProductoResumenDTO.builder()
                        .nombre(comp.getProducto().getModelo_dpc())
                        .precio(precio)
                        .foto(comp.getProducto().getRutaFoto_dpc())
                        .descripcion(comp.getProducto().getDescripcion_dpc())
                        .stock(comp.getProducto().getInventarioDisponible())
                        .build();

                dto.setProducto(pResumen);
            }

            return dto;
        }).toList();
    }
    public CompradorDTO obtenerPorId(Long id) {
        Comprador comprador = compradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado con ID: " + id));

        // Usamos la misma lógica de mapeo que en el listar para traer la info del producto
        CompradorDTO dto = compradorMapper.toDTO(comprador);

        if (comprador.getProducto() != null) {
            dto.setProducto(CompradorDTO.ProductoResumenDTO.builder()
                    .nombre(comprador.getProducto().getModelo_dpc())
                    .precio(comprador.getProducto().getPrecio_dpc())
                    .foto(comprador.getProducto().getRutaFoto_dpc())
                    .descripcion(comprador.getProducto().getDescripcion_dpc())
                    .stock(comprador.getProducto().getInventarioDisponible())
                    .build());
            dto.setProductoId(comprador.getProducto().getId());
        }

        return dto;
    }
    // Método para actualizar los datos
    public void actualizar(Long id, CompradorDTO dto) {
        // 1. Buscar el registro existente
        Comprador compradorExistente = compradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede editar: Comprador no encontrado"));

        // 2. Actualizar campos básicos
        compradorExistente.setNombre(dto.getNombre());
        compradorExistente.setTelefono(dto.getTelefono());
        compradorExistente.setDireccion(dto.getDireccion());

        compradorExistente.setCantidad(dto.getCantidad());
        compradorExistente.setPagado(dto.isPagado());

        // 3. Si el producto cambió, buscamos el nuevo y lo asignamos
        if (!compradorExistente.getProducto().getId().equals(dto.getProductoId())) {
            Producto nuevoProducto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("El nuevo producto seleccionado no existe"));
            compradorExistente.setProducto(nuevoProducto);
        }
        // 🔥 ACTUALIZAR DISTRITO PROFESIONALMENTE
        if (dto.getDistritoId() != null) {

            if (compradorExistente.getDistrito() == null ||
                    !compradorExistente.getDistrito().getId().equals(dto.getDistritoId())) {

                Ubicacion nuevaUbicacion = ubicacionRepository.findById(dto.getDistritoId())
                        .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));

                compradorExistente.setDistrito(nuevaUbicacion);
            }

        } else {
            compradorExistente.setDistrito(null);
        }

        // 4. Guardar cambios (Transactional se encarga del commit)
        compradorRepository.save(compradorExistente);
    }
}