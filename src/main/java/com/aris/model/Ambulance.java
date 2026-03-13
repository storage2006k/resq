package com.aris.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_code", unique = true, nullable = false)
    private String unitCode;

    private Double lat;
    private Double lng;

    @Column(nullable = false)
    private String status = "STANDBY";

    @Column(name = "assigned_incident_id")
    private Long assignedIncidentId;

    public Ambulance() {}

    public Ambulance(String unitCode, Double lat, Double lng, String status) {
        this.unitCode = unitCode;
        this.lat = lat;
        this.lng = lng;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getAssignedIncidentId() { return assignedIncidentId; }
    public void setAssignedIncidentId(Long assignedIncidentId) { this.assignedIncidentId = assignedIncidentId; }
}
