package com.aris.service;

import com.aris.dto.DispatchRequest;
import com.aris.model.Ambulance;
import com.aris.model.Dispatch;
import com.aris.model.Hospital;
import com.aris.model.Incident;
import com.aris.repository.AmbulanceRepository;
import com.aris.repository.DispatchRepository;
import com.aris.repository.HospitalRepository;
import com.aris.repository.IncidentRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final HospitalRepository hospitalRepository;
    private final IncidentRepository incidentRepository;
    private final RoutingService routingService;
    private final EventBroadcaster eventBroadcaster;

    public DispatchService(DispatchRepository dispatchRepository,
                           AmbulanceRepository ambulanceRepository,
                           HospitalRepository hospitalRepository,
                           IncidentRepository incidentRepository,
                           RoutingService routingService,
                           EventBroadcaster eventBroadcaster) {
        this.dispatchRepository = dispatchRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.hospitalRepository = hospitalRepository;
        this.incidentRepository = incidentRepository;
        this.routingService = routingService;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Transactional
    public Dispatch dispatch(DispatchRequest request) {
        Ambulance ambulance = ambulanceRepository.findById(request.getUnitId())
                .orElseThrow(() -> new RuntimeException("Ambulance not found"));

        // Guard: only STANDBY ambulances can be dispatched
        if (!"STANDBY".equals(ambulance.getStatus())) {
            throw new RuntimeException("Unit " + ambulance.getUnitCode() + " is already on a mission (" + ambulance.getStatus() + "). Cannot re-dispatch.");
        }

        Hospital hospital = hospitalRepository.findById(request.getHospitalId())
                .orElseThrow(() -> new RuntimeException("Hospital not found"));
        Incident incident = incidentRepository.findById(request.getIncidentId())
                .orElseThrow(() -> new RuntimeException("Incident not found"));

        // Calculate ETA
        var route = routingService.calculateRoute(
                ambulance.getLat(), ambulance.getLng(),
                hospital.getLat(), hospital.getLng());

        // Update ambulance status
        ambulance.setStatus("TRANSIT");
        ambulance.setAssignedIncidentId(incident.getId());
        ambulanceRepository.save(ambulance);

        // Update incident status
        incident.setStatus("ACTIVE");
        incidentRepository.save(incident);

        // Decrement hospital bed count
        if (hospital.getAvailableBeds() > 0) {
            hospital.setAvailableBeds(hospital.getAvailableBeds() - 1);
            hospitalRepository.save(hospital);
        }

        // Create dispatch record
        Dispatch dispatch = new Dispatch(
                ambulance.getId(),
                hospital.getId(),
                incident.getId(),
                route.getDurationMinutes()
        );
        dispatch = dispatchRepository.save(dispatch);

        // Broadcast event
        eventBroadcaster.broadcast("NPC DISPATCH",
                String.format("🚑 Unit %s dispatched to %s for Incident #%d — ETA: %.1f min",
                        ambulance.getUnitCode(), hospital.getName(),
                        incident.getId(), route.getDurationMinutes()),
                "DISPATCH");

        return dispatch;
    }
}
