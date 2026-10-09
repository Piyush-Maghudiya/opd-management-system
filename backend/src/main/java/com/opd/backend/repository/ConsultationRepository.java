package com.opd.backend.repository;

import com.opd.backend.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * ConsultationRepository
 * 
 * Provides database access operations for Consultation entity using Spring Data JPA.
 */
@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    /**
     * Check if a consultation already exists for a specific appointment.
     * Prevents duplicate consultations for the same appointment.
     */
    boolean existsByAppointment_Id(Long appointmentId);

    /**
     * Find consultation associated with a specific appointment.
     */
    Optional<Consultation> findByAppointment_Id(Long appointmentId);

    /**
     * Retrieve all completed consultations for a given patient.
     * Uses explicit JPQL query joining Consultation -> Appointment -> Patient.
     */
    @Query("SELECT c FROM Consultation c " +
           "WHERE c.appointment.patient.id = :patientId AND c.status = 'COMPLETED' " +
           "ORDER BY c.appointment.appointmentDateTime DESC")
    List<Consultation> findCompletedByPatientId(@Param("patientId") Long patientId);
}
