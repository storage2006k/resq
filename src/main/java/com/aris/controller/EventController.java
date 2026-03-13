package com.aris.controller;

import com.aris.model.EventLog;
import com.aris.repository.EventLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventLogRepository eventLogRepository;

    public EventController(EventLogRepository eventLogRepository) {
        this.eventLogRepository = eventLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<EventLog>> getRecent() {
        return ResponseEntity.ok(eventLogRepository.findTop50ByOrderByTimestampDesc());
    }
}
