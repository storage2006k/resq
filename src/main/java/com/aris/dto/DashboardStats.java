package com.aris.dto;

public class DashboardStats {
    private long activeIncidents;
    private long deployedUnits;
    private double avgEta;
    private long availableBeds;
    private long totalAmbulances;
    private long totalHospitals;

    public DashboardStats() {}

    public DashboardStats(long activeIncidents, long deployedUnits, double avgEta, 
                          long availableBeds, long totalAmbulances, long totalHospitals) {
        this.activeIncidents = activeIncidents;
        this.deployedUnits = deployedUnits;
        this.avgEta = avgEta;
        this.availableBeds = availableBeds;
        this.totalAmbulances = totalAmbulances;
        this.totalHospitals = totalHospitals;
    }

    public long getActiveIncidents() { return activeIncidents; }
    public void setActiveIncidents(long activeIncidents) { this.activeIncidents = activeIncidents; }
    public long getDeployedUnits() { return deployedUnits; }
    public void setDeployedUnits(long deployedUnits) { this.deployedUnits = deployedUnits; }
    public double getAvgEta() { return avgEta; }
    public void setAvgEta(double avgEta) { this.avgEta = avgEta; }
    public long getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(long availableBeds) { this.availableBeds = availableBeds; }
    public long getTotalAmbulances() { return totalAmbulances; }
    public void setTotalAmbulances(long totalAmbulances) { this.totalAmbulances = totalAmbulances; }
    public long getTotalHospitals() { return totalHospitals; }
    public void setTotalHospitals(long totalHospitals) { this.totalHospitals = totalHospitals; }
}
