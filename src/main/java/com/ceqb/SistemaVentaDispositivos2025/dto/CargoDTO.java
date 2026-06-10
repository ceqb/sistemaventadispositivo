package com.ceqb.SistemaVentaDispositivos2025.dto;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CargoDTO implements Serializable {
    private int idcargo;
    private String nombreCargo;
}
