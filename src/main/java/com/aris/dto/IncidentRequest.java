package com.aris.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class IncidentRequest {
    private String patientId;
    @NotBlank
    private String condition;
    @NotNull
    private Double lat;
    @NotNull
    private Double lng;

    public IncidentRequest() {}

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
}
