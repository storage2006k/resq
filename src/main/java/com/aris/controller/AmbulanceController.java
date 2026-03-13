package com.aris.controller;

import com.aris.model.Ambulance;
import com.aris.service.AmbulanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ambulances")
public class AmbulanceController {

    private final AmbulanceService ambulanceService;

    public AmbulanceController(AmbulanceService ambulanceService) {
        this.ambulanceService = ambulanceService;
    }

    @GetMapping
    public ResponseEntity<List<Ambulance>> getAll() {
        return ResponseEntity.ok(ambulanceService.getAll());
    }
}
