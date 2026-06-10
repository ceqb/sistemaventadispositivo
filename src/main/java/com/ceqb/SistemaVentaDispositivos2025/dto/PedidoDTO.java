package com.ceqb.SistemaVentaDispositivos2025.dto;

import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.model.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PedidoDTO implements Serializable {

    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private String direccionEntrega;
    private String numeroPedido;
    private String preferenciaId;
    private boolean reservaLiberada = false;
    private String paymentId;
    private LocalDateTime fechaPedido;
    private BigDecimal total;
    private MetodoPago metodoPago;
    private String estadoPago;
    private EstadoPedido estadoPedido;
    private List<DetallePedidoDTO> detalles; // Lista de los ítems de la orden
    private Long repartidorId;
    private String repartidorNombre;
    private LocalDateTime fechaEntrega;
    private LocalDateTime fechaProcesando;
    private LocalDateTime fechaAsignacion;
    private String mensaje;

    private String qrToken;
    private Boolean qrUsado;
    private LocalDateTime qrUsadoEn;

}
