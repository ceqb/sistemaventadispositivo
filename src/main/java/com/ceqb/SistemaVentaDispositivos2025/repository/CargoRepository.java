package com.ceqb.SistemaVentaDispositivos2025.repository;

import com.ceqb.SistemaVentaDispositivos2025.model.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CargoRepository extends JpaRepository<Cargo, Integer> {

    Optional<Cargo> findByNombrecargo(String nombrecargo);
}
