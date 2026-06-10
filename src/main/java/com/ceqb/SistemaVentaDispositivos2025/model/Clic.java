package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clics")
public class Clic implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One con la entidad Producto.
    // Muchos clics pueden pertenecer a un solo producto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // El ID del usuario que realizó el clic (puede ser anónimo).
    @Column(name = "user_id")
    private String userId;

    // La fecha y hora en que se registró el clic.
    // @CreationTimestamp hace que JPA establezca este valor automáticamente al momento de la creación.
    @CreationTimestamp
    @Column(name = "fecha_hora", updatable = false)
    private LocalDateTime fechaHora;
}
