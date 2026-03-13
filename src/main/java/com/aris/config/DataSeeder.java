package com.aris.config;

import com.aris.model.*;
import com.aris.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EventLogRepository eventLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      HospitalRepository hospitalRepository,
                      AmbulanceRepository ambulanceRepository,
                      EventLogRepository eventLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.eventLogRepository = eventLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedHospitals();
        seedAmbulances();
        seedInitialEvents();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.COMMAND));
        userRepository.save(new User("dispatcher", passwordEncoder.encode("dispatch123"), Role.DISPATCHER));
        userRepository.save(new User("coordinator", passwordEncoder.encode("coord123"), Role.COORDINATOR));
        userRepository.save(new User("supervisor", passwordEncoder.encode("super123"), Role.SUPERVISOR));

        System.out.println("✅ Seeded 4 users (admin/dispatcher/coordinator/supervisor)");
    }

    private void seedHospitals() {
        if (hospitalRepository.count() > 0) return;

        hospitalRepository.save(new Hospital("AIIMS Delhi", 28.5672, 77.2100, 200, 42, "Trauma,Cardiac,Neuro,Burns"));
        hospitalRepository.save(new Hospital("Safdarjung Hospital", 28.5685, 77.2078, 180, 35, "Trauma,Ortho,General"));
        hospitalRepository.save(new Hospital("Sir Ganga Ram Hospital", 28.6380, 77.1908, 150, 28, "Cardiac,Neuro,Pediatric"));
        hospitalRepository.save(new Hospital("Max Super Speciality Saket", 28.5274, 77.2159, 120, 51, "Cardiac,Oncology,Transplant"));
        hospitalRepository.save(new Hospital("Apollo Hospital", 28.5530, 77.2593, 160, 38, "Trauma,Cardiac,Neuro,Robotic"));
        hospitalRepository.save(new Hospital("Fortis Escorts Heart Institute", 28.5494, 77.2207, 100, 22, "Cardiac,Vascular"));
        hospitalRepository.save(new Hospital("RML Hospital", 28.6252, 77.2021, 170, 45, "General,Trauma,Burns"));

        System.out.println("✅ Seeded 7 Delhi-area hospitals");
    }

    private void seedAmbulances() {
        if (ambulanceRepository.count() > 0) return;

        ambulanceRepository.save(new Ambulance("ARIS-001", 28.6139, 77.2090, "STANDBY"));
        ambulanceRepository.save(new Ambulance("ARIS-002", 28.5535, 77.2588, "STANDBY"));
        ambulanceRepository.save(new Ambulance("ARIS-003", 28.6353, 77.2250, "ACTIVE"));
        ambulanceRepository.save(new Ambulance("ARIS-004", 28.5245, 77.1855, "STANDBY"));
        ambulanceRepository.save(new Ambulance("HELI-001", 28.5800, 77.2100, "STANDBY"));

        System.out.println("✅ Seeded 5 units (4 ambulances + 1 helicopter)");
    }

    private void seedInitialEvents() {
        if (eventLogRepository.count() > 0) return;

        eventLogRepository.save(new EventLog("SYSTEM", "🟢 ARIS v1.0 — System initialized successfully", "INFO"));
        eventLogRepository.save(new EventLog("SYSTEM", "📡 All communication channels operational", "INFO"));
        eventLogRepository.save(new EventLog("AI ROUTER", "🧠 Routing intelligence engine online", "INFO"));
        eventLogRepository.save(new EventLog("SYSTEM", "🏥 7 hospitals connected to network", "INFO"));
        eventLogRepository.save(new EventLog("SYSTEM", "🚑 5 emergency units registered and tracking", "INFO"));

        System.out.println("✅ Seeded initial event logs");
    }
}
