package com.ceqb.SistemaVentaDispositivos2025.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO implements Serializable {

    private Long id;
    private String usuario;


    private String clave;
    private String nombre;
    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico no tiene un formato válido")
    private String correo;
    private Integer intentosFallidos;
    private Boolean bloqueado;

    private LocalDateTime expiracionCodigo;
    private String codigoDesbloqueo;

    private Boolean verificado;
    private String tokenVerificacion;

    private int idCargo;        // Para actualizar el cargo
    private String nombreCargo; // Para mostrar el nombre del cargo



}
