-- ==============================================================
-- OPD (Outpatient Department) Management Module - Database Schema
-- MySQL 8.0 Compatible DDL
-- ==============================================================

-- 1. Create Database
CREATE DATABASE IF NOT EXISTS opd_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE opd_db;

-- 2. Patients Table
-- Stores demographic information for outpatients registered at the clinic.
CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(20) NOT NULL,
    age INT NOT NULL,
    phone VARCHAR(15) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_patient_age CHECK (age >= 1 AND age <= 130)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Appointments Table
-- Stores appointment bookings linked to an existing patient.
-- Many appointments can belong to one patient.
CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_name VARCHAR(255) NOT NULL,
    appointment_date_time DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) 
        REFERENCES patients(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX idx_appointment_datetime (appointment_date_time),
    INDEX idx_appointment_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Consultations Table
-- Stores clinical consultation summary recorded by doctor for an appointment.
-- UNIQUE constraint on appointment_id ensures 1-to-1 relationship (one consultation per appointment).
CREATE TABLE IF NOT EXISTS consultations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    appointment_id BIGINT NOT NULL UNIQUE,
    blood_pressure VARCHAR(30) NOT NULL,
    temperature DOUBLE NOT NULL,
    notes TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consultation_appointment FOREIGN KEY (appointment_id) 
        REFERENCES appointments(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT chk_consultation_temp CHECK (temperature >= 70.0 AND temperature <= 115.0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==============================================================
-- Sample Demonstration Seed Data (Optional for testing)
-- ==============================================================

-- Seed Patients
INSERT INTO patients (name, gender, age, phone) VALUES 
('Ramesh Sharma', 'Male', 42, '9876543210'),
('Anita Desai', 'Female', 29, '9123456789'),
('Vikram Mehta', 'Male', 55, '9988776655');

-- Seed Appointments (Current Date & Tomorrow)
INSERT INTO appointments (patient_id, doctor_name, appointment_date_time, status) VALUES 
(1, 'Dr. Arvind Gupta', NOW() + INTERVAL 2 HOUR, 'SCHEDULED'),
(2, 'Dr. Sunita Kapoor', NOW() + INTERVAL 3 HOUR, 'SCHEDULED'),
(3, 'Dr. Arvind Gupta', NOW() + INTERVAL 1 DAY, 'SCHEDULED');

-- Seed Consultation (In Progress)
INSERT INTO consultations (appointment_id, blood_pressure, temperature, notes, status) VALUES 
(1, '120/80', 98.6, 'Routine health review. Vitals stable.', 'IN_PROGRESS');
