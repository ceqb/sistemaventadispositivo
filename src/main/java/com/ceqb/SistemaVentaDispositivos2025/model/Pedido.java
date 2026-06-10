package com.ceqb.SistemaVentaDispositivos2025.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "numeroPedido")
    private String numeroPedido;

    @Column(name = "direccionEntrega", nullable = false)
    private String direccionEntrega;

    @Column(name = "preferenciaId")
    private String preferenciaId;

    @Column(name = "paymentId")
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago")
    private MetodoPago metodoPago;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_procesando")
    private LocalDateTime fechaProcesando;

    @Column(nullable = false)
    private boolean reservaLiberada = false;
    @Column(name = "fecha_confirmacion_reserva")
    private LocalDateTime fechaConfirmacionReserva;
    private boolean reservaConfirmada;
    @Column(name = "fecha_pedido", nullable = false, length = 255)
    private LocalDateTime fechaPedido;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;

    @Column(name = "qr_token", unique = true)
    private String qrToken;

    @Column(name = "qr_usado")
    private Boolean qrUsado = false;

    @Column(name = "qr_usado_en")
    private LocalDateTime qrUsadoEn;

     /*@Enumerated(EnumType.STRING)
    private EstadoPago estadoPago;*/

    private String estadoPago; // Ejemplo: "Pendiente", "Pagado"

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoPedido; // Ejemplo: "Procesando", "Enviado"

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles;

    @ManyToOne
    @JoinColumn(name = "id_repartidor")
    private Usuario repartidor;


}
