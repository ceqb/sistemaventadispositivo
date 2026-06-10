package com.ceqb.SistemaVentaDispositivos2025.dto;

import com.ceqb.SistemaVentaDispositivos2025.model.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoImagenDTO implements Serializable {

    private Long id;
    private Long productoId;
    private String rutaImagen;
    private Integer orden;
}
