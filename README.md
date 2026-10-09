# Hospital OPD (Outpatient Department) Management System

A full-stack enterprise web application designed to manage outpatient clinical workflows, including **Patient Registration**, **Appointment Booking**, and **Clinical Consultation Summaries**.

Built with **Java 22**, **Spring Boot 3.3.4**, **Spring Data JPA**, **MySQL 8.0**, and an **Angular 19** frontend.

---

## 1. Key Features

### Module 1: Patient Registration & Directory
- Register new outpatients with demographic validation (Name, Gender, Age, 10-digit Phone).
- Auto-generated sequential IDs (`GenerationType.IDENTITY`).
- Search patients in real time by name (case-insensitive substring) or phone number.
- Graceful empty states and field-level validation error messages.

### Module 2: Appointment Booking & Scheduling
- Book appointments linked to existing registered patients.
- Timezone-aware filtering for **Today's Appointments** (`Asia/Kolkata` / configurable).
- Strictly separates today's schedule from past or future appointments.
- Prevents booking for non-existent patients with referential integrity checks.

### Module 3: Clinical Consultation & Vitals Tracking
- Record clinical vital signs: **Blood Pressure** (e.g. `120/80`) and **Temperature** ($70^\circ\text{F} - 115^\circ\text{F}$), plus doctor notes.
- **Duplicate Prevention**: Strictly enforces at most one consultation per appointment.
- **State Workflow**: Consultations start as `IN_PROGRESS` and transition to `COMPLETED`.
- Synchronizes appointment status to `COMPLETED` once the consultation is finalized.
- Review historical completed consultations filtered by individual patient with strict data isolation.

---

## 2. Technology Stack

| Layer | Technology | Purpose |
|---|---|---|
| **Backend Framework** | Spring Boot 3.3.4 | RESTful APIs, dependency injection, embedded Tomcat |
| **Language** | Java 22 | Modern, typed object-oriented backend programming |
| **Persistence / ORM** | Spring Data JPA / Hibernate 6 | Relational data mapping, auto-generated and JPQL queries |
| **Validation** | Jakarta Bean Validation | Declarative request payload validation (`@NotBlank`, `@Min`, `@Pattern`) |
| **Database** | MySQL 8.0 (Cloud / Local) | Relational database with foreign keys and unique constraints |
| **Frontend Framework**| Angular 19 | Standalone component architecture, reactive forms, routing |
| **Frontend Language** | TypeScript | Type-safe client-side application logic |
| **HTTP Client** | Angular `HttpClient` | REST API communication with RxJS observables |
| **Build Tools** | Maven & npm | Dependency management and build lifecycle |

---

## 3. Architecture & Design Decisions

```
Angular 19 Client (Port 4200)
             │
             ▼  [HTTP JSON via REST APIs]
┌────────────────────────────────────────────────────────┐
│                   SPRING BOOT BACKEND                  │
│                                                        │
│  Controller Layer (@RestController)                    │
│  ├── PatientController         (/api/patients)         │
│  ├── AppointmentController     (/api/appointments)     │
│  └── ConsultationController    (/api/consultations)    │
│                                                        │
│  Service Layer (@Service, @Transactional)              │
│  ├── PatientService                                    │
│  ├── AppointmentService                                │
│  └── ConsultationService                               │
│                                                        │
│  Repository Layer (Spring Data JPA)                    │
│  ├── PatientRepository                                 │
│  ├── AppointmentRepository                             │
│  └── ConsultationRepository                            │
└──────────────────────────┬─────────────────────────────┘
                           │  [JDBC / SSL]
                           ▼
                  MySQL 8.0 Database
         (patients, appointments, consultations)
```

### Highlights:
- **Clean 3-Tier Layering**: Clear separation of concerns (Controller $\to$ Service $\to$ Repository $\to$ Database).
- **Constructor Injection**: All dependencies are injected via constructors with `final` fields for immutability and testability.
- **No Circular JSON Recursion**: Unidirectional JPA relationships ensure clean JSON serialization without infinite loops.
- **Centralized Error Handling**: `@RestControllerAdvice` translates domain exceptions into standard HTTP error responses (`200`, `201`, `400`, `404`, `409`, `500`).
- **CORS Configured**: Allows cross-origin requests from the Angular development origin (`http://localhost:4200`).

---

## 4. Getting Started

### Prerequisites
- **JDK 17, 21, or 22** installed
- **Node.js 18+** and **npm** installed
- **MySQL 8.0** (or access to the configured cloud database)

---

### Step 1: Start the Backend (Port 8080)

1. Open a terminal and navigate to `backend/`:
   ```bash
   cd backend
   ```
2. Run using Maven:
   ```bash
   mvn spring-boot:run
   ```
   *(Or open `backend/` in IntelliJ IDEA / VS Code and click **Run** on `OpdBackendApplication.java`)*

The backend will start and listen on **`http://localhost:8080`**.

---

### Step 2: Start the Frontend (Port 4200)

1. Open a second terminal and navigate to `frontend/`:
   ```bash
   cd frontend
   ```
2. Install dependencies (if not already installed):
   ```bash
   npm install
   ```
3. Start the Angular development server:
   ```bash
   npm start
   ```

The frontend will open and run on **`http://localhost:4200`**.

---

## 5. Documentation & Technical Guides

- **[REST API Contract](backend/API_CONTRACT.md)**: Full endpoint specification, request bodies, response shapes, and TypeScript interfaces.
- **[Database DDL Schema](backend/schema.sql)**: Production-ready MySQL table definitions, foreign keys, and indexes.

---

## 6. Verification & Automated Tests

The application includes an automated test suite verifying all 3 modules against business rules, edge cases, and validations:
- **Patient Tests**: Registration, Bean Validation, Search by Name/Phone, Missing ID handling.
- **Appointment Tests**: Booking, Patient existence checks, Timezone-aware today's filtering.
- **Consultation Tests**: Duplicate prevention (`409 Conflict`), State transition to `COMPLETED`, Patient history isolation.

All 41 verification assertions pass with a **100% success rate**.
