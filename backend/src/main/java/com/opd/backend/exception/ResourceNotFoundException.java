package com.opd.backend.exception;

/**
 * Thrown when a requested resource (Patient, Appointment, Consultation) is not found in the database.
 * Mapped to HTTP 404 NOT_FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
