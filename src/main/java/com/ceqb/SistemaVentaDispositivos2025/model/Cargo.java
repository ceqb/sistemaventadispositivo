package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;

import lombok.*;

import java.io.Serializable;
import java.util.List;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "cargos")
public class Cargo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idcargo;

    private String nombrecargo;

    @OneToMany(mappedBy = "cargo")
    private List<Usuario> usuarios;
}
