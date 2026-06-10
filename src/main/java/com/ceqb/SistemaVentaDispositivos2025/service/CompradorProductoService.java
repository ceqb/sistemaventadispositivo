package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.CompradorDTO;

import java.util.List;

public interface CompradorProductoService {

     void guardar(CompradorDTO compradorDTO);
     List<CompradorDTO> listar();
     CompradorDTO obtenerPorId(Long id);
     void actualizar(Long id, CompradorDTO dto);
}
