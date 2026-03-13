package com.aris.dto;

import jakarta.validation.constraints.NotNull;

public class DispatchRequest {
    @NotNull
    private Long unitId;
    @NotNull
    private Long hospitalId;
    @NotNull
    private Long incidentId;

    public DispatchRequest() {}

    public Long getUnitId() { return unitId; }
    public void setUnitId(Long unitId) { this.unitId = unitId; }
    public Long getHospitalId() { return hospitalId; }
    public void setHospitalId(Long hospitalId) { this.hospitalId = hospitalId; }
    public Long getIncidentId() { return incidentId; }
    public void setIncidentId(Long incidentId) { this.incidentId = incidentId; }
}
