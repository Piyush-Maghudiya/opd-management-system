package com.opd.backend.controller;

import com.opd.backend.entity.Appointment;
import com.opd.backend.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AppointmentController
 * 
 * Exposes REST APIs for Appointment Booking and Daily Scheduling.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * POST /api/appointments
     * Books an appointment for an existing patient.
     * Returns HTTP 201 Created.
     */
    @PostMapping
    public ResponseEntity<Appointment> bookAppointment(@Valid @RequestBody Appointment appointment) {
        Appointment savedAppointment = appointmentService.bookAppointment(appointment);
        return new ResponseEntity<>(savedAppointment, HttpStatus.CREATED);
    }

    /**
     * GET /api/appointments/today
     * Retrieves all appointments scheduled for the current calendar date
     * based on the configured clinic timezone.
     * Returns HTTP 200 OK.
     */
    @GetMapping("/today")
    public ResponseEntity<List<Appointment>> getTodayAppointments() {
        List<Appointment> todayList = appointmentService.getTodayAppointments();
        return ResponseEntity.ok(todayList);
    }

    /**
     * GET /api/appointments/{id}
     * Retrieves an appointment by ID.
     * Returns HTTP 200 OK or HTTP 404 NOT_FOUND.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }
}
