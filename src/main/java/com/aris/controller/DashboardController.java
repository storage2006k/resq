package com.aris.controller;

import com.aris.dto.DashboardStats;
import com.aris.repository.DispatchRepository;
import com.aris.service.AmbulanceService;
import com.aris.service.HospitalService;
import com.aris.service.IncidentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IncidentService incidentService;
    private final AmbulanceService ambulanceService;
    private final HospitalService hospitalService;
    private final DispatchRepository dispatchRepository;

    public DashboardController(IncidentService incidentService,
                               AmbulanceService ambulanceService,
                               HospitalService hospitalService,
                               DispatchRepository dispatchRepository) {
        this.incidentService = incidentService;
        this.ambulanceService = ambulanceService;
        this.hospitalService = hospitalService;
        this.dispatchRepository = dispatchRepository;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        long activeIncidents = incidentService.countActive();
        long deployedUnits = ambulanceService.countDeployed();
        Double avgEta = dispatchRepository.findAverageEta();
        long availableBeds = hospitalService.totalAvailableBeds();
        long totalAmbulances = ambulanceService.countTotal();
        long totalHospitals = hospitalService.getAll().size();

        DashboardStats stats = new DashboardStats(
                activeIncidents,
                deployedUnits,
                avgEta != null ? avgEta : 0.0,
                availableBeds,
                totalAmbulances,
                totalHospitals
        );

        return ResponseEntity.ok(stats);
    }
}
