package com.ceqb.SistemaVentaDispositivos2025.mapper;

import com.ceqb.SistemaVentaDispositivos2025.dto.UsuarioDTO;

import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import com.ceqb.SistemaVentaDispositivos2025.repository.CargoRepository;



public class UsuarioMapper {

    public static UsuarioDTO toDto(Usuario usuario) {
        if (usuario == null) return null;

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .usuario(usuario.getUsuario())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .verificado(Boolean.FALSE.equals(usuario.getBloqueado()))
                .tokenVerificacion(usuario.getTokenVerificacion())

                .bloqueado(Boolean.TRUE.equals(usuario.getBloqueado()))
                .intentosFallidos(usuario.getIntentosFallidos() != null ? usuario.getIntentosFallidos() : 0)
                .expiracionCodigo(usuario.getExpiracionCodigo())
                .idCargo(usuario.getCargo() != null ? usuario.getCargo().getIdcargo() : 0)
                .nombreCargo(usuario.getCargo() != null ? usuario.getCargo().getNombrecargo() : null)
                .bypassValidaciones(Boolean.TRUE.equals(usuario.getBypassValidaciones()))
                .build();
    }

    // ✅ Este método ya no depende del CargoRepository
    public static Usuario toEntity(UsuarioDTO dto) {
        if (dto == null) return null;

        Usuario usuario = new Usuario();
        usuario.setId(dto.getId());
        usuario.setUsuario(dto.getUsuario());
        usuario.setNombre(dto.getNombre());
        usuario.setCorreo(dto.getCorreo());
        usuario.setClave(dto.getClave());
        usuario.setBloqueado(dto.getBloqueado());
        usuario.setIntentosFallidos(dto.getIntentosFallidos());
        usuario.setExpiracionCodigo(dto.getExpiracionCodigo());

        // El cargo NO se asigna aquí. Lo hará el servicio.

        return usuario;
    }
    }


