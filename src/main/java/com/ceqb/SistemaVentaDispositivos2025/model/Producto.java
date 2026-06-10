package com.ceqb.SistemaVentaDispositivos2025.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "productos")
public class Producto implements Serializable {
    //ORINGIAL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dpc") // columna en BD
    private Long id;

    @Column(name = "rutaFoto_dpc")
    private String rutaFoto_dpc;

    @Column(name = "rutaVideo_dpc")
    private String rutaVideo_dpc;

    @Column(name = "serie_dpc")
    private String serie_dpc;

    @Column(name = "modelo_dpc")
    private String modelo_dpc;

    @Column(name = "descripcion_dpc", columnDefinition = "TEXT")
    private String descripcion_dpc;

    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    private boolean destacado;

    @Column(name = "precio_dpc")
    private Double precio_dpc; // PRECIO BASE

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProductoImagen> imagenes = new HashSet<>();

    /**
     CAMPOS PARA PRECIOS DINAMICOS

    @Column(name = "inventario", nullable = false)
    private Integer inventario=0;
    **/




    // 🧱 MODELO C
    @Column(name = "inventario_total")
    private Integer inventarioTotal;

    @Column(name = "inventario_reservado")
    private Integer inventarioReservado;

    @Column(name = "ventasRecientes")
    private Integer ventasRecientes=0;

    @Column(name = "clics")
    private Integer clics=0;

    /**
     * 🔹 Cálculo de días en stock (NO se guarda en BD)
     */
    @Transient
    public long getDiasEnStock() {
        if (fechaCreacion == null) return 0;
        return Duration.between(fechaCreacion, LocalDateTime.now()).toDays();
    }

    @Transient
    public int getInventarioDisponible() {
        int total = inventarioTotal != null ? inventarioTotal : 0;
        int reservado = inventarioReservado != null ? inventarioReservado : 0;
        return total - reservado;
    }

    // Método calculado (NO se guarda en BD)
    @Transient
    public Double getPrecioDinamico() {
        double precioFinal = precio_dpc != null ? precio_dpc : 0.0;

        int horaDelDia = java.time.LocalDateTime.now().getHour();

        // 📌 1. Factor Inventario
        int inventarioDisponible = getInventarioDisponible();

        if (inventarioDisponible < 10) {
            precioFinal *= 1.10;
        } else if (inventarioDisponible > 100) {
            precioFinal *= 0.95;
        }

        // 📌 2. Factor Demanda
        if (ventasRecientes != null) {
            if (ventasRecientes > 50) precioFinal *= 1.15;
            else if (ventasRecientes < 5) precioFinal *= 0.97;
        }

        // 📌 3. Factor Hora
        if (horaDelDia >= 18 && horaDelDia <= 22) precioFinal *= 1.05;
        else if (horaDelDia >= 2 && horaDelDia <= 6) precioFinal *= 0.90;

        // 📌 4. Factor Clics + Conversión
        if (clics != null && ventasRecientes != null) {
            double ratioConversion = ventasRecientes / (clics + 1.0);
            if (clics > 100 && ratioConversion < 0.02) precioFinal *= 0.90;
            else if (ratioConversion > 0.10) precioFinal *= 1.10;
        }

        // 📌 5. Factor Antigüedad (descuento progresivo)
        long diasEnStock = getDiasEnStock();
        if (diasEnStock > 30) {
            // Ejemplo: 1% de descuento cada 30 días, máximo 20%
            double descuento = Math.min((diasEnStock / 30) * 0.01, 0.20);
            precioFinal *= (1 - descuento);
        }

        // 📌 5. Normalización
        if (precioFinal < precio_dpc * 0.5) precioFinal = precio_dpc * 0.5;
        if (precioFinal > precio_dpc * 2) precioFinal = precio_dpc * 2;

        return Math.round(precioFinal * 100.0) / 100.0;
    }

    /*
     ****************************************************
     */
    @ManyToOne
    @JoinColumn(name = "id_marca", nullable = false)
    private Marca marca;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL)
    private List<Comprador> compradores;
    // ────────────────────────────────────────────────
    // ★★★ Relación ManyToMany con Categorías ★★★
    // ────────────────────────────────────────────────
    @ManyToMany
    @JoinTable(
            name = "producto_categoria",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    @JsonIgnoreProperties({"productos"})           // evita recursión infinita
    @ToString.Exclude                               // evita stackoverflow en logs
    private List<Categoria> categorias = new ArrayList<>();

    // Helpers muy útiles para la lógica de negocio y el controlador
    public void addCategoria(Categoria categoria) {
        if (categoria == null) return;
        if (!this.categorias.contains(categoria)) {
            this.categorias.add(categoria);
            categoria.getProductos().add(this);     // mantener sincronía bidireccional
        }
    }

    public void removeCategoria(Categoria categoria) {
        if (categoria == null) return;
        this.categorias.remove(categoria);
        categoria.getProductos().remove(this);      // mantener sincronía
    }

    public void clearCategorias() {
        for (Categoria cat : new ArrayList<>(this.categorias)) {
            removeCategoria(cat);
        }
    }

    // Opcional: equals y hashCode solo por ID (recomendado en entidades JPA)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}