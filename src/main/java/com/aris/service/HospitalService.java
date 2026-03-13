package com.aris.service;

import com.aris.model.Hospital;
import com.aris.repository.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public List<Hospital> getAll() {
        return hospitalRepository.findAll();
    }

    public Hospital getById(Long id) {
        return hospitalRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Hospital not found: " + id));
    }

    public long totalAvailableBeds() {
        return hospitalRepository.findAll().stream()
                .mapToInt(Hospital::getAvailableBeds)
                .sum();
    }
}
