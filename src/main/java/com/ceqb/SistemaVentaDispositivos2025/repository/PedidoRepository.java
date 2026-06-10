package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.EstadoPedido;
import com.ceqb.SistemaVentaDispositivos2025.model.MetodoPago;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Todos los pedidos de un usuario
    List<Pedido> findByUsuarioOrderByFechaPedidoDesc(Usuario usuario);
    Optional<Pedido> findByNumeroPedido(String numeroPedido);

    // Trae el último pedido de un usuario con un estado de pago específico
    Optional<Pedido> findTopByUsuarioAndEstadoPagoOrderByIdDesc(Usuario usuario, String estadoPago);

    /*OBTENER TODOS LOS PEDIDOS APROBADOS*/
    long countByEstadoPago(String estadoPago);

    // ✅ Método para encontrar pedidos pendientes que son antiguos
    List<Pedido> findByEstadoPagoAndMetodoPagoAndFechaPedidoBefore(String estadoPago, MetodoPago metodoPago, LocalDateTime fecha);


            /*REPARTIDOR*/
    @Query("SELECT p FROM Pedido p " +
            "WHERE p.repartidor IS NULL " +
            "AND ((p.metodoPago = 'MERCADOPAGO' AND p.estadoPago = 'APROBADO') " +
            "  OR (p.metodoPago = 'CONTRAENTREGA' AND p.estadoPago = 'PENDIENTE'))")
    List<Pedido> findPedidosDisponibles();

    @Query("SELECT COUNT(p) FROM Pedido p " +
            "WHERE p.repartidor IS NULL " +
            "AND ((p.metodoPago = 'MERCADOPAGO' AND p.estadoPago = 'APROBADO') " +
            "  OR (p.metodoPago = 'CONTRAENTREGA' AND p.estadoPago = 'PENDIENTE'))")
    long contarPedidosDisponibles();

    //List<Pedido> findByRepartidorIsNullAndEstadoPago(String estadoPago); // pedidos libres (sin repartidor)

    // ✅ Pedidos que ya están asignados a un repartidor específico
    List<Pedido> findByRepartidorId(Long repartidorId);

    Optional<Pedido> findTopByUsuarioOrderByIdDesc(Usuario usuario);
    List<Pedido> findByEstadoPedido(EstadoPedido estadoPedido);
    // ✅ NUEVO: Cuenta los pedidos en un estado específico
    long countByEstadoPedido(EstadoPedido estadoPedido);

    @Query("SELECT p FROM Pedido p " +
            "WHERE p.fechaAsignacion IS NOT NULL " +
            "AND p.fechaEntrega IS NULL " +
            "AND p.fechaAsignacion < :limite")
    List<Pedido> findPedidosRetrasados(@Param("limite") LocalDateTime limite);




    @Query("""
    SELECT p FROM Pedido p
    WHERE p.estadoPedido = :estado
      AND (
           p.fechaProcesando IS NULL
           OR p.fechaProcesando < :limite
      )
""")
    List<Pedido> findPedidosParaConfirmar(
            @Param("estado") EstadoPedido estado,
            @Param("limite") LocalDateTime limite
    );

    @Query("""
    SELECT p FROM Pedido p
    WHERE p.estadoPedido = :estado
      AND p.fechaPedido <= :limite
""")
    List<Pedido> findPedidosParaProcesar(
            @Param("estado") EstadoPedido estado,
            @Param("limite") LocalDateTime limite
    );

    Optional<Pedido> findByQrToken(String qrToken);
}
