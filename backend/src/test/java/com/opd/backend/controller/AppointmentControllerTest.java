package com.opd.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opd.backend.entity.Appointment;
import com.opd.backend.entity.Patient;
import com.opd.backend.repository.AppointmentRepository;
import com.opd.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ZoneId applicationZoneId;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
        testPatient = patientRepository.save(new Patient("Sneha Reddy", "Female", 27, "9876501234"));
    }

    @Test
    void testBookAppointment_Valid() throws Exception {
        LocalDateTime aptTime = LocalDateTime.now(applicationZoneId).plusHours(2).withNano(0);
        Appointment apt = new Appointment();
        apt.setPatientId(testPatient.getId());
        apt.setDoctorName("Dr. Arvind Gupta");
        apt.setAppointmentDateTime(aptTime);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.doctorName").value("Dr. Arvind Gupta"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.patient.id").value(testPatient.getId()))
                .andExpect(jsonPath("$.patient.name").value("Sneha Reddy"));
    }

    @Test
    void testBookAppointment_InvalidPatientReference() throws Exception {
        LocalDateTime aptTime = LocalDateTime.now(applicationZoneId).plusHours(2).withNano(0);
        Appointment apt = new Appointment();
        apt.setPatientId(99999L); // non-existent patient
        apt.setDoctorName("Dr. Arvind Gupta");
        apt.setAppointmentDateTime(aptTime);

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apt)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Patient not found")));
    }

    @Test
    void testBookAppointment_MissingDoctorName() throws Exception {
        Appointment apt = new Appointment();
        apt.setPatientId(testPatient.getId());
        apt.setDoctorName(""); // blank doctor name
        apt.setAppointmentDateTime(LocalDateTime.now(applicationZoneId).plusHours(2));

        mockMvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(apt)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.doctorName").exists());
    }

    @Test
    void testGetTodayAppointments_FiltersCorrectly() throws Exception {
        LocalDate today = LocalDate.now(applicationZoneId);

        // Appointment 1: Today at 10:00 AM
        Appointment aptToday1 = new Appointment(testPatient, "Dr. Mehta", today.atTime(10, 0), "SCHEDULED");
        appointmentRepository.save(aptToday1);

        // Appointment 2: Today at 3:30 PM
        Appointment aptToday2 = new Appointment(testPatient, "Dr. Mehta", today.atTime(15, 30), "SCHEDULED");
        appointmentRepository.save(aptToday2);

        // Appointment 3: Yesterday (should NOT appear in today's list)
        Appointment aptYesterday = new Appointment(testPatient, "Dr. Mehta", today.minusDays(1).atTime(11, 0), "SCHEDULED");
        appointmentRepository.save(aptYesterday);

        // Appointment 4: Tomorrow (should NOT appear in today's list)
        Appointment aptTomorrow = new Appointment(testPatient, "Dr. Mehta", today.plusDays(1).atTime(11, 0), "SCHEDULED");
        appointmentRepository.save(aptTomorrow);

        mockMvc.perform(get("/api/appointments/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Mehta"))
                .andExpect(jsonPath("$[1].doctorName").value("Dr. Mehta"));
    }

    @Test
    void testGetAppointmentById_Found() throws Exception {
        Appointment saved = appointmentRepository.save(
                new Appointment(testPatient, "Dr. Roy", LocalDateTime.now(applicationZoneId).plusHours(1), "SCHEDULED"));

        mockMvc.perform(get("/api/appointments/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.doctorName").value("Dr. Roy"));
    }

    @Test
    void testGetAppointmentById_NotFound() throws Exception {
        mockMvc.perform(get("/api/appointments/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
