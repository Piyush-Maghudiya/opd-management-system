package com.opd.backend.repository;

import com.opd.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * PatientRepository
 * 
 * Provides database access operations for Patient entity using Spring Data JPA.
 * 
 * Annotations used:
 * - @Repository: Indicates that this interface is a Spring-managed repository component
 *   for data persistence operations.
 * 
 * Spring Data JPA automatically generates SQL implementations at runtime based on method names.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * Search patients where the name contains the search term (case-insensitive)
     * OR phone number contains the search term.
     * Explicit JPQL query ensures 100% dialect compatibility across MySQL and H2.
     */
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) OR p.phone LIKE CONCAT('%', :phone, '%')")
    List<Patient> findByNameContainingIgnoreCaseOrPhoneContaining(@org.springframework.data.repository.query.Param("name") String name, 
                                                                 @org.springframework.data.repository.query.Param("phone") String phone);
}
