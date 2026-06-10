package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.dto.ProductoDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.FavoritoMapper;
import com.ceqb.SistemaVentaDispositivos2025.mapper.ProductoMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Favorito;
import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.ceqb.SistemaVentaDispositivos2025.repository.FavoritoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.ProductoRepository;
import com.ceqb.SistemaVentaDispositivos2025.repository.UsuarioRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.FavoritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoritoServiceImpl implements FavoritoService {

    private final FavoritoRepository favoritoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;
    private final FavoritoMapper favoritoMapper;
    @Override
    public boolean toggleFavorito(Long usuarioId, Long productoId) {
        // Buscar si ya existe favorito
        var favorito = favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId);

        if (favorito.isPresent()) {
            // Ya existe → eliminar → ahora NO es favorito
            favoritoRepository.delete(favorito.get());
            return false;
        }

        // Si no existe → crear nuevo favorito
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Favorito nuevo = new Favorito();
        nuevo.setUsuario(usuario);
        nuevo.setProducto(producto);
        nuevo.setFechaRegistro(java.time.LocalDateTime.now());

        favoritoRepository.save(nuevo);

        return true; // Ahora ES favorito
    }

    @Override
    public boolean esFavorito(Long usuarioId, Long productoId) {
        return favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId)
                .isPresent();    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDTO> obtenerFavoritosUsuario(Long usuarioId) {
        // 1. Obtienes la lista de Favoritos
        List<Favorito> favoritos = favoritoRepository.findAllByUsuarioIdWithProductAndCategories(usuarioId);

        // 2. Mapeas manualmente al DTO extrayendo el producto
        return favoritos.stream()
                .map(fav -> {
                    ProductoDTO dto = productoMapper.toDTO(fav.getProducto());
                    // Forzamos a true porque si está en esta lista, obviamente es favorito
                    dto.setFavorito(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarPorProductoYUsuario(Long productoId, Long usuarioId) {
        // 1. Encontrar el registro Favorito por ProductoId y UsuarioId
        // Asegúrate de que tu repositorio tenga este método:
        Optional<Favorito> favoritoOptional = favoritoRepository.findByUsuario_IdAndProducto_Id(usuarioId, productoId);

        if (favoritoOptional.isPresent()) {
            // 2. Eliminar usando el ID del registro Favorito
            favoritoRepository.delete(favoritoOptional.get());
        } else {
            // Manejar el caso donde no se encuentra el favorito (opcional)
            throw new RuntimeException("El producto no está marcado como favorito para este usuario.");
        }

    }

    @Override
    public Integer contarFavoritosPorUsuario(Long usuarioId) {
        return favoritoRepository.countByUsuarioId(usuarioId);
    }
}

