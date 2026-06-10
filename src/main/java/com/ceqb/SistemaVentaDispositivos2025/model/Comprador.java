package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comprador")
public class Comprador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String telefono;

    private String direccion;

    private Long cantidad;

    private Double total;

    // Método para calcular el total antes de mostrarlo
    public Double calcularTotal() {
        if (producto != null && producto.getPrecio_dpc() != null && cantidad != null) {
            return producto.getPrecio_dpc() * cantidad;
        }
        return 0.0;
    }
    private boolean pagado;

    @ManyToOne
    @JoinColumn(name = "distrito_id")
    private Ubicacion distrito;



    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Producto producto;

}
