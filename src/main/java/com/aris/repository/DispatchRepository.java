package com.aris.repository;

import com.aris.model.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {
    List<Dispatch> findByIncidentId(Long incidentId);
    List<Dispatch> findByAmbulanceId(Long ambulanceId);

    @Query("SELECT AVG(d.etaMinutes) FROM Dispatch d WHERE d.etaMinutes IS NOT NULL")
    Double findAverageEta();
}
