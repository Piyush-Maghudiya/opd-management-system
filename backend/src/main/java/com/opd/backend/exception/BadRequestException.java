package com.opd.backend.exception;

/**
 * Thrown when client sends invalid data or invalid references.
 * Mapped to HTTP 400 BAD_REQUEST.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
