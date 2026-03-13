package com.aris.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double lat;
    private Double lng;

    @Column(name = "total_beds")
    private Integer totalBeds;

    @Column(name = "available_beds")
    private Integer availableBeds;

    private String specialties;

    @Column(nullable = false)
    private String status = "OPEN";

    public Hospital() {}

    public Hospital(String name, Double lat, Double lng, Integer totalBeds, Integer availableBeds, String specialties) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        this.specialties = specialties;
        this.status = "OPEN";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }
    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }
    public String getSpecialties() { return specialties; }
    public void setSpecialties(String specialties) { this.specialties = specialties; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
