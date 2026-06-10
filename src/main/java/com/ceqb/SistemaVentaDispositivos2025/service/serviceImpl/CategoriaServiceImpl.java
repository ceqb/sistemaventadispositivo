package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;

import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;
import com.ceqb.SistemaVentaDispositivos2025.repository.CategoriaRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.CategoriaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }

    @Override
    public Categoria obtenerPorNombre(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        // Busca exactamente por nombre (case-insensitive si quieres)
        return categoriaRepository.findByNombreCategoriaIgnoreCase(nombre.trim())
                .orElse(null);
    }
}
