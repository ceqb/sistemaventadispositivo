package com.ceqb.SistemaVentaDispositivos2025.dto;


import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioLoginDTO implements Serializable {
    private String nombre;
    private String clave;
}
