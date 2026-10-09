package com.opd.backend.service;

import com.opd.backend.entity.Appointment;
import com.opd.backend.entity.Patient;
import com.opd.backend.exception.BadRequestException;
import com.opd.backend.exception.ResourceNotFoundException;
import com.opd.backend.repository.AppointmentRepository;
import com.opd.backend.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

/**
 * AppointmentService
 * 
 * Handles business logic for booking and querying appointments.
 * Uses constructor injection for dependencies and handles timezone calculations.
 */
@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ZoneId applicationZoneId;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              ZoneId applicationZoneId) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.applicationZoneId = applicationZoneId;
    }

    /**
     * Books an appointment for an existing registered patient.
     * Validates that the patient actually exists in the database.
     */
    public Appointment bookAppointment(Appointment appointment) {
        if (appointment == null) {
            throw new BadRequestException("Appointment data must not be null");
        }

        Long rawPatientId = appointment.getPatientId();
        if (rawPatientId == null && appointment.getPatient() != null) {
            rawPatientId = appointment.getPatient().getId();
        }

        if (rawPatientId == null) {
            throw new BadRequestException("A valid patient ID is required to book an appointment");
        }

        final Long targetPatientId = rawPatientId;
        // Validate that patient exists in MySQL database
        Patient existingPatient = patientRepository.findById(targetPatientId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cannot book appointment: Patient not found with id: " + targetPatientId));

        appointment.setPatient(existingPatient);

        if (appointment.getDoctorName() == null || appointment.getDoctorName().trim().isEmpty()) {
            throw new BadRequestException("Doctor name is required");
        }
        appointment.setDoctorName(appointment.getDoctorName().trim());

        if (appointment.getAppointmentDateTime() == null) {
            throw new BadRequestException("Appointment date and time is required");
        }

        if (appointment.getStatus() == null || appointment.getStatus().trim().isEmpty()) {
            appointment.setStatus("SCHEDULED");
        }

        return appointmentRepository.save(appointment);
    }

    /**
     * Retrieves all appointments scheduled for the current calendar day
     * in the configured application timezone (e.g. Asia/Kolkata).
     */
    @Transactional(readOnly = true)
    public List<Appointment> getTodayAppointments() {
        LocalDate today = LocalDate.now(applicationZoneId);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return appointmentRepository.findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(startOfDay, endOfDay);
    }

    /**
     * Retrieves an appointment by ID.
     */
    @Transactional(readOnly = true)
    public Appointment getAppointmentById(Long id) {
        if (id == null) {
            throw new BadRequestException("Appointment ID must not be null");
        }
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }
}
