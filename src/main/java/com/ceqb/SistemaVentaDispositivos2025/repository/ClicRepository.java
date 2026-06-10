package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.Clic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ClicRepository extends JpaRepository<Clic, Long> {
    int countByProductoId(Long productoId);
    @Query("SELECT COUNT(c) FROM Clic c WHERE c.producto.id = ?1 AND c.fechaHora >= ?2")
    Integer countByProductoIdAndFechaClicAfter(Long productoId, LocalDateTime fechaDesde);


}
