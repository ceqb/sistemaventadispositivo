package com.ceqb.SistemaVentaDispositivos2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarcaDTO implements Serializable {
    private Long id_marca;
    private String nombreMarca;
    private boolean estado;

}
