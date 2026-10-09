package com.opd.backend.repository;

import com.opd.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AppointmentRepository
 * 
 * Provides database access operations for Appointment entity using Spring Data JPA.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    /**
     * Retrieve appointments scheduled between start and end date-time, ordered chronologically.
     * Used to query today's appointments for the clinic.
     */
    List<Appointment> findByAppointmentDateTimeBetweenOrderByAppointmentDateTimeAsc(
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    /**
     * Retrieve all appointments for a specific patient.
     */
    List<Appointment> findByPatient_IdOrderByAppointmentDateTimeDesc(Long patientId);
}
