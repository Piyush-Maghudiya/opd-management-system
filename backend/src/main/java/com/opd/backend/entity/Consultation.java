package com.opd.backend.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Consultation Entity
 * 
 * Represents the clinical diagnosis and vitals recorded by the doctor during an appointment.
 * 
 * Relationships:
 * - Each appointment can have at most one consultation.
 * - @OneToOne mapping with unique foreign key constraint (appointment_id) enforces 1-to-1 relationship.
 */
@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @NotBlank(message = "Blood pressure is required")
    @Column(name = "blood_pressure", nullable = false, length = 30)
    private String bloodPressure;

    @NotNull(message = "Temperature is required")
    @DecimalMin(value = "70.0", message = "Temperature must be a valid human body reading (above 70°F)")
    @DecimalMax(value = "115.0", message = "Temperature must be a valid human body reading (below 115°F)")
    @Column(name = "temperature", nullable = false)
    private Double temperature;

    @NotBlank(message = "Consultation notes are required")
    @Column(name = "notes", nullable = false, columnDefinition = "TEXT")
    private String notes;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "IN_PROGRESS";

    // Transient field to allow frontend to pass "appointmentId" directly in JSON
    @Transient
    private Long appointmentId;

    // Default constructor required by JPA
    public Consultation() {
    }

    public Consultation(Appointment appointment, String bloodPressure, Double temperature, String notes, String status) {
        this.appointment = appointment;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.notes = notes;
        this.status = (status != null && !status.isBlank()) ? status : "IN_PROGRESS";
    }

    public Consultation(Long id, Appointment appointment, String bloodPressure, Double temperature, String notes, String status) {
        this.id = id;
        this.appointment = appointment;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
        this.notes = notes;
        this.status = (status != null && !status.isBlank()) ? status : "IN_PROGRESS";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("appointmentId")
    public Long getAppointmentId() {
        if (appointment != null) {
            return appointment.getId();
        }
        return appointmentId;
    }

    @JsonProperty("appointmentId")
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
        if (this.appointment == null && appointmentId != null) {
            Appointment placeholder = new Appointment();
            placeholder.setId(appointmentId);
            this.appointment = placeholder;
        }
    }

    @JsonProperty("patientId")
    public Long getPatientId() {
        if (appointment != null && appointment.getPatient() != null) {
            return appointment.getPatient().getId();
        }
        return null;
    }

    @JsonProperty("patientName")
    public String getPatientName() {
        if (appointment != null && appointment.getPatient() != null) {
            return appointment.getPatient().getName();
        }
        return null;
    }

    @JsonProperty("doctorName")
    public String getDoctorName() {
        if (appointment != null) {
            return appointment.getDoctorName();
        }
        return null;
    }

    @JsonProperty("appointmentDate")
    public String getAppointmentDate() {
        if (appointment != null && appointment.getAppointmentDateTime() != null) {
            return appointment.getAppointmentDateTime().toLocalDate().toString();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "id=" + id +
                ", appointmentId=" + (appointment != null ? appointment.getId() : null) +
                ", bloodPressure='" + bloodPressure + '\'' +
                ", temperature=" + temperature +
                ", notes='" + notes + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
