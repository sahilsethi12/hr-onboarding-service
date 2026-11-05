# Bulk Employee Upload — Specification

## 1. Problem Statement

Build a web application that allows HR users to upload CSV files containing 500–50,000 employee records, validate them, detect duplicates, .

Users must be able to monitor upload progress, download rejected records with error details, re-upload corrected files without creating duplicates, and browse employees.

The application must also load 50 preloaded employees during startup.

## 2. Functional Requirements

- Upload CSV files up to 20 MB.
- Validate mandatory fields, dates, gender, mobile number, and optional email.
- Process records independently so one invalid row does not stop the upload.
- Detect duplicates within an upload and against existing employees.
- Process uploads asynchronously and expose job progress.
- Generate a downloadable CSV report containing rejected rows and error details.
- Support whole-file re-upload without duplicate employee insertion.
- Provide paginated employee listing and employee details.
- Load 50 preloaded employees idempotently at startup.

## 3. Ambiguities and Decisions

| Ambiguity | Decision |
| Employee unique identifier not provided | Use normalized first name, last name, DOB, and mobile number as a composite identity key. |
| Duplicate handling | Skip duplicates and count them separately from rejected rows. |
| Invalid row handling | Reject only the affected row; continue processing. |
| Upload processing | Use asynchronous processing with Camel SEDA. |
| Progress tracking | Maintain total, processed, inserted, rejected, and duplicate counters. |
| Corrected file upload | Allow the complete file to be uploaded again; skip previously inserted employees. |
| Optional email | Accept blank values; validate when supplied. |
| Date format | Strict DD-MM-YYYY validation. |
| Preloaded data | Load from a classpath CSV resource and skip existing employees. |
| Row numbering | Use CSV record numbers, starting at 2 to account for the header. |

**Duplicate detection limitation:** The composite identity key is a practical substitute because no official employee ID is supplied. It may not identify the same employee if their identifying details change.

## 4. Technical Design

- Java 21 and Red Hat build of Quarkus.
- Apache Camel REST DSL for HTTP endpoints and SEDA for asynchronous processing.
- Quarkus CDI services for business logic.
- Hibernate ORM with Panache.
- PostgreSQL for persistence.
- HTML, CSS, and JavaScript frontend.
- Docker Compose for deployment.
- 
**Main tables:**
- `employees` — employee records and unique identity key.
- `upload_jobs` — upload status and processing counters.
- `upload_row_results` — per-row results and validation errors.

Each employee row is processed in an independent transaction.

## 5. Out of Scope

- Authentication and role-based access.
- Employee editing and deletion.
- Excel file support.
- External HR/payroll integrations.
- Kafka or distributed microservices.
- Advanced analytics and reporting.
- Fuzzy duplicate matching.
- Durable recovery of in-progress SEDA jobs after application restart.
- Physical source-line tracking for multiline CSV records.

## 6. Acceptance Criteria

1. Application starts using `docker compose up`.
2. The 50 preloaded employees are available.
3. CSV uploads are processed asynchronously.
4. Valid, rejected, and duplicate records are counted correctly.
5. Error reports can be downloaded.
6. Corrected whole-file uploads do not create duplicates.s
7. Upload history and employee browsing work.
8. Preloaded employees are not duplicated after restart.

## 7. Future Enhancements (Time Permitting)

Given additional implementation time, the architecture could be enhanced with:

- **Kafka:** Replace the in-memory Camel SEDA queue with durable message-based processing. This would improve reliability, recovery, and scalability for large uploads.
- **Redis:** Cache frequently accessed upload status and progress information, reducing repeated database reads during frontend polling.
- **Horizontal scaling:** Allow multiple application instances to process upload jobs using Kafka consumers.
- **Job recovery:** Resume or safely retry interrupted uploads after application restarts.