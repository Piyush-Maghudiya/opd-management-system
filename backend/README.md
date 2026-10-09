# OPD Management Module - Backend

Backend service for the Outpatient Department (OPD) Management Module built with **Java 22**, **Spring Boot 3.3.4**, **Spring Data JPA**, **Hibernate**, and **MySQL 8.0**.

---

## 1. Prerequisites

Before running the application, ensure the following software is installed on your machine:
- **Java Development Kit (JDK)**: JDK 17, 21, or 22 installed. (Verified with Oracle JDK 22).
- **Maven**: Apache Maven 3.8+ (bundled in IntelliJ IDEA or standalone).
- **MySQL Server**: MySQL 8.0 or compatible database server.

---

## 2. MySQL Database Setup

### Step 1: Create Database
Open MySQL Command Line Client or MySQL Workbench, log in with your MySQL credentials, and run:
### Connected Cloud Database (Aiven Cloud MySQL)
The backend is now pre-configured to connect directly to your managed cloud database on Aiven:
- **Host**: `mysql-3c4d67bc-maghudiyapiyush5952-7beb.l.aivencloud.com`
- **Port**: `26314`
- **Database**: `defaultdb`
- **User**: `avnadmin`
- **SSL Mode**: `REQUIRED`

Because `spring.jpa.hibernate.ddl-auto=update` is enabled, Hibernate will **automatically create and maintain the required tables** (`patients`, `appointments`, `consultations`) inside `defaultdb` on the cloud when Spring Boot boots!

---

## 3. Configuration (`application.properties`)

The configuration file is located at `src/main/resources/application.properties`:
```properties
server.port=8080
spring.datasource.url=jdbc:mysql://mysql-3c4d67bc-maghudiyapiyush5952-7beb.l.aivencloud.com:26314/defaultdb?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=avnadmin
spring.datasource.password=YOUR_DATABASE_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
app.timezone=Asia/Kolkata
```

---

## 4. How to Start the Backend

### Option A: Using Maven Command Line
```powershell
cd backend
mvn spring-boot:run
```

### Option B: Using IntelliJ IDEA / Eclipse / VS Code
1. Open the `backend/` folder in your IDE.
2. Navigate to `src/main/java/com/opd/backend/OpdBackendApplication.java`.
3. Right-click and select **Run 'OpdBackendApplication'**.

The backend will start and listen on port **`8080`**:
```
Tomcat started on port 8080 (http) with context path '/'
Started OpdBackendApplication in 7 seconds
```

---

## 5. Verification & Testing

### Running the Complete Automated Test Suite (41 Tests)
A full programmatic test suite is provided in `src/test/java/com/opd/backend/StandaloneVerificationRunner.java`. It tests all 3 modules against an in-memory database without requiring MySQL to be active:

```powershell
# Run using your IDE or java command
& 'C:\Program Files\Java\jdk-22\bin\java.exe' -cp (Get-Content runtime-cp.txt) com.opd.backend.StandaloneVerificationRunner
```
**Output**:
```
  VERIFICATION RESULT: 41 / 41 TESTS PASSED (100%)
```

---

## 6. Frontend Integration & API Documentation

- **API Contract**: See [`API_CONTRACT.md`](./API_CONTRACT.md) for full endpoint specifications, request payloads, response schemas, and TypeScript interfaces.
- **Interview Preparation**: See [`INTERVIEW_GUIDE.md`](./INTERVIEW_GUIDE.md) for detailed explanations of architecture, annotations, database design, step-by-step code flows, and 25+ interview questions and answers.
- **Progress Tracking**: See [`BACKEND_PROGRESS.md`](./BACKEND_PROGRESS.md) for the completion checklist.

---

## 7. CORS Configuration
CORS is pre-configured in `CorsConfig.java` to allow requests from the Angular development server (`http://localhost:4200` and `http://127.0.0.1:4200`) with standard HTTP methods (`GET`, `POST`, `PATCH`, `PUT`, `DELETE`, `OPTIONS`).
