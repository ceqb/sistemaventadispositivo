package com.ceqb.SistemaVentaDispositivos2025.dto;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoriaDTO {

    private Long id;
    private String nombreCategoria;
    private Boolean estado = true;
    private List<Producto> productos;


}
