package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.DetallePedido;
import com.ceqb.SistemaVentaDispositivos2025.model.Pedido;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("SELECT COALESCE(SUM(d.cantidad), 0) " +
            "FROM DetallePedido d " +
            "WHERE d.producto.id = :productoId " +
            "AND d.pedido.estadoPago = 'APROBADO' " +
            "AND d.pedido.fechaPedido >= :fechaInicio")
    Integer ventasRecientes(@Param("productoId") Long productoId,
                            @Param("fechaInicio") LocalDateTime fechaInicio);


    @Query("SELECT d.producto, SUM(d.cantidad) as totalVentas " +
            "FROM DetallePedido d " +
            "WHERE d.pedido.fechaPedido >= :fechaInicio " +
            "GROUP BY d.producto " +
            "ORDER BY totalVentas DESC " +
            "LIMIT 10")
    List<Object[]> findTop10BySalesAndDate(@Param("fechaInicio") LocalDateTime fechaInicio);

    @Modifying
    @Transactional
    void deleteByPedido(Pedido pedido);

    /*cliente con más compras aprobadas*/
    @Query("SELECT d.pedido.usuario, COUNT(d) AS totalCompras " +
            "FROM DetallePedido d " +
            "WHERE d.pedido.estadoPago = 'APROBADO' " +
            "GROUP BY d.pedido.usuario " +
            "ORDER BY COUNT(d) DESC")
    List<Object[]> findClienteConMasComprasAprobadas();

    List<DetallePedido> findByPedido(Pedido pedido);

    @Query("""
    SELECT dp.producto.id AS productoId, COUNT(dp.id) AS ventas
    FROM DetallePedido dp
    WHERE dp.pedido.fechaPedido >= :fechaInicio
    GROUP BY dp.producto.id
""")
    List<Object[]> obtenerVentasAgrupadas(LocalDateTime fechaInicio);
}
