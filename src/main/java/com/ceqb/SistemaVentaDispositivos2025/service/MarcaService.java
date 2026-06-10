package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.MarcaDTO;

import java.util.List;

public interface MarcaService {
    List<MarcaDTO> listar();
    MarcaDTO guardar(MarcaDTO marca);
    MarcaDTO buscarPorId(Long id);
    void eliminar(Long id);
    void cambiarEstado(Long id);
}
