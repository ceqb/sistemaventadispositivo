package com.auditoria_service.repositorio;

import com.auditoria_service.model.AuditoriaAcceso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaAccesoRepository extends JpaRepository<AuditoriaAcceso, Long> {
}
