package com.ceqb.SistemaVentaDispositivos2025.service;

import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {
    Optional<UsuarioDTO> login(String nombre, String clave);
    List<UsuarioDTO> listarTodos();
    void registrarNuevoUsuario(UsuarioDTO usuarioDTO, HttpServletRequest request);
    UsuarioDTO buscarPorId(Long id);
    UsuarioDTO buscarPorUsuario(String usuario);
    UsuarioDTO desbloquearUsuario(String usuario, String codigo, HttpServletRequest request);
    UsuarioDTO reenviarCodigoDesbloqueo(String usuario);
    void eliminar(Long id);
    boolean cambiarClave(String nombreUsuario, String nuevaClave);
    List<UsuarioDTO> obtenerRepartidores();
    boolean existeCorreo(String correo);
    boolean existeUsuario(String usuario);
    void enviarCorreoVerificacion(String correo, String token, HttpServletRequest request);
    boolean verificarCuenta(String token);
    void reenviarCorreoVerificacion(String correo, HttpServletRequest request);
    void enviarTokenResetPassword(String correo,HttpServletRequest request);
    boolean restablecerContrasena(String token, String nuevaClave);
    Usuario save(Usuario usuario);
    Optional<Usuario> findByTokenResetPassword(String token);
}
