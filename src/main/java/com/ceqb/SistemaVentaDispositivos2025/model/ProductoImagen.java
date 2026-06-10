package com.ceqb.SistemaVentaDispositivos2025.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;


@Entity
@Table(name = "producto_imagen")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImagen implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    @ToString.Exclude // <--- CRITICO
    @JsonBackReference
    private Producto producto;

    @Column(nullable = false)
    private String rutaImagen;

    private Integer orden;
}
