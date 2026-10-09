package com.opd.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opd.backend.entity.Patient;
import com.opd.backend.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();
    }

    @Test
    void testRegisterValidPatient() throws Exception {
        Patient patient = new Patient("Amit Sharma", "Male", 29, "9876543210");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Amit Sharma"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.age").value(29))
                .andExpect(jsonPath("$.phone").value("9876543210"));
    }

    @Test
    void testRejectInvalidPatient_MissingNameAndInvalidAge() throws Exception {
        Patient invalidPatient = new Patient("", "Male", -5, "12345");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPatient)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.age").exists())
                .andExpect(jsonPath("$.validationErrors.phone").exists());
    }

    @Test
    void testListAllPatients() throws Exception {
        patientRepository.save(new Patient("Pooja Patel", "Female", 34, "9123456780"));
        patientRepository.save(new Patient("Rahul Verma", "Male", 45, "9876123450"));

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Pooja Patel"))
                .andExpect(jsonPath("$[1].name").value("Rahul Verma"));
    }

    @Test
    void testSearchPatientByName() throws Exception {
        patientRepository.save(new Patient("Rajesh Kumar", "Male", 40, "9876543211"));
        patientRepository.save(new Patient("Sunita Roy", "Female", 32, "9811122233"));

        mockMvc.perform(get("/api/patients").param("search", "rajesh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Rajesh Kumar"));
    }

    @Test
    void testSearchPatientByPhone() throws Exception {
        patientRepository.save(new Patient("Karan Johar", "Male", 50, "9998887776"));
        patientRepository.save(new Patient("Sara Ali", "Female", 26, "9112233445"));

        mockMvc.perform(get("/api/patients").param("search", "888777"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Karan Johar"));
    }

    @Test
    void testGetPatientById_Found() throws Exception {
        Patient saved = patientRepository.save(new Patient("Vikas Rao", "Male", 38, "9555666777"));

        mockMvc.perform(get("/api/patients/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Vikas Rao"));
    }

    @Test
    void testGetPatientById_NotFound() throws Exception {
        mockMvc.perform(get("/api/patients/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message", containsString("Patient not found")));
    }
}
