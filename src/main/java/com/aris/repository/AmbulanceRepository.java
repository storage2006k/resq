package com.aris.repository;

import com.aris.model.Ambulance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AmbulanceRepository extends JpaRepository<Ambulance, Long> {
    List<Ambulance> findByStatus(String status);
    Optional<Ambulance> findByUnitCode(String unitCode);
    long countByStatus(String status);
    boolean existsByUnitCode(String unitCode);
}
