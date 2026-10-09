package com.opd.backend.exception;

/**
 * Thrown when an operation violates a uniqueness constraint or business state.
 * E.g., duplicate consultation for the same appointment or completing an already completed consultation.
 * Mapped to HTTP 409 CONFLICT.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
