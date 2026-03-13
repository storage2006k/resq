package com.aris.service;

import com.aris.model.Ambulance;
import com.aris.repository.AmbulanceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AmbulanceService {

    private final AmbulanceRepository ambulanceRepository;

    public AmbulanceService(AmbulanceRepository ambulanceRepository) {
        this.ambulanceRepository = ambulanceRepository;
    }

    public List<Ambulance> getAll() {
        return ambulanceRepository.findAll();
    }

    public Ambulance getById(Long id) {
        return ambulanceRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Ambulance not found: " + id));
    }

    public long countDeployed() {
        return ambulanceRepository.countByStatus("TRANSIT") + ambulanceRepository.countByStatus("ACTIVE");
    }

    public long countTotal() {
        return ambulanceRepository.count();
    }
}
