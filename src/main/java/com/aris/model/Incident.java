package com.aris.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private String patientId;

    @Column(name = "condition_desc")
    private String condition;

    private Double lat;
    private Double lng;

    @Column(nullable = false)
    private String status = "NEW";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Incident() {}

    public Incident(String patientId, String condition, Double lat, Double lng) {
        this.patientId = patientId;
        this.condition = condition;
        this.lat = lat;
        this.lng = lng;
        this.status = "NEW";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
