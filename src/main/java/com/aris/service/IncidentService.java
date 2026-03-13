package com.aris.service;

import com.aris.dto.IncidentRequest;
import com.aris.model.Incident;
import com.aris.repository.IncidentRepository;
import com.aris.websocket.EventBroadcaster;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final EventBroadcaster eventBroadcaster;

    public IncidentService(IncidentRepository incidentRepository, EventBroadcaster eventBroadcaster) {
        this.incidentRepository = incidentRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    public List<Incident> getAllActive() {
        // Only show incidents waiting for pickup (red dots).
        // IN_TRANSPORT = patient picked up → remove red marker from map.
        List<Incident> active = new java.util.ArrayList<>();
        active.addAll(incidentRepository.findByStatus("NEW"));
        active.addAll(incidentRepository.findByStatus("ACTIVE"));
        return active;
    }

    public List<Incident> getAll() {
        return incidentRepository.findAll();
    }

    public Incident create(IncidentRequest request) {
        Incident incident = new Incident(
                request.getPatientId(),
                request.getCondition(),
                request.getLat(),
                request.getLng()
        );
        incident = incidentRepository.save(incident);

        eventBroadcaster.broadcast("SYSTEM",
                String.format("🚨 NEW INCIDENT #%d — %s at [%.4f, %.4f]",
                        incident.getId(), incident.getCondition(), incident.getLat(), incident.getLng()),
                "ALERT");

        return incident;
    }

    public long countActive() {
        return incidentRepository.countByStatus("NEW")
                + incidentRepository.countByStatus("ACTIVE")
                + incidentRepository.countByStatus("IN_TRANSPORT");
    }
}
