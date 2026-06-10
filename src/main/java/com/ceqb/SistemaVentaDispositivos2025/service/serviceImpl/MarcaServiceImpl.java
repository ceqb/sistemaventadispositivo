package com.ceqb.SistemaVentaDispositivos2025.service.serviceImpl;


import com.ceqb.SistemaVentaDispositivos2025.dto.MarcaDTO;
import com.ceqb.SistemaVentaDispositivos2025.mapper.MarcaMapper;
import com.ceqb.SistemaVentaDispositivos2025.model.Marca;
import com.ceqb.SistemaVentaDispositivos2025.repository.MarcaRepository;
import com.ceqb.SistemaVentaDispositivos2025.service.MarcaService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;
    private final MarcaMapper marcaMapper;

    @Override
    public List<MarcaDTO> listar() {
        return marcaRepository.findAll()
                .stream()
                .map(marcaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MarcaDTO guardar(MarcaDTO marcaDTO) {
        Marca marca = marcaMapper.toEntity(marcaDTO);
        return marcaMapper.toDTO(marcaRepository.save(marca));
    }

    @Override
    public MarcaDTO buscarPorId(Long id) {
        return marcaRepository.findById(id)
                .map(marcaMapper::toDTO)
                .orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        marcaRepository.deleteById(id);
    }

    @Override
    public void cambiarEstado(Long id) {
        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cargo no encontrado"));

        marca.setEstado(!marca.isEstado()); // Cambia el estado
        marcaRepository.save(marca);
    }
}
