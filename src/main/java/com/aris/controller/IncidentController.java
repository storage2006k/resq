package com.aris.controller;

import com.aris.dto.IncidentRequest;
import com.aris.model.Incident;
import com.aris.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    public ResponseEntity<List<Incident>> getActive() {
        return ResponseEntity.ok(incidentService.getAllActive());
    }

    @PostMapping
    public ResponseEntity<Incident> create(@Valid @RequestBody IncidentRequest request) {
        return ResponseEntity.ok(incidentService.create(request));
    }
}
