package com.ceqb.SistemaVentaDispositivos2025.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Ubicacion")
public class Ubicacion {

    @Id
    private Long id;

    private String departamento;
    private String provincia;
    private String distrito;
}
