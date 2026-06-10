package com.ceqb.SistemaVentaDispositivos2025.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductoDTO implements Serializable {
    private Long id_dpc;
    private String IdHash;

    private String rutaFoto_dpc; // imagenPrincipal (actual)
    private List<String> imagenes; // nuevas imágenes
    private String rutaVideo_dpc;
    private String serie_dpc;
    private String modelo_dpc;
    private String descripcion_dpc;
    private boolean destacado;
    private Double precio_dpc;
    private boolean favorito;
    //ORIGINAL
    private Double precioDinamico;

    // 🧱 MODELO C
    private Integer inventarioTotal;
    private Integer inventarioReservado;
    private Integer inventarioDisponible;


    private Integer ventasRecientes;
    private Integer clics;
    private Long diasEnStock;

    private Long id_marca;
    private String nombreMarca;
    //id_categoria
    private Long id;
    private String nombreCategoria;

    private List<Long> categoriaIds = new ArrayList<>();
    private List<String> nombresCategorias = new ArrayList<>();
}