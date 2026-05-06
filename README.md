# RadiaSync Management API

RESTful API for managing patients undergoing radioisotope treatments, featuring real-time radiation decay calculations, smartwatch telemetry integration, FHIR interoperability, and automated medical notifications.

## Technologies and Services

### Core Framework
- **Java 21 LTS** - Programming language
- **Spring Boot 4.0.3** - Application framework
- **Maven** - Dependency management and build tool

### Database
- **PostgreSQL** - Production relational database
- **H2** - In-memory database for development/testing
- **Spring Data JPA / Hibernate** - ORM with PostgreSQL dialect
- **HikariCP** - Connection pooling (30s timeout, max 10 connections)

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication using jjwt 0.11.5
- **Spring Security Filter** - Request interception via JwtFilter

### External Services
- **SendGrid** - Transactional email delivery (welcome emails, password resets, support)
- **Cloudinary** - Image storage for user avatars
- **HAPI FHIR R4** - Healthcare interoperability standard (HL7 FHIR)

### Document Generation
- **OpenPDF 1.3.30** - Radiological discharge report generation

### Infrastructure
- **Docker** - Containerization support
- **Scheduled Tasks** - Hourly radiation decay verification and automated alerts

---

## Project Structure

```
src/main/java/radioisotops/api/com/example/demo/
├── DemoApplication.java              # Application entry point
│
├── controller/                       # REST API Controllers
│   ├── AuthController.java           # Authentication and user preferences
│   ├── PatientController.java        # Patient management and telemetry
│   ├── UserController.java           # Doctor and admin user management
│   ├── WatchController.java          # Smartwatch-specific endpoints
│   └── NotificationController.java   # Notification management
│
├── model/                            # JPA Entities
│   ├── User.java                     # System users (ADMIN, MEDICO, PACIENTE)
│   ├── Patient.java                  # Patient clinical data and watch info
│   ├── Doctor.java                   # Medical professional data
│   ├── Treatment.java                # Radioisotope treatment records
│   ├── Device.java                   # Smartwatch device data
│   ├── Notification.java             # System notifications and alerts
│   ├── UserActivity.java             # Doctor-patient visit history
│   └── AlertConfig.java              # Alert threshold configuration
│
├── repository/                        # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── PatientRepository.java
│   ├── DoctorRepository.java
│   ├── TreatmentRepository.java
│   ├── DeviceRepository.java
│   ├── NotificationRepository.java
│   └── ActivityRepository.java
│
├── service/                           # Business Logic Services
│   ├── EmailService.java              # SendGrid email operations
│   ├── PdfGeneratorService.java       # PDF report generation
│   └── FileStorageService.java        # Cloudinary image uploads
│
├── security/                          # Authentication and Security
│   ├── JwtUtil.java                   # Token generation and validation
│   ├── JwtFilter.java                 # JWT request filter
│   ├── WebConfig.java                 # CORS configuration
│   ├── ScheduledTaskService.java      # Hourly scheduled tasks
│   └── CloudinaryConfig.java          # Image service configuration
│
├── fhir/                              # FHIR Interoperability
│   ├── FhirServerConfig.java          # FHIR server configuration
│   ├── PatientResourceProvider.java   # FHIR Patient resource
│   └── ObservationResourceProvider.java # FHIR Observation resource
│
└── dto/                               # Data Transfer Objects
    ├── LoginRequest.java
    ├── LoginResponseDTO.java
    ├── WatchEstadoDTO.java
    ├── PatientTableDTO.java
    └── PreferenciasDTO.java
```

---

## API Endpoints

### Authentication and User Management
**Controller:** `src/main/java/radioisotops/api/com/example/demo/controller/AuthController.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Authenticate user with email and password. Returns JWT token and user data. | No |
| POST | `/api/auth/login-watch` | Smartwatch authentication using watchId. | No |
| GET | `/api/auth/me` | Get current authenticated user data. | Yes |
| GET | `/api/auth/doctores` | List all users with MEDICO role. | Yes |
| POST | `/api/auth/doctor/{id}/status` | Toggle doctor active/inactive status. | Yes |
| POST | `/api/auth/doctor/{id}/password` | Reset doctor password and send email notification. | Yes |
| POST | `/api/auth/update-password` | Update password for authenticated user. | Yes |
| PUT | `/api/auth/preferencias` | Update user notification preferences. | Yes |

---

### Patient Management
**Controller:** `src/main/java/radioisotops/api/com/example/demo/controller/PatientController.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/patients/register-full` | Complete patient registration: creates User, Patient, and Treatment in a single transaction. | Yes |
| GET | `/api/patients/perfil/{cip}` | Get patient profile with treatment status and radiation level. | Yes |
| GET | `/api/patients/lista-gestion` | List all patients with treatment status for medical management. | Yes |
| POST | `/api/patients/{cip}/actualizar-telemetria` | Update smartwatch telemetry data (battery, last sync). | Yes |
| POST | `/api/patients/{cip}/update-watch` | Update smartwatch device status and generate low battery alerts. | Yes |
| POST | `/api/patients/{cip}/send-instruction` | Send medical instruction to patient's smartwatch as notification. | Yes |
| GET | `/api/patients/{cip}/informe-alta` | Generate and download radiological discharge PDF report. | Yes |
| GET | `/api/patients/count-total` | Get total number of registered patients. | Yes |
| POST | `/api/patients/{cip}/register-view` | Register doctor visit to patient profile (activity history). | Yes |
| GET | `/api/patients/recent-patients` | Get 4 most recently visited patients by doctor. | Yes |
| GET | `/api/patients/dashboard/{userId}` | Get patient dashboard data (progress, isotope, next appointment). | Yes |
| POST | `/api/patients/{userId}/update-mood` | Update patient mood status (happy/neutral/straight/sad). | Yes |
| POST | `/api/patients/{userId}/contact-doctor` | Patient contact request to doctor (sends notification and email). | Yes |

---

### User Administration
**Controller:** `src/main/java/radioisotops/api/com/example/demo/controller/UserController.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/users/register-doctor` | Register new doctor (admin only). Sends welcome email. | Yes |
| PUT | `/api/users/{id}/update-password` | Update user password and clear change-required flag. | Yes |
| GET | `/api/users/{id}` | Get complete user profile by ID. | Yes |
| POST | `/api/users/{id}/upload-avatar` | Upload user avatar image to Cloudinary. | Yes |

---

### Smartwatch API
**Controller:** `src/main/java/radioisotops/api/com/example/demo/controller/WatchController.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/watch/estado/{cip}` | Get treatment status for smartwatch: isolation days, battery, messages, and phase-specific instructions. | No |
| POST | `/api/watch/actualizar-telemetria/{cip}` | Update watch telemetry and device status. | No |

---

### Notifications
**Controller:** `src/main/java/radioisotops/api/com/example/demo/controller/NotificationController.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/notifications/me` | Get all notifications for authenticated doctor. | Yes |
| PUT | `/api/notifications/{id}/read` | Mark notification as read. | Yes |
| GET | `/api/notifications/count` | Count unread notifications for doctor. | Yes |
| GET | `/api/notifications/patient/{cip}` | Get messages/instructions for patient (marks as read). | Yes |
| GET | `/api/notifications/consultas` | Get support queries sent by patients. | Yes |
| POST | `/api/notifications/patient/{cip}/send-instruction` | Send formal instruction to patient's watch (predefined messages per phase). | Yes |
| GET | `/api/notifications/count-today` | Count alerts generated today. | Yes |

---

### FHIR Interoperability
**Configuration:** `src/main/java/radioisotops/api/com/example/demo/fhir/FhirServerConfig.java`
**Resources:**
- `src/main/java/radioisotops/api/com/example/demo/fhir/PatientResourceProvider.java`
- `src/main/java/radioisotops/api/com/example/demo/fhir/ObservationResourceProvider.java`

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/fhir/Patient?identifier={dni}` | Search patient by DNI/CIP using FHIR R4 standard. | No |
| GET | `/fhir/Observation?subject=Patient/{id}` | Get patient observations (e.g., watch battery level) in FHIR format. | No |

---

## Data Models

### User (`src/main/java/radioisotops/api/com/example/demo/model/User.java`)
System users with roles: ADMIN, MEDICO, PACIENTE. Stores credentials, notification preferences, language, and timezone.

### Patient (`src/main/java/radioisotops/api/com/example/demo/model/Patient.java`)
Clinical patient data including DNI/CIP, birth date, mood status, and smartwatch details (watchId, battery, last sync). OneToOne relationship with User and Doctor.

### Doctor (`src/main/java/radioisotops/api/com/example/demo/model/Doctor.java`)
Medical professional data: specialty, license number. OneToOne relationship with User.

### Treatment (`src/main/java/radioisotops/api/com/example/demo/model/Treatment.java`)
Radioisotope treatment records: isotope type, dosage (MBq), dates, instructions. ManyToOne relationships with Patient and Doctor.

### Device (`src/main/java/radioisotops/api/com/example/demo/model/Device.java`)
Smartwatch device data: serial number, type, status, last connection. OneToOne with Patient.

### Notification (`src/main/java/radioisotops/api/com/example/demo/model/Notification.java`)
System notifications and alerts: message, date, read status, subject. ManyToOne with Doctor and Patient.

### AlertConfig (`src/main/java/radioisotops/api/com/example/demo/model/AlertConfig.java`)
Configurable alert thresholds per parameter (radiation levels, battery, etc.).

### UserActivity (`src/main/java/radioisotops/api/com/example/demo/model/UserActivity.java`)
Activity history tracking which doctor visited which patient and when.

---

## Radiation Decay Calculation

The API calculates real-time remaining activity based on isotope half-lives:

| Isotope | Half-life |
|---------|-----------|
| I-131 | 192.48 hours |
| Lu-177 | 159.36 hours |
| Co-60 | 46164 hours |

### Treatment Phases

| Phase | Radiation Level | Protocol |
|-------|----------------|----------|
| Initial | >400 MBq | Total isolation |
| Decay | 400-1 MBq | Precautions required |
| Exemption | <1 MBq | Full clearance |

The `ScheduledTaskService` (`src/main/java/radioisotops/api/com/example/demo/security/ScheduledTaskService.java`) runs hourly to verify decay progress and automatically alert doctors when patients drop below 400 MBq.

---

## Setup Instructions

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- PostgreSQL (for production)
- SendGrid account
- Cloudinary account

### Local Development

1. Clone the repository:
```bash
git clone <repository-url>
cd API
```

2. Configure environment variables (see Environment Variables section).

3. Run with Maven:
```bash
./mvnw spring-boot:run
```

Or build and run the JAR:
```bash
./mvnw clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

### Docker Deployment

```bash
docker build -t radioisotope-api .
docker run -p 8080:8080 --env-file .env radioisotope-api
```

---

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `PORT` | Application port (default: 8080) | No |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | Yes |
| `SPRING_DATASOURCE_USERNAME` | Database username | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database password | Yes |
| `AUTH_SECRET_KEY` | JWT token signing key | Yes |
| `CLOUDINARY_API_KEY` | Cloudinary API key | Yes |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret | Yes |
| `SENDGRID_API_KEY` | SendGrid API key for emails | Yes |

### Example `.env` file:
```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/radioisotope_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
AUTH_SECRET_KEY=your-secret-key-min-256-bits
CLOUDINARY_API_KEY=your-cloudinary-key
CLOUDINARY_API_SECRET=your-cloudinary-secret
SENDGRID_API_KEY=SG.your-sendgrid-key
```

---

## Configuration

**File:** `src/main/resources/application.properties`

Key settings:
- JPA Hibernate DDL auto-update mode
- PostgreSQL dialect configured
- Europe/Madrid timezone
- Cloudinary cloud name: dszlunuep
- JWT token expiration: 24 hours

---

## Authentication

All endpoints except `/api/auth/login`, `/api/auth/login-watch`, and FHIR endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <token>
```

Token structure (24h validity):
- Subject: user email
- Claims: user ID, role
- Signed with HS256 algorithm

**JWT Utilities:** `src/main/java/radioisotops/api/com/example/demo/security/JwtUtil.java`
**JWT Filter:** `src/main/java/radioisotops/api/com/example/demo/security/JwtFilter.java`

---

## Build and Test

```bash
# Run tests
./mvnw test

# Build without tests
./mvnw clean package -DskipTests

# Check compiled classes
./mvnw compile
```

Test file: `src/test/java/radioisotops/api/com/example/demo/DemoApplicationTests.java`

---

## License

© 2026 RadiaSync. All rights reserved.
