package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "marcas")
public class Marca implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_marca;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombreMarca;

    @Column(name = "estado", nullable = false)
    private boolean estado;
}
