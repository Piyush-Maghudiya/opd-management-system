package com.opd.backend.service;

import com.opd.backend.entity.Appointment;
import com.opd.backend.entity.Consultation;
import com.opd.backend.exception.BadRequestException;
import com.opd.backend.exception.ConflictException;
import com.opd.backend.exception.ResourceNotFoundException;
import com.opd.backend.repository.AppointmentRepository;
import com.opd.backend.repository.ConsultationRepository;
import com.opd.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ConsultationService
 * 
 * Handles business logic for creating and completing outpatient consultations,
 * validating vitals (blood pressure & temperature), and preventing duplicate records.
 */
@Service
@Transactional
public class ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;

    public ConsultationService(ConsultationRepository consultationRepository,
                               AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
    }

    /**
     * Saves a consultation summary for an existing appointment.
     * Enforces:
     * 1. Appointment must exist.
     * 2. Only one consultation is allowed per appointment (duplicate check).
     * 3. Both vitals (blood pressure, temperature) and notes are required.
     */
    public Consultation saveConsultation(Consultation consultation) {
        if (consultation == null) {
            throw new BadRequestException("Consultation data must not be null");
        }

        // Determine target appointment ID
        Long targetAppointmentId = consultation.getAppointmentId();
        if (targetAppointmentId == null && consultation.getAppointment() != null) {
            targetAppointmentId = consultation.getAppointment().getId();
        }

        if (targetAppointmentId == null) {
            throw new BadRequestException("A valid appointment ID is required to record a consultation");
        }

        // Verify appointment exists in database
        Long appointmentId = targetAppointmentId;
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cannot record consultation: Appointment not found with id: " + appointmentId));

        // Prevent duplicate consultations for the same appointment
        if (consultationRepository.existsByAppointment_Id(appointmentId)) {
            throw new ConflictException(
                    "A consultation summary already exists for appointment ID: " + appointmentId);
        }

        // Validate vitals and notes
        if (consultation.getBloodPressure() == null || consultation.getBloodPressure().trim().isEmpty()) {
            throw new BadRequestException("Blood pressure reading is required (e.g. '120/80')");
        }

        if (consultation.getTemperature() == null) {
            throw new BadRequestException("Body temperature is required (e.g. 98.6)");
        }
        if (consultation.getTemperature() < 70.0 || consultation.getTemperature() > 115.0) {
            throw new BadRequestException("Temperature must be a valid human body reading between 70°F and 115°F");
        }

        if (consultation.getNotes() == null || consultation.getNotes().trim().isEmpty()) {
            throw new BadRequestException("Consultation notes are required");
        }

        consultation.setAppointment(existingAppointment);
        consultation.setBloodPressure(consultation.getBloodPressure().trim());
        consultation.setNotes(consultation.getNotes().trim());

        if (consultation.getStatus() == null || consultation.getStatus().trim().isEmpty()) {
            consultation.setStatus("IN_PROGRESS");
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Marks an in-progress consultation as COMPLETED.
     * Enforces:
     * 1. Consultation must exist.
     * 2. Cannot complete an already completed consultation (prevents repeated completion).
     * 3. Updates associated appointment status to COMPLETED for consistency.
     */
    public Consultation completeConsultation(Long consultationId) {
        if (consultationId == null) {
            throw new BadRequestException("Consultation ID must not be null");
        }

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Consultation not found with id: " + consultationId));

        if ("COMPLETED".equalsIgnoreCase(consultation.getStatus())) {
            throw new ConflictException(
                    "Consultation ID " + consultationId + " is already marked as COMPLETED and cannot be completed again.");
        }

        // Ensure required vitals exist before completing
        if (consultation.getBloodPressure() == null || consultation.getBloodPressure().trim().isEmpty() ||
            consultation.getTemperature() == null ||
            consultation.getNotes() == null || consultation.getNotes().trim().isEmpty()) {
            throw new BadRequestException("Cannot complete consultation: vitals and notes must be filled first.");
        }

        // Mark consultation as completed
        consultation.setStatus("COMPLETED");

        // Keep associated appointment status synchronized
        Appointment appointment = consultation.getAppointment();
        if (appointment != null) {
            appointment.setStatus("COMPLETED");
            appointmentRepository.save(appointment);
        }

        return consultationRepository.save(consultation);
    }

    /**
     * Retrieves all completed consultations for a specific patient.
     * Shows patient, doctor, appointment date, vitals, notes, and status.
     */
    @Transactional(readOnly = true)
    public List<Consultation> getCompletedConsultationsByPatientId(Long patientId) {
        if (patientId == null) {
            throw new BadRequestException("Patient ID must not be null");
        }

        // Verify that the patient exists in the system
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }

        return consultationRepository.findCompletedByPatientId(patientId);
    }

    /**
     * Retrieves a consultation by its ID.
     */
    @Transactional(readOnly = true)
    public Consultation getConsultationById(Long id) {
        if (id == null) {
            throw new BadRequestException("Consultation ID must not be null");
        }
        return consultationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + id));
    }
}
