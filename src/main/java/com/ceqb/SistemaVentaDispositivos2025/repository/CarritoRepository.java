package com.ceqb.SistemaVentaDispositivos2025.repository;
import com.ceqb.SistemaVentaDispositivos2025.model.Carrito;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CarritoRepository extends JpaRepository<Carrito, Long> {

    // Obtener todos los items del carrito de un usuario
    List<Carrito> findByUsuario(Usuario usuario);

    // Todos los items de un usuario por su id
    List<Carrito> findByUsuario_Id(Long usuarioId);

    // Buscar un producto específico en el carrito de un usuario
    Optional<Carrito> findByUsuarioAndProducto_Id(Usuario usuario, Long productoId);
    //Optional<Carrito> findByProducto_Id(Long productoId);
    // Eliminar un producto específico del carrito de un usuario
    void deleteByUsuarioAndProducto_Id(Usuario usuario, Long productoId);

    // Vaciar carrito de un usuario
    void deleteByUsuario(Usuario usuario);
}
