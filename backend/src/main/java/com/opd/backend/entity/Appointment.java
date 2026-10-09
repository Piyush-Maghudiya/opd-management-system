package com.opd.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * Appointment Entity
 * 
 * Represents an outpatient consultation booking for a patient with a doctor.
 * 
 * Annotations used:
 * - @ManyToOne: Specifies a Many-to-One relationship where many appointments can belong to one patient.
 * - @JoinColumn: Specifies the foreign key column (patient_id) referencing the patients table.
 * - @JsonFormat: Ensures LocalDateTime is consistently serialized to ISO format.
 */
@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Doctor name is required")
    @Column(name = "doctor_name", nullable = false)
    private String doctorName;

    @NotNull(message = "Appointment date and time is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "SCHEDULED";

    // Transient field to allow frontend to pass "patientId" directly in JSON
    @Transient
    private Long patientId;

    @Transient
    private String appointmentDate;

    @Transient
    private String appointmentTime;

    // Default constructor required by JPA
    public Appointment() {
    }

    public Appointment(Patient patient, String doctorName, LocalDateTime appointmentDateTime, String status) {
        this.patient = patient;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = (status != null && !status.isBlank()) ? status : "SCHEDULED";
    }

    public Appointment(Long id, Patient patient, String doctorName, LocalDateTime appointmentDateTime, String status) {
        this.id = id;
        this.patient = patient;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = (status != null && !status.isBlank()) ? status : "SCHEDULED";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }

    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("patientId")
    public Long getPatientId() {
        if (patient != null) {
            return patient.getId();
        }
        return patientId;
    }

    @JsonProperty("patientId")
    public void setPatientId(Long patientId) {
        this.patientId = patientId;
        if (this.patient == null && patientId != null) {
            Patient placeholder = new Patient();
            placeholder.setId(patientId);
            this.patient = placeholder;
        }
    }

    @JsonProperty("appointmentDate")
    public String getAppointmentDate() {
        if (appointmentDateTime != null) {
            return appointmentDateTime.toLocalDate().toString();
        }
        return appointmentDate;
    }

    @JsonProperty("appointmentDate")
    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
        syncAppointmentDateTime();
    }

    @JsonProperty("appointmentTime")
    public String getAppointmentTime() {
        if (appointmentDateTime != null) {
            String timeStr = appointmentDateTime.toLocalTime().toString();
            if (timeStr.length() > 8) {
                timeStr = timeStr.substring(0, 8);
            }
            return timeStr;
        }
        return appointmentTime;
    }

    @JsonProperty("appointmentTime")
    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
        syncAppointmentDateTime();
    }

    @JsonProperty("patientName")
    public String getPatientName() {
        if (patient != null) {
            return patient.getName();
        }
        return null;
    }

    private void syncAppointmentDateTime() {
        if (this.appointmentDateTime == null && this.appointmentDate != null && !this.appointmentDate.isBlank()
                && this.appointmentTime != null && !this.appointmentTime.isBlank()) {
            try {
                String timePart = this.appointmentTime.trim();
                if (timePart.length() == 5) {
                    timePart = timePart + ":00";
                }
                this.appointmentDateTime = LocalDateTime.parse(this.appointmentDate.trim() + "T" + timePart);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patient=" + (patient != null ? patient.getName() : null) +
                ", doctorName='" + doctorName + '\'' +
                ", appointmentDateTime=" + appointmentDateTime +
                ", status='" + status + '\'' +
                '}';
    }
}
