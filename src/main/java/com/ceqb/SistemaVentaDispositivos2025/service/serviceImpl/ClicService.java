package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.model.Clic;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.repository.ClicRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClicService {

    private final ClicRepository clicRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void registrarClic(Long productoId, String userId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Crear el registro de clic usando userId (String)
        Clic clic = Clic.builder()
                .producto(producto)
                .userId(userId)   // <--- aquí va el userId directamente
                .build();

        clicRepository.save(clic);

        // Incrementar y guardar clicsCount
        //producto.incrementarClics();
        productoRepository.save(producto);  // persiste clicsCount en tabla productos
    }
}
