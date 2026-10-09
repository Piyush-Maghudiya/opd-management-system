# OPD Management Module - REST API Contract

**Base URL**: `http://localhost:8080/api`  
**Content-Type**: `application/json`  
**Date-Time Format**: ISO-8601 string (`yyyy-MM-dd'T'HH:mm:ss`, e.g. `2026-10-09T14:30:00`)  
**Configured Timezone**: `Asia/Kolkata` (configurable via `app.timezone` in `application.properties`)  
**CORS**: Allowed for Angular development origins (`http://localhost:4200`, `http://127.0.0.1:4200`)  

---

## Standard Error Response Structure

All endpoints return a standardized JSON error response when an error occurs:

```json
{
  "timestamp": "2026-10-09T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Input validation failed. Please check the provided fields.",
  "validationErrors": {
    "phone": "Phone number must be a valid 10-digit number",
    "age": "Age must be at least 1"
  }
}
```

### HTTP Status Code Conventions
- `200 OK`: Request succeeded (GET, PATCH).
- `201 Created`: Resource successfully created (POST).
- `400 Bad Request`: Input validation failed, malformed JSON, or missing required fields.
- `404 Not Found`: Target patient, appointment, or consultation record does not exist.
- `409 Conflict`: Business rule violation (e.g., duplicate consultation for the same appointment, or attempting to complete an already completed consultation).
- `500 Internal Server Error`: Unexpected server issue (stack traces and credentials suppressed).

---

## 1. Patient APIs

### 1.1 Register New Patient
- **Endpoint**: `POST /api/patients`
- **Description**: Registers a new outpatient.
- **Request Body**:
  ```json
  {
    "name": "Ramesh Sharma",
    "gender": "Male",
    "age": 42,
    "phone": "9876543210"
  }
  ```
- **Field Constraints**:
  - `name`: string, required, non-blank
  - `gender`: string, required, non-blank (`"Male"`, `"Female"`, or `"Other"`)
  - `age`: integer, required, minimum `1`, maximum `130`
  - `phone`: string, required, exactly 10 digits (`^[0-9]{10}$`)
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 1,
    "name": "Ramesh Sharma",
    "gender": "Male",
    "age": 42,
    "phone": "9876543210"
  }
  ```
- **Errors**:
  - `400 Bad Request`: Validation failure (empty name, invalid age, invalid phone format).

---

### 1.2 Get All Patients / Search Patients
- **Endpoint**: `GET /api/patients`
- **Optional Query Parameter**:
  - `search` (string): Search query to filter patients by name (case-insensitive substring) OR phone number.
- **Examples**:
  - `GET /api/patients` -> Returns all registered patients.
  - `GET /api/patients?search=ramesh` -> Searches patients with "ramesh" in name.
  - `GET /api/patients?search=987654` -> Searches patients with "987654" in phone.
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "name": "Ramesh Sharma",
      "gender": "Male",
      "age": 42,
      "phone": "9876543210"
    },
    {
      "id": 2,
      "name": "Anita Desai",
      "gender": "Female",
      "age": 29,
      "phone": "9123456789"
    }
  ]
  ```
- **Note**: If no matching patients are found, returns `200 OK` with an empty array `[]`.

---

### 1.3 Get Patient by ID
- **Endpoint**: `GET /api/patients/{id}`
- **Path Variable**: `id` (numeric ID of the patient)
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "name": "Ramesh Sharma",
    "gender": "Male",
    "age": 42,
    "phone": "9876543210"
  }
  ```
- **Errors**:
  - `404 Not Found`: When patient with the given ID does not exist.

---

## 2. Appointment APIs

### 2.1 Book Appointment
- **Endpoint**: `POST /api/appointments`
- **Description**: Books an appointment for an existing registered patient.
- **Request Body (Frontend Option A - with patientId)**:
  ```json
  {
    "patientId": 1,
    "doctorName": "Dr. Arvind Gupta",
    "appointmentDateTime": "2026-10-09T15:30:00"
  }
  ```
- **Request Body (Frontend Option B - with nested patient object)**:
  ```json
  {
    "patient": {
      "id": 1
    },
    "doctorName": "Dr. Arvind Gupta",
    "appointmentDateTime": "2026-10-09T15:30:00"
  }
  ```
  *(Both formats are fully accepted by the backend).*
- **Field Constraints**:
  - `patientId` / `patient.id`: number, required, must correspond to an existing registered patient.
  - `doctorName`: string, required, non-blank.
  - `appointmentDateTime`: ISO-8601 string (`yyyy-MM-dd'T'HH:mm:ss`), required.
  - `status`: string, optional (defaults to `"SCHEDULED"` if omitted).
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 1,
    "patient": {
      "id": 1,
      "name": "Ramesh Sharma",
      "gender": "Male",
      "age": 42,
      "phone": "9876543210"
    },
    "doctorName": "Dr. Arvind Gupta",
    "appointmentDateTime": "2026-10-09T15:30:00",
    "status": "SCHEDULED",
    "patientId": 1
  }
  ```
- **Errors**:
  - `400 Bad Request`: Missing patient ID, missing doctor name, or missing appointment date-time.
  - `404 Not Found`: When the referenced patient ID does not exist in the database.

---

### 2.2 Get Today's Appointments
- **Endpoint**: `GET /api/appointments/today`
- **Description**: Returns all appointments scheduled for the current calendar day in the configured clinic timezone (`Asia/Kolkata`).
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "patient": {
        "id": 1,
        "name": "Ramesh Sharma",
        "gender": "Male",
        "age": 42,
        "phone": "9876543210"
      },
      "doctorName": "Dr. Arvind Gupta",
      "appointmentDateTime": "2026-10-09T15:30:00",
      "status": "SCHEDULED",
      "patientId": 1
    }
  ]
  ```
- **Note**: Appointments scheduled for yesterday or tomorrow are strictly excluded. Returns `[]` if no appointments are scheduled for today.

---

### 2.3 Get Appointment by ID
- **Endpoint**: `GET /api/appointments/{id}`
- **Path Variable**: `id` (numeric ID of the appointment)
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "patient": {
      "id": 1,
      "name": "Ramesh Sharma",
      "gender": "Male",
      "age": 42,
      "phone": "9876543210"
    },
    "doctorName": "Dr. Arvind Gupta",
    "appointmentDateTime": "2026-10-09T15:30:00",
    "status": "SCHEDULED",
    "patientId": 1
  }
  ```
- **Errors**:
  - `404 Not Found`: When appointment does not exist.

---

## 3. Consultation APIs

### 3.1 Record Consultation Summary
- **Endpoint**: `POST /api/consultations`
- **Description**: Records clinical vitals (blood pressure, temperature) and short notes for an appointment.
- **Request Body (Frontend Option A - with appointmentId)**:
  ```json
  {
    "appointmentId": 1,
    "bloodPressure": "120/80",
    "temperature": 98.6,
    "notes": "Patient complains of mild headache. Prescribed rest and hydration."
  }
  ```
- **Request Body (Frontend Option B - with nested appointment object)**:
  ```json
  {
    "appointment": {
      "id": 1
    },
    "bloodPressure": "120/80",
    "temperature": 98.6,
    "notes": "Patient complains of mild headache. Prescribed rest and hydration."
  }
  ```
- **Field Constraints**:
  - `appointmentId` / `appointment.id`: number, required, must reference an existing appointment.
  - `bloodPressure`: string, required, non-blank (e.g. `"120/80"`).
  - `temperature`: number (double), required, valid human temperature range (`70.0` to `115.0` °F).
  - `notes`: string, required, non-blank.
  - `status`: string, optional (defaults to `"IN_PROGRESS"` upon creation).
- **Success Response (`201 Created`)**:
  ```json
  {
    "id": 1,
    "appointment": {
      "id": 1,
      "patient": {
        "id": 1,
        "name": "Ramesh Sharma",
        "gender": "Male",
        "age": 42,
        "phone": "9876543210"
      },
      "doctorName": "Dr. Arvind Gupta",
      "appointmentDateTime": "2026-10-09T15:30:00",
      "status": "SCHEDULED",
      "patientId": 1
    },
    "bloodPressure": "120/80",
    "temperature": 98.6,
    "notes": "Patient complains of mild headache. Prescribed rest and hydration.",
    "status": "IN_PROGRESS",
    "appointmentId": 1
  }
  ```
- **Errors**:
  - `400 Bad Request`: Missing vitals, missing notes, or temperature out of valid range.
  - `404 Not Found`: Referenced appointment ID does not exist.
  - `409 Conflict`: A consultation summary already exists for this appointment (prevents duplicates).

---

### 3.2 Mark Consultation as Completed
- **Endpoint**: `PATCH /api/consultations/{id}/complete`
- **Path Variable**: `id` (numeric ID of the consultation)
- **Description**: Marks the consultation status as `"COMPLETED"`. Also automatically synchronizes the associated appointment status to `"COMPLETED"`.
- **Request Body**: None (empty body).
- **Success Response (`200 OK`)**:
  ```json
  {
    "id": 1,
    "appointment": {
      "id": 1,
      "patient": {
        "id": 1,
        "name": "Ramesh Sharma",
        "gender": "Male",
        "age": 42,
        "phone": "9876543210"
      },
      "doctorName": "Dr. Arvind Gupta",
      "appointmentDateTime": "2026-10-09T15:30:00",
      "status": "COMPLETED",
      "patientId": 1
    },
    "bloodPressure": "120/80",
    "temperature": 98.6,
    "notes": "Patient complains of mild headache. Prescribed rest and hydration.",
    "status": "COMPLETED",
    "appointmentId": 1
  }
  ```
- **Errors**:
  - `404 Not Found`: When consultation with the given ID does not exist.
  - `409 Conflict`: When the consultation is already marked as `"COMPLETED"` (prevents repeated completion).

---

### 3.3 Get Completed Consultations for a Patient
- **Endpoint**: `GET /api/patients/{patientId}/consultations`
- **Path Variable**: `patientId` (numeric ID of the patient)
- **Description**: Retrieves consultation history for a specific patient where `status = 'COMPLETED'`.
- **Success Response (`200 OK`)**:
  ```json
  [
    {
      "id": 1,
      "appointment": {
        "id": 1,
        "patient": {
          "id": 1,
          "name": "Ramesh Sharma",
          "gender": "Male",
          "age": 42,
          "phone": "9876543210"
        },
        "doctorName": "Dr. Arvind Gupta",
        "appointmentDateTime": "2026-10-09T15:30:00",
        "status": "COMPLETED",
        "patientId": 1
      },
      "bloodPressure": "120/80",
      "temperature": 98.6,
      "notes": "Patient complains of mild headache. Prescribed rest and hydration.",
      "status": "COMPLETED",
      "appointmentId": 1
    }
  ]
  ```
- **Errors**:
  - `404 Not Found`: When `patientId` does not exist in the database.
- **Note**: If the patient exists but has no completed consultations yet, returns `200 OK` with an empty array `[]`.

---

## 4. TypeScript Interfaces for Angular Frontend

For seamless integration in Angular, define these TypeScript interfaces in `frontend/src/app/models/opd.models.ts`:

```typescript
export interface Patient {
  id?: number;
  name: string;
  gender: string;
  age: number;
  phone: string;
}

export interface Appointment {
  id?: number;
  patient?: Patient;
  patientId?: number;
  doctorName: string;
  appointmentDateTime: string; // ISO string '2026-10-09T14:30:00'
  status?: 'SCHEDULED' | 'COMPLETED';
}

export interface Consultation {
  id?: number;
  appointment?: Appointment;
  appointmentId?: number;
  bloodPressure: string;
  temperature: number;
  notes: string;
  status?: 'IN_PROGRESS' | 'COMPLETED';
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  validationErrors?: { [field: string]: string };
}
```
