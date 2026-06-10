package com.ceqb.SistemaVentaDispositivos2025.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UbicacionDTO {
    private String departamento;
    private String provincia;
    private String distrito;
}
