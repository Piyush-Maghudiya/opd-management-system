package com.opd.backend.controller;

import com.opd.backend.entity.Patient;
import com.opd.backend.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * PatientController
 * 
 * Exposes REST APIs for Patient Registration and Patient Directory.
 * 
 * Annotations used:
 * - @RestController: Convenience annotation combining @Controller and @ResponseBody,
 *   signaling that methods return domain objects serialized directly into HTTP response bodies (JSON).
 * - @RequestMapping("/api/patients"): Defines the base URI path for all endpoints in this controller.
 * - @PostMapping, @GetMapping: Maps HTTP POST and GET requests to specific handler methods.
 * - @Valid: Triggers Bean Validation on incoming request body according to constraints on Patient entity.
 * - @RequestBody: Deserializes the incoming JSON request payload into a Java Patient object.
 * - @PathVariable: Extracts template variables from the URI path (e.g. {id}).
 * - @RequestParam: Extracts URL query parameters (e.g. ?search=John).
 */
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    // Constructor injection
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    /**
     * POST /api/patients
     * Registers a new outpatient.
     * Returns HTTP 201 Created on success.
     */
    @PostMapping
    public ResponseEntity<Patient> registerPatient(@Valid @RequestBody Patient patient) {
        Patient savedPatient = patientService.registerPatient(patient);
        return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);
    }

    /**
     * GET /api/patients
     * GET /api/patients?search=xyz
     * Retrieves all patients, or filters patients by name or phone number.
     * Returns HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<Patient>> getPatients(
            @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.trim().isEmpty()) {
            return ResponseEntity.ok(patientService.searchPatients(search));
        }
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    /**
     * GET /api/patients/{id}
     * Retrieves a patient by ID.
     * Returns HTTP 200 OK or HTTP 404 NOT_FOUND.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Patient patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }
}
