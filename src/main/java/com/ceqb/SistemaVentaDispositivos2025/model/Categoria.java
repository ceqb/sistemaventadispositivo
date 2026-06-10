package com.ceqb.SistemaVentaDispositivos2025.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categorias")
public class Categoria implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombreCategoria;

    @Column(name = "estado")
    private Boolean estado = true;

    @ManyToMany(mappedBy = "categorias")
    @JsonBackReference              // lado inverso → no se serializa desde aquí
    @ToString.Exclude
    @JsonIgnoreProperties({"categorias"})
    private List<Producto> productos = new ArrayList<>();

    // Helpers (opcional pero muy recomendado)
    public void addProducto(Producto producto) {
        if (producto == null) return;
        if (!this.productos.contains(producto)) {
            this.productos.add(producto);
            producto.getCategorias().add(this);
        }
    }

    public void removeProducto(Producto producto) {
        if (producto == null) return;
        this.productos.remove(producto);
        producto.getCategorias().remove(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Categoria categoria = (Categoria) o;
        return Objects.equals(id, categoria.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}