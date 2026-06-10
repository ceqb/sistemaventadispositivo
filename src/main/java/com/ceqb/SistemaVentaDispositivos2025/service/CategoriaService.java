package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.model.Categoria;

import java.util.List;

public interface CategoriaService {

    List<Categoria> listar();
    Categoria guardar(Categoria categoria);
    Categoria obtenerPorId(Long id);
    void eliminar(Long id);
    Categoria obtenerPorNombre(String nombre);
}
