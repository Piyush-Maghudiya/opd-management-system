package com.opd.backend;

import com.opd.backend.controller.AppointmentController;
import com.opd.backend.controller.ConsultationController;
import com.opd.backend.controller.PatientController;
import com.opd.backend.entity.Appointment;
import com.opd.backend.entity.Consultation;
import com.opd.backend.entity.Patient;
import com.opd.backend.exception.BadRequestException;
import com.opd.backend.exception.ConflictException;
import com.opd.backend.exception.ResourceNotFoundException;
import com.opd.backend.repository.AppointmentRepository;
import com.opd.backend.repository.ConsultationRepository;
import com.opd.backend.repository.PatientRepository;
import com.opd.backend.service.AppointmentService;
import com.opd.backend.service.ConsultationService;
import com.opd.backend.service.PatientService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Automated Verification Suite for OPD Management Module.
 * Executes all required functional and business rule tests directly against the Spring Boot context.
 */
public class StandaloneVerificationRunner {

    private static int passedTests = 0;
    private static int totalTests = 0;

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
            throw new AssertionError("Assertion failed for: " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("    OPD MANAGEMENT MODULE - FULL SUITE VERIFICATION RUNNER     ");
        System.out.println("===============================================================");

        ConfigurableApplicationContext context = SpringApplication.run(
                OpdBackendApplication.class,
                "--spring.config.additional-location=classpath:application-test.properties",
                "--server.port=0"
        );

        try {
            PatientController patientController = context.getBean(PatientController.class);
            AppointmentController appointmentController = context.getBean(AppointmentController.class);
            ConsultationController consultationController = context.getBean(ConsultationController.class);

            PatientService patientService = context.getBean(PatientService.class);
            AppointmentService appointmentService = context.getBean(AppointmentService.class);
            ConsultationService consultationService = context.getBean(ConsultationService.class);

            PatientRepository patientRepository = context.getBean(PatientRepository.class);
            AppointmentRepository appointmentRepository = context.getBean(AppointmentRepository.class);
            ConsultationRepository consultationRepository = context.getBean(ConsultationRepository.class);
            ZoneId zoneId = context.getBean(ZoneId.class);

            // Cleanup database
            consultationRepository.deleteAll();
            appointmentRepository.deleteAll();
            patientRepository.deleteAll();

            // ==============================================================
            // MODULE 1: PATIENT TESTS
            // ==============================================================
            System.out.println("\n--- MODULE 1: PATIENT REGISTRATION & SEARCH TESTS ---");

            // Test 1: Register valid patient 1
            Patient p1 = new Patient("Ramesh Sharma", "Male", 42, "9876543210");
            ResponseEntity<Patient> respP1 = patientController.registerPatient(p1);
            assertTrue(respP1.getStatusCode() == HttpStatus.CREATED, "Register patient 1 returns HTTP 201 CREATED");
            assertTrue(respP1.getBody() != null && respP1.getBody().getId() != null, "Patient 1 receives auto-generated ID");
            Patient savedP1 = respP1.getBody();

            // Test 2: Register valid patient 2
            Patient p2 = new Patient("Anita Desai", "Female", 29, "9123456789");
            ResponseEntity<Patient> respP2 = patientController.registerPatient(p2);
            assertTrue(respP2.getStatusCode() == HttpStatus.CREATED, "Register patient 2 returns HTTP 201 CREATED");
            Patient savedP2 = respP2.getBody();

            // Test 3: Reject invalid patient - Blank name
            try {
                patientService.registerPatient(new Patient("", "Female", 25, "9998887776"));
                assertTrue(false, "Should reject patient with empty name");
            } catch (BadRequestException e) {
                assertTrue(e.getMessage().contains("name"), "Reject patient with blank name");
            }

            // Test 4: Reject invalid patient - Invalid age
            try {
                patientService.registerPatient(new Patient("Invalid Age", "Male", -5, "9998887776"));
                assertTrue(false, "Should reject patient with negative age");
            } catch (BadRequestException e) {
                assertTrue(e.getMessage().contains("age"), "Reject patient with invalid age");
            }

            // Test 5: Reject invalid patient - Invalid phone number format
            try {
                patientService.registerPatient(new Patient("Invalid Phone", "Male", 30, "123"));
                assertTrue(false, "Should reject patient with invalid phone");
            } catch (BadRequestException e) {
                assertTrue(e.getMessage().contains("10 digits"), "Reject patient with invalid phone format");
            }

            // Test 6: List all registered patients
            ResponseEntity<List<Patient>> allResp = patientController.getPatients(null);
            assertTrue(allResp.getBody() != null && allResp.getBody().size() == 2, "List all patients returns 2 registered patients");

            // Test 7: Search patient by name
            ResponseEntity<List<Patient>> searchNameResp = patientController.getPatients("Ramesh");
            assertTrue(searchNameResp.getBody() != null && searchNameResp.getBody().size() == 1, "Search by name 'Ramesh' finds 1 patient");
            assertTrue(searchNameResp.getBody().get(0).getName().equals("Ramesh Sharma"), "Search by name matches correct patient");

            // Test 8: Search patient by phone
            ResponseEntity<List<Patient>> searchPhoneResp = patientController.getPatients("9123456789");
            assertTrue(searchPhoneResp.getBody() != null && searchPhoneResp.getBody().size() == 1, "Search by phone '9123456789' finds 1 patient");
            assertTrue(searchPhoneResp.getBody().get(0).getName().equals("Anita Desai"), "Search by phone matches correct patient");

            // Test 9: Get patient by valid ID
            ResponseEntity<Patient> byIdResp = patientController.getPatientById(savedP1.getId());
            assertTrue(byIdResp.getStatusCode() == HttpStatus.OK, "Get patient by ID returns HTTP 200 OK");
            assertTrue(byIdResp.getBody().getName().equals("Ramesh Sharma"), "Get patient by ID returns correct patient data");

            // Test 10: Get patient by non-existent ID
            try {
                patientService.getPatientById(99999L);
                assertTrue(false, "Should throw ResourceNotFoundException for missing patient");
            } catch (ResourceNotFoundException e) {
                assertTrue(e.getMessage().contains("not found"), "Handles non-existent patient ID with ResourceNotFoundException");
            }

            // ==============================================================
            // MODULE 2: APPOINTMENT TESTS
            // ==============================================================
            System.out.println("\n--- MODULE 2: APPOINTMENT BOOKING & TODAY SCHEDULE TESTS ---");

            // Test 11: Book appointment for patient 1
            LocalDateTime todayNow = LocalDateTime.now(zoneId);
            Appointment apt1 = new Appointment();
            apt1.setPatientId(savedP1.getId());
            apt1.setDoctorName("Dr. Arvind Gupta");
            apt1.setAppointmentDateTime(todayNow.plusHours(2));
            ResponseEntity<Appointment> respApt1 = appointmentController.bookAppointment(apt1);
            assertTrue(respApt1.getStatusCode() == HttpStatus.CREATED, "Book appointment 1 returns HTTP 201 CREATED");
            Appointment savedApt1 = respApt1.getBody();
            assertTrue(savedApt1.getStatus().equals("SCHEDULED"), "Appointment status defaults to SCHEDULED");
            assertTrue(savedApt1.getPatient().getId().equals(savedP1.getId()), "Appointment linked to correct Patient 1");

            // Test 12: Book appointment for patient 2
            Appointment apt2 = new Appointment();
            apt2.setPatientId(savedP2.getId());
            apt2.setDoctorName("Dr. Arvind Gupta");
            apt2.setAppointmentDateTime(todayNow.plusHours(3));
            ResponseEntity<Appointment> respApt2 = appointmentController.bookAppointment(apt2);
            assertTrue(respApt2.getStatusCode() == HttpStatus.CREATED, "Book appointment 2 returns HTTP 201 CREATED");
            Appointment savedApt2 = respApt2.getBody();

            // Test 13: Reject appointment with non-existent patient
            Appointment badPatientApt = new Appointment();
            badPatientApt.setPatientId(99999L);
            badPatientApt.setDoctorName("Dr. Sharma");
            badPatientApt.setAppointmentDateTime(todayNow.plusHours(1));
            try {
                appointmentService.bookAppointment(badPatientApt);
                assertTrue(false, "Should reject booking for non-existent patient");
            } catch (ResourceNotFoundException e) {
                assertTrue(e.getMessage().contains("Patient not found"), "Reject appointment for non-existent patient");
            }

            // Test 14: Reject appointment with missing doctor name
            Appointment badDoctorApt = new Appointment();
            badDoctorApt.setPatientId(savedP1.getId());
            badDoctorApt.setDoctorName("");
            badDoctorApt.setAppointmentDateTime(todayNow.plusHours(1));
            try {
                appointmentService.bookAppointment(badDoctorApt);
                assertTrue(false, "Should reject appointment with blank doctor name");
            } catch (BadRequestException e) {
                assertTrue(e.getMessage().contains("Doctor name is required"), "Reject appointment with blank doctor name");
            }

            // Test 15: Today's appointments filtering (Today vs Yesterday vs Tomorrow)
            // Add yesterday's appointment
            Appointment yesterdayApt = new Appointment(savedP1, "Dr. Mehta", todayNow.minusDays(1), "SCHEDULED");
            appointmentRepository.save(yesterdayApt);

            // Add tomorrow's appointment
            Appointment tomorrowApt = new Appointment(savedP2, "Dr. Roy", todayNow.plusDays(1), "SCHEDULED");
            appointmentRepository.save(tomorrowApt);

            ResponseEntity<List<Appointment>> todayResp = appointmentController.getTodayAppointments();
            assertTrue(todayResp.getStatusCode() == HttpStatus.OK, "Get today's appointments returns HTTP 200 OK");
            assertTrue(todayResp.getBody() != null && todayResp.getBody().size() == 2,
                    "Today's appointments returns exactly 2 appointments (yesterday and tomorrow excluded)");

            // ==============================================================
            // MODULE 3: CONSULTATION TESTS
            // ==============================================================
            System.out.println("\n--- MODULE 3: CONSULTATION SUMMARY & WORKFLOW TESTS ---");

            // Test 16: Save consultation for Appointment 1
            Consultation cons1 = new Consultation();
            cons1.setAppointmentId(savedApt1.getId());
            cons1.setBloodPressure("120/80");
            cons1.setTemperature(98.6);
            cons1.setNotes("Patient reports mild headache. Prescribed Paracetamol.");
            ResponseEntity<Consultation> respCons1 = consultationController.saveConsultation(cons1);
            assertTrue(respCons1.getStatusCode() == HttpStatus.CREATED, "Save consultation returns HTTP 201 CREATED");
            Consultation savedCons1 = respCons1.getBody();
            assertTrue(savedCons1.getId() != null, "Consultation receives auto-generated ID");
            assertTrue(savedCons1.getStatus().equals("IN_PROGRESS"), "Consultation status defaults to IN_PROGRESS");
            assertTrue(savedCons1.getBloodPressure().equals("120/80"), "Vitals: Blood pressure matches");
            assertTrue(savedCons1.getTemperature().equals(98.6), "Vitals: Temperature matches");

            // Test 17: Reject duplicate consultation for same appointment
            Consultation dupCons = new Consultation();
            dupCons.setAppointmentId(savedApt1.getId());
            dupCons.setBloodPressure("130/85");
            dupCons.setTemperature(99.0);
            dupCons.setNotes("Second consultation attempt");
            try {
                consultationService.saveConsultation(dupCons);
                assertTrue(false, "Should reject duplicate consultation for same appointment");
            } catch (ConflictException e) {
                assertTrue(e.getMessage().contains("already exists"), "Duplicate consultation rejected with ConflictException");
            }

            // Test 18: Reject consultation with non-existent appointment
            Consultation missingAptCons = new Consultation();
            missingAptCons.setAppointmentId(99999L);
            missingAptCons.setBloodPressure("120/80");
            missingAptCons.setTemperature(98.6);
            missingAptCons.setNotes("Notes");
            try {
                consultationService.saveConsultation(missingAptCons);
                assertTrue(false, "Should reject consultation for non-existent appointment");
            } catch (ResourceNotFoundException e) {
                assertTrue(e.getMessage().contains("Appointment not found"), "Reject consultation for non-existent appointment");
            }

            // Test 19: Reject consultation with invalid temperature (< 70 F or > 115 F)
            Consultation badTempCons = new Consultation();
            badTempCons.setAppointmentId(savedApt2.getId());
            badTempCons.setBloodPressure("120/80");
            badTempCons.setTemperature(50.0);
            badTempCons.setNotes("Invalid temp");
            try {
                consultationService.saveConsultation(badTempCons);
                assertTrue(false, "Should reject unrealistic temperature");
            } catch (BadRequestException e) {
                assertTrue(e.getMessage().contains("Temperature"), "Reject consultation with unrealistic temperature");
            }

            // Test 20: Mark consultation as COMPLETED
            ResponseEntity<Consultation> compResp = consultationController.completeConsultation(savedCons1.getId());
            assertTrue(compResp.getStatusCode() == HttpStatus.OK, "Mark consultation complete returns HTTP 200 OK");
            assertTrue(compResp.getBody().getStatus().equals("COMPLETED"), "Consultation status transitions to COMPLETED");

            // Test 21: Verify associated appointment is also synchronized to COMPLETED
            Appointment updatedApt1 = appointmentService.getAppointmentById(savedApt1.getId());
            assertTrue(updatedApt1.getStatus().equals("COMPLETED"), "Associated appointment status synchronized to COMPLETED");

            // Test 22: Prevent repeated completion of consultation
            try {
                consultationService.completeConsultation(savedCons1.getId());
                assertTrue(false, "Should reject repeated completion of already completed consultation");
            } catch (ConflictException e) {
                assertTrue(e.getMessage().contains("already marked as COMPLETED"), "Repeated completion rejected with ConflictException");
            }

            // Test 23: Retrieve completed consultations for Patient 1
            ResponseEntity<List<Consultation>> p1HistoryResp = consultationController.getCompletedConsultationsByPatient(savedP1.getId());
            assertTrue(p1HistoryResp.getStatusCode() == HttpStatus.OK, "Get patient 1 consultations returns HTTP 200 OK");
            assertTrue(p1HistoryResp.getBody() != null && p1HistoryResp.getBody().size() == 1,
                    "Patient 1 has exactly 1 completed consultation in history");
            Consultation historyItem = p1HistoryResp.getBody().get(0);
            assertTrue(historyItem.getAppointment().getDoctorName().equals("Dr. Arvind Gupta"), "Consultation history includes doctor name");
            assertTrue(historyItem.getBloodPressure().equals("120/80"), "Consultation history includes blood pressure vitals");
            assertTrue(historyItem.getTemperature().equals(98.6), "Consultation history includes temperature vitals");
            assertTrue(historyItem.getNotes().contains("Paracetamol"), "Consultation history includes consultation notes");

            // Test 24: Verify Patient 2 has 0 completed consultations (patient history isolation)
            ResponseEntity<List<Consultation>> p2HistoryResp = consultationController.getCompletedConsultationsByPatient(savedP2.getId());
            assertTrue(p2HistoryResp.getBody() != null && p2HistoryResp.getBody().isEmpty(),
                    "Patient 2 history has 0 completed consultations (patient isolation verified)");

            System.out.println("\n===============================================================");
            System.out.println("  VERIFICATION RESULT: " + passedTests + " / " + totalTests + " TESTS PASSED (100%)");
            System.out.println("===============================================================");

        } finally {
            context.close();
        }
    }
}
