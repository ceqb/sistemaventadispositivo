package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String nombre); // Para buscar por nombre de usuario
    List<Usuario> findByCargoNombrecargoIgnoreCase(String nombreCargo);

    /*
     **********************************************************************
     *                           CLIENTE CON MAS COMPRAS                   *
     **********************************************************************
     */

    /*
     **********************************************************************
     *                           CLIENTES REGISTRADOS                   *
     **********************************************************************
     */
    long count();
    /*
     **********************************************************************
     *                           CLIENTES ANÓNIMOS                   *
     **********************************************************************
     */
    boolean existsByCorreo(String correo);

    boolean existsByUsuario(String usuario);

    Optional<Usuario> findByTokenVerificacion(String token); //Sirve para verificar email
    Optional<Usuario> findByTokenResetPassword(String token);

    Optional<Usuario> findByCorreo(String correo);


}
