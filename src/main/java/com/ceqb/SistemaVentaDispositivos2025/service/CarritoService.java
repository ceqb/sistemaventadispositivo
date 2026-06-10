package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.CarritoDTO;
import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import jakarta.servlet.http.HttpSession;

import java.util.List;

public interface CarritoService {

    // Agregar producto al carrito
    //void agregarProducto1(Long usuarioId, ProductoDTO producto, int cantidad);
    void agregarProducto(UsuarioDTO usuarioDTO, Long productoId, int cantidad);
    // Obtener el carrito de un usuario
    List<CarritoDTO> obtenerCarrito(Long usuarioId);
    void fusionarCarrito(List<CarritoDTO> carritoAnonimo, UsuarioDTO usuario);
    // Eliminar un producto del carrito
    void eliminarProducto(Long usuarioId, Long productoId);
    void asignarPrecioUnitario(CarritoDTO item);
    // Vaciar el carrito de un usuario
    void vaciarCarrito(Long usuarioId);
    void agregarProductoAnonimo(Long productoId, int cantidad, HttpSession session);
    // Obtener carrito (otra forma, pero igual al de arriba)
    List<CarritoDTO> obtenerCarritoPorUsuario(Long usuarioId);
    void disminuirCantidad(Long productoId, HttpSession session);
    void aumentarCantidad(Long idProducto,HttpSession session);
    void vaciarCarritoAnonimo(HttpSession session);
}
