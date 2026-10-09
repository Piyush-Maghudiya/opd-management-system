package com.opd.backend.service;

import com.opd.backend.entity.Patient;
import com.opd.backend.exception.BadRequestException;
import com.opd.backend.exception.ResourceNotFoundException;
import com.opd.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PatientService
 * 
 * Handles business logic for registering, searching, and retrieving patients.
 * 
 * Annotations used:
 * - @Service: Marks this class as a Spring Service layer bean containing business logic.
 * - @Transactional: Ensures operations run within a database transaction context.
 * 
 * Uses constructor injection for PatientRepository dependency.
 */
@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    // Constructor injection: Spring automatically injects PatientRepository bean
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * Registers a new patient.
     * Validates business constraints before saving.
     */
    public Patient registerPatient(Patient patient) {
        if (patient == null) {
            throw new BadRequestException("Patient data must not be null");
        }
        if (patient.getName() == null || patient.getName().trim().isEmpty()) {
            throw new BadRequestException("Patient name is required");
        }
        if (patient.getGender() == null || patient.getGender().trim().isEmpty()) {
            throw new BadRequestException("Gender is required");
        }
        if (patient.getAge() == null || patient.getAge() < 1 || patient.getAge() > 130) {
            throw new BadRequestException("Valid age between 1 and 130 is required");
        }
        if (patient.getPhone() == null || !patient.getPhone().matches("^[0-9]{10}$")) {
            throw new BadRequestException("Phone number must be exactly 10 digits");
        }

        // Clean up input strings
        patient.setName(patient.getName().trim());
        patient.setGender(patient.getGender().trim());
        patient.setPhone(patient.getPhone().trim());

        return patientRepository.save(patient);
    }

    /**
     * Retrieves all registered patients.
     */
    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    /**
     * Searches patients by name or phone number.
     * If search term is null or empty, returns all patients.
     */
    @Transactional(readOnly = true)
    public List<Patient> searchPatients(String search) {
        if (search == null || search.trim().isEmpty()) {
            return patientRepository.findAll();
        }
        String query = search.trim();
        return patientRepository.findByNameContainingIgnoreCaseOrPhoneContaining(query, query);
    }

    /**
     * Retrieves a patient by their unique ID.
     * Throws ResourceNotFoundException if patient does not exist.
     */
    @Transactional(readOnly = true)
    public Patient getPatientById(Long id) {
        if (id == null) {
            throw new BadRequestException("Patient ID must not be null");
        }
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }
}
