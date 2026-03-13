package com.aris.repository;

import com.aris.model.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventLogRepository extends JpaRepository<EventLog, Long> {
    List<EventLog> findTop50ByOrderByTimestampDesc();
}
