package com.opd.backend.controller;

import com.opd.backend.entity.Consultation;
import com.opd.backend.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ConsultationController
 * 
 * Exposes REST APIs for Recording Consultations, Completing Consultations,
 * and Viewing Completed Consultation History by Patient.
 */
@RestController
@RequestMapping("/api")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /**
     * POST /api/consultations
     * Saves a consultation summary for an existing appointment.
     * Records blood pressure, temperature, and doctor notes.
     * Returns HTTP 201 Created.
     */
    @PostMapping("/consultations")
    public ResponseEntity<Consultation> saveConsultation(@Valid @RequestBody Consultation consultation) {
        Consultation savedConsultation = consultationService.saveConsultation(consultation);
        return new ResponseEntity<>(savedConsultation, HttpStatus.CREATED);
    }

    /**
     * PATCH /api/consultations/{id}/complete
     * Marks an in-progress consultation as COMPLETED.
     * Also updates the associated appointment status to COMPLETED.
     * Returns HTTP 200 OK or 409 Conflict if already completed.
     */
    @PatchMapping("/consultations/{id}/complete")
    public ResponseEntity<Consultation> completeConsultation(@PathVariable Long id) {
        Consultation completedConsultation = consultationService.completeConsultation(id);
        return ResponseEntity.ok(completedConsultation);
    }

    /**
     * GET /api/consultations/{id}
     * Retrieves a single consultation by its ID.
     * Returns HTTP 200 OK or HTTP 404 NOT_FOUND.
     */
    @GetMapping("/consultations/{id}")
    public ResponseEntity<Consultation> getConsultationById(@PathVariable Long id) {
        Consultation consultation = consultationService.getConsultationById(id);
        return ResponseEntity.ok(consultation);
    }

    /**
     * GET /api/patients/{patientId}/consultations
     * Retrieves all completed consultations for a specific patient.
     * Returns HTTP 200 OK with list of completed consultations.
     */
    @GetMapping("/patients/{patientId}/consultations")
    public ResponseEntity<List<Consultation>> getCompletedConsultationsByPatient(
            @PathVariable Long patientId) {
        List<Consultation> history = consultationService.getCompletedConsultationsByPatientId(patientId);
        return ResponseEntity.ok(history);
    }
}
