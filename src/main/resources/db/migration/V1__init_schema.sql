-- ═══════════════════════════════════════════════════
-- RESQ — Database Schema Migration V1
-- Flyway Migration: V1__init_schema.sql
-- ═══════════════════════════════════════════════════

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL DEFAULT 'DISPATCHER',
    last_login      TIMESTAMP
);

-- Hospitals table
CREATE TABLE IF NOT EXISTS hospitals (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    lat             DOUBLE PRECISION,
    lng             DOUBLE PRECISION,
    total_beds      INTEGER DEFAULT 0,
    available_beds  INTEGER DEFAULT 0,
    specialties     TEXT,
    status          VARCHAR(50) NOT NULL DEFAULT 'OPEN'
);

-- Ambulances table
CREATE TABLE IF NOT EXISTS ambulances (
    id                   BIGSERIAL PRIMARY KEY,
    unit_code            VARCHAR(50) UNIQUE NOT NULL,
    lat                  DOUBLE PRECISION,
    lng                  DOUBLE PRECISION,
    status               VARCHAR(50) NOT NULL DEFAULT 'STANDBY',
    assigned_incident_id BIGINT
);

-- Incidents table
CREATE TABLE IF NOT EXISTS incidents (
    id              BIGSERIAL PRIMARY KEY,
    patient_id      VARCHAR(100),
    condition_desc  TEXT,
    lat             DOUBLE PRECISION,
    lng             DOUBLE PRECISION,
    status          VARCHAR(50) NOT NULL DEFAULT 'NEW',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dispatches table
CREATE TABLE IF NOT EXISTS dispatches (
    id              BIGSERIAL PRIMARY KEY,
    ambulance_id    BIGINT NOT NULL REFERENCES ambulances(id),
    hospital_id     BIGINT NOT NULL REFERENCES hospitals(id),
    incident_id     BIGINT NOT NULL REFERENCES incidents(id),
    eta_minutes     DOUBLE PRECISION,
    dispatched_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Event Logs table
CREATE TABLE IF NOT EXISTS event_logs (
    id              BIGSERIAL PRIMARY KEY,
    source          VARCHAR(100) NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    type            VARCHAR(50) NOT NULL,
    timestamp       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ═══ Indexes ═══
CREATE INDEX IF NOT EXISTS idx_incidents_status ON incidents(status);
CREATE INDEX IF NOT EXISTS idx_ambulances_status ON ambulances(status);
CREATE INDEX IF NOT EXISTS idx_dispatches_ambulance ON dispatches(ambulance_id);
CREATE INDEX IF NOT EXISTS idx_dispatches_incident ON dispatches(incident_id);
CREATE INDEX IF NOT EXISTS idx_event_logs_timestamp ON event_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_hospitals_status ON hospitals(status);

-- ═══ Seed Data ═══

-- Default users (passwords: admin123, dispatch123, coord123, super123 — BCrypt)
INSERT INTO users (username, password_hash, role) VALUES
    ('admin',       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COMMAND'),
    ('dispatcher',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DISPATCHER'),
    ('coordinator', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'COORDINATOR'),
    ('supervisor',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'SUPERVISOR')
ON CONFLICT (username) DO NOTHING;

-- Delhi NCR Hospitals
INSERT INTO hospitals (name, lat, lng, total_beds, available_beds, specialties, status) VALUES
    ('AIIMS Delhi',                 28.5672, 77.2100, 120, 35, 'Trauma,Cardiology,Neurology,Burns',  'OPEN'),
    ('Safdarjung Hospital',         28.5690, 77.2070, 100, 28, 'Trauma,Orthopedics,Emergency',       'OPEN'),
    ('Sir Ganga Ram Hospital',      28.6358, 77.1903, 80,  22, 'Cardiology,Neurology,Pediatrics',    'OPEN'),
    ('Max Super Speciality Saket',  28.5275, 77.2157, 90,  18, 'Cardiology,Oncology,Neurology',      'OPEN'),
    ('Fortis Escorts Heart Institute', 28.5505, 77.2225, 70, 15, 'Cardiology,Cardiac Surgery',       'OPEN'),
    ('Apollo Hospital Mathura Road', 28.5395, 77.2835, 85,  20, 'Multi-Specialty,Trauma,Burns',      'OPEN'),
    ('Ram Manohar Lohia Hospital',  28.6274, 77.2080, 95,  25, 'Emergency,Trauma,General Surgery',   'OPEN')
ON CONFLICT DO NOTHING;

-- Delhi NCR Ambulances
INSERT INTO ambulances (unit_code, lat, lng, status) VALUES
    ('RESQ-001', 28.6100, 77.2300, 'STANDBY'),
    ('RESQ-002', 28.5500, 77.1900, 'STANDBY'),
    ('RESQ-003', 28.5800, 77.2500, 'STANDBY'),
    ('RESQ-004', 28.5200, 77.2100, 'STANDBY'),
    ('HELI-001', 28.5560, 77.1000, 'STANDBY')
ON CONFLICT (unit_code) DO NOTHING;

-- Initial event log
INSERT INTO event_logs (source, message, type) VALUES
    ('SYSTEM', '🚀 RESQ system initialized — all units operational', 'INFO')
ON CONFLICT DO NOTHING;
