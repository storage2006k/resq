package com.aris.repository;

import com.aris.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByStatus(String status);
    List<Incident> findByStatusNot(String status);
    long countByStatus(String status);
}
