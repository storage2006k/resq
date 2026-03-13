package com.aris.dto;

import java.util.List;

public class RouteResponse {
    private List<double[]> coordinates;
    private Double distanceKm;
    private Double durationMinutes;

    public RouteResponse() {}

    public RouteResponse(List<double[]> coordinates, Double distanceKm, Double durationMinutes) {
        this.coordinates = coordinates;
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
    }

    public List<double[]> getCoordinates() { return coordinates; }
    public void setCoordinates(List<double[]> coordinates) { this.coordinates = coordinates; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public Double getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Double durationMinutes) { this.durationMinutes = durationMinutes; }
}
