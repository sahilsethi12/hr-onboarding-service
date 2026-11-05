# Bulk Employee Upload — HR Onboarding

A web-based application for bulk employee onboarding using CSV files. It validates employee records, detects duplicates, processes uploads asynchronously, and provides upload progress and error reports.

## 1. Technology Stack

- Java 21
- Red Hat build of Quarkus 3.27.3
- Apache Camel Quarkus — REST DSL and asynchronous processing
- PostgreSQL
- Hibernate ORM with Panache
- HTML, CSS and JavaScript
- Docker Compose
- Red Hat UBI 9 with OpenJDK 21 runtime

## 2. Features

- Upload CSV files containing up to 50,000 employee records.
- Validate mandatory fields, dates, gender, mobile number and email.
- Process records asynchronously.
- Detect duplicates within an upload and against existing employees.
- Track total, processed, inserted, rejected and duplicate records.
- Download rejected records with validation errors.
- Re-upload corrected files without inserting existing employees again.
- Browse employees and upload history.
- Load 50 preloaded employees during startup.

## 3. Prerequisites

For Docker deployment:

- Docker Engine
- Docker Compose v2

For local development:

- Java 21
- Maven
- PostgreSQL 16

## 4. Run the Application

Clone the repository and navigate to the project root.

```bash
git clone https://github.com/sahilsethi12/hr-onboarding-service.git
cd hr-onboarding-service
```

Start the application:

```bash
docker compose up --build
```

This starts:

| Service | Purpose |
| backend | Quarkus application and frontend |
| postgres | PostgreSQL database |

The database and uploaded files use persistent Docker volumes.

Open the application:

**Frontend:** http://localhost:8080


Stop the application:

```bash
docker compose down
```

To stop the application and remove database and upload volumes:

```bash
docker compose down -v
```

**Warning:** The second command permanently removes persisted application data.


## 4.1 Faster Builds

The Dockerfile uses a multi-stage build and separates Maven dependency resolution from source compilation. 
This allows Docker/Podman to reuse cached Maven dependencies when application source code changes.

### First build

```bash
docker compose up --build -d
```

The first build may take longer because it downloads the base images and Maven dependencies.

### Subsequent builds

When only backend source code changes, rebuild only the backend:

```bash
docker compose up -d --build backend
```

### Build backend image separately

```bash
docker compose build backend
```

Then start the services:

```bash
docker compose up -d
```

**Build optimization:** Keep `pom.xml` unchanged when only modifying Java source files. This allows the Maven dependency layer to remain cached.

### View build and application logs

```bash
docker compose logs -f backend
```

### Check running containers

```bash
docker compose ps
```
## 5. CSV Format

The CSV file must contain these columns:

```csv
first_name,last_name,dob,gender,mobile,email,department,designation,date_of_joining
```

Example:

```csv
first_name,last_name,dob,gender,mobile,email,department,designation,date_of_joining
John,Doe,15-06-1990,M,9876543210,john@example.com,IT,Developer,01-01-2024
```

### Validation Rules

- First name and last name are mandatory.
- DOB and date of joining must use `DD-MM-YYYY`.
- Gender must be `M` or `F`.
- Mobile must contain exactly 10 digits.
- Email is optional but must be valid when supplied.
- Invalid records are rejected individually; other records continue processing.

## 6. Application Workflow

1. Upload the employee CSV.
2. The application creates an upload job and returns its ID.
3. Camel processes the file asynchronously through a SEDA route.
4. Each employee record is validated and checked for duplicates.
5. Valid employees are saved in PostgreSQL.
6. Rejected and duplicate records are tracked separately.
7. The frontend displays processing progress.
8. Download the error report, correct the records and upload the complete file again.

### Processing Reconciliation

```text
Total Rows = Inserted + Rejected + Duplicate
```

## 7. API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/upload` | Upload employee CSV |
| GET | `/api/uploads/{jobId}` | Get upload status |
| GET | `/api/uploads/{jobId}/errors` | Download error report |
| GET | `/api/uploads?page=0&size=10` | Upload history |
| GET | `/api/employees?page=0&size=20` | Employee listing |
| GET | `/api/employees/{id}` | Employee details |

The upload endpoint returns HTTP `202 Accepted` because processing happens asynchronously.

## 8. Database Design

The application uses three primary tables:

| Table | Responsibility |
|---|---|
| `employees` | Stores employee records and unique identity keys |
| `upload_jobs` | Stores upload metadata, status and processing counters |
| `upload_row_results` | Stores individual row outcomes and error details |

Duplicate detection uses a normalized composite identity key based on first name, last name, DOB and mobile number.

## 9. Preloaded Employees

The application loads the supplied 50 employee records from:

```text
src/main/resources/data/preloaded-employees.csv
```

The startup loader checks existing employee identity keys before inserting records. Restarting the application does not insert the same employees again.

## 10. Local Development

Configure PostgreSQL and use the default local datasource settings in `application.properties`.

Start the application:

```bash
mvn quarkus:dev
```

Run tests:

```bash
mvn test
```

Package the application:

```bash
mvn package -DskipTests
```

## 11. Design Decisions and Limitations

- A modular monolith was chosen to keep the solution simple within the assignment timebox.
- Apache Camel SEDA provides asynchronous in-process processing.
- Each employee row is processed independently.
- PostgreSQL remains the source of truth.
- The current SEDA queue is in-memory; interrupted jobs are not durably recovered after an application restart.
- Duplicate detection uses a composite identity because no official employee identifier is provided.

### Future Enhancements

With additional implementation time:

- Kafka for durable asynchronous processing and recovery.
- Redis for upload status and progress caching.
- Automated integration and end-to-end tests.
- Job retry and recovery mechanisms.
- Authentication, authorization and audit logging.

## 12. Project Structure

```text
hr-onboarding-service/
├── docker/
│   └── Dockerfile
├── docker-compose.yml
├── pom.xml
├── README.md
├── spec.md
├── plan.md
├── ai-log.md
└── src/
    └── main/
        ├── java/
        └── resources/
            ├── application.properties
            ├── data/
            │   └── preloaded-employees.csv
            └── META-INF/resources/
                ├── index.html
                ├── css/
                └── js/
```
