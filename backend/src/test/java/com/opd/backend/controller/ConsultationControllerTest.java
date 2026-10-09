package com.opd.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opd.backend.entity.Appointment;
import com.opd.backend.entity.Consultation;
import com.opd.backend.entity.Patient;
import com.opd.backend.repository.AppointmentRepository;
import com.opd.backend.repository.ConsultationRepository;
import com.opd.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ConsultationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ConsultationRepository consultationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Patient patient1;
    private Patient patient2;
    private Appointment appointment1;
    private Appointment appointment2;

    @BeforeEach
    void setUp() {
        consultationRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();

        patient1 = patientRepository.save(new Patient("Rohan Das", "Male", 31, "9876540001"));
        patient2 = patientRepository.save(new Patient("Meera Sen", "Female", 28, "9876540002"));

        appointment1 = appointmentRepository.save(
                new Appointment(patient1, "Dr. Kapoor", LocalDateTime.now().plusHours(1), "SCHEDULED"));
        appointment2 = appointmentRepository.save(
                new Appointment(patient2, "Dr. Kapoor", LocalDateTime.now().plusHours(2), "SCHEDULED"));
    }

    @Test
    void testSaveConsultation_Success() throws Exception {
        Consultation consultation = new Consultation();
        consultation.setAppointmentId(appointment1.getId());
        consultation.setBloodPressure("120/80");
        consultation.setTemperature(98.6);
        consultation.setNotes("Patient has mild headache. Prescribed rest.");

        mockMvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultation)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.bloodPressure").value("120/80"))
                .andExpect(jsonPath("$.temperature").value(98.6))
                .andExpect(jsonPath("$.notes").value("Patient has mild headache. Prescribed rest."))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.appointment.id").value(appointment1.getId()));
    }

    @Test
    void testSaveConsultation_InvalidAppointmentReference() throws Exception {
        Consultation consultation = new Consultation();
        consultation.setAppointmentId(99999L);
        consultation.setBloodPressure("120/80");
        consultation.setTemperature(98.6);
        consultation.setNotes("Notes");

        mockMvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(consultation)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Appointment not found")));
    }

    @Test
    void testPreventDuplicateConsultationForSameAppointment() throws Exception {
        // Save first consultation
        Consultation first = new Consultation(appointment1, "120/80", 98.4, "Initial checkup", "IN_PROGRESS");
        consultationRepository.save(first);

        // Try to save second consultation for same appointment1
        Consultation duplicate = new Consultation();
        duplicate.setAppointmentId(appointment1.getId());
        duplicate.setBloodPressure("130/85");
        duplicate.setTemperature(99.1);
        duplicate.setNotes("Second checkup attempt");

        mockMvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    void testCompleteConsultation_Success() throws Exception {
        Consultation saved = consultationRepository.save(
                new Consultation(appointment1, "118/78", 98.2, "Healthy vitals", "IN_PROGRESS"));

        mockMvc.perform(patch("/api/consultations/" + saved.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Verify associated appointment is also marked COMPLETED
        Appointment updatedApt = appointmentRepository.findById(appointment1.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("COMPLETED", updatedApt.getStatus());
    }

    @Test
    void testCompleteConsultation_PreventRepeatedCompletion() throws Exception {
        Consultation saved = consultationRepository.save(
                new Consultation(appointment1, "118/78", 98.2, "Healthy vitals", "COMPLETED"));

        // Attempting to complete already completed consultation should yield 409 Conflict
        mockMvc.perform(patch("/api/consultations/" + saved.getId() + "/complete"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already marked as COMPLETED")));
    }

    @Test
    void testGetCompletedConsultationsByPatientId() throws Exception {
        // Consultation 1 for Patient 1 - COMPLETED
        consultationRepository.save(
                new Consultation(appointment1, "120/80", 98.6, "Completed checkup for Patient 1", "COMPLETED"));

        // Consultation 2 for Patient 2 - COMPLETED
        consultationRepository.save(
                new Consultation(appointment2, "125/82", 99.0, "Completed checkup for Patient 2", "COMPLETED"));

        // Query Patient 1's history
        mockMvc.perform(get("/api/patients/" + patient1.getId() + "/consultations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bloodPressure").value("120/80"))
                .andExpect(jsonPath("$[0].appointment.patient.id").value(patient1.getId()))
                .andExpect(jsonPath("$[0].appointment.patient.name").value("Rohan Das"))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));

        // Query Patient 2's history
        mockMvc.perform(get("/api/patients/" + patient2.getId() + "/consultations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].bloodPressure").value("125/82"))
                .andExpect(jsonPath("$[0].appointment.patient.id").value(patient2.getId()))
                .andExpect(jsonPath("$[0].appointment.patient.name").value("Meera Sen"));
    }
}
