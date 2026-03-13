package com.aris.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dispatches")
public class Dispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ambulance_id", nullable = false)
    private Long ambulanceId;

    @Column(name = "hospital_id", nullable = false)
    private Long hospitalId;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "eta_minutes")
    private Double etaMinutes;

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt = LocalDateTime.now();

    public Dispatch() {}

    public Dispatch(Long ambulanceId, Long hospitalId, Long incidentId, Double etaMinutes) {
        this.ambulanceId = ambulanceId;
        this.hospitalId = hospitalId;
        this.incidentId = incidentId;
        this.etaMinutes = etaMinutes;
        this.dispatchedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getAmbulanceId() { return ambulanceId; }
    public void setAmbulanceId(Long ambulanceId) { this.ambulanceId = ambulanceId; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }
    public Double getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Double etaMinutes) { this.etaMinutes = etaMinutes; }
    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
}
