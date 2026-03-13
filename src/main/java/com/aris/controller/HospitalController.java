package com.aris.controller;

import com.aris.model.Hospital;
import com.aris.service.HospitalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public ResponseEntity<List<Hospital>> getAll() {
        return ResponseEntity.ok(hospitalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getById(@PathVariable Long id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }
}
