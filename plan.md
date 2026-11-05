# Bulk Employee Upload — Implementation Plan

## 1. Implementation Milestones

| Milestone | Work |
| 1. Project setup | Configure Java 21, Quarkus, Camel, PostgreSQL, and frontend structure. |
| 2. Data model | Create employee, upload job, and row result entities. |
| 3. Employee validation | Implement mandatory field, date, gender, mobile, and email validation. |
| 4. Upload processing | Implement CSV upload, asynchronous processing, duplicate detection, and result tracking. |
| 5. REST APIs | Expose upload, status, error report, history, and employee endpoints. |
| 6. Frontend | Build upload interface, progress tracking, history, and employee listing. |
| 7. Preloaded data | Load 50 employees during startup without duplicate insertion. |
| 8. Deployment | Package the application using UBI and configure Docker Compose with PostgreSQL. |

## 2. Data Model

### employees
- `id` — UUID, primary key
- `first_name`, `last_name`
- `dob`, `gender`, `mobile`, `email`
- `department`, `designation`
- `date_of_joining`
- `identity_key` — unique composite identity
- `created_at`

### upload_jobs
- `id` — UUID, primary key
- `file_name`, `file_path`
- `status`
- `total_rows`, `processed_rows`
- `inserted_rows`, `rejected_rows`, `duplicate_rows`
- `failure_message`
- `created_at`, `started_at`, `completed_at`

### upload_row_results
- `id` — primary key
- `job_id` — upload job reference
- `input_row_number`
- `result_status`
- Original CSV field values
- `error_details`
- `processed_at`

Unique constraint: `(job_id, input_row_number)`.

## 3. API Sketch

| Method | Endpoint | Purpose |
| POST | `/api/upload` | Upload CSV and create a job |
| GET | `/api/uploads/{jobId}` | Get processing status and counters |
| GET | `/api/uploads/{jobId}/errors` | Download rejected rows |
| GET | `/api/uploads?page=0&size=10` | Browse upload history |
| GET | `/api/employees?page=0&size=20` | Browse employees |
| GET | `/api/employees/{id}` | Get employee details |

Upload returns HTTP `202 Accepted` with the job ID because processing continues asynchronously.

## 4. Test Strategy

### Validation Tests
- Missing mandatory fields.
- Invalid and impossible dates.
- Invalid gender and mobile number.
- Optional and invalid email values.

### Processing Tests
- Valid employee insertion.
- Duplicate detection within the same file.
- Duplicate detection against existing employees.
- Independent row processing when some rows are invalid.
- Correct processing counters and row-level results.

### Idempotency Tests
- Upload a file successfully.
- Re-upload the complete corrected file.
- Verify previously inserted employees are not inserted again.

### Integration Tests
- Verify REST endpoints and PostgreSQL persistence.
- Verify upload status and error report generation.
- Verify preloaded employees are loaded only once.
- Verify pagination and employee details.

### Deployment Tests
- Start the application using Docker Compose from a clean environment.
- Verify database readiness and application startup.
- Verify the 50 preloaded employees.
- Perform an end-to-end CSV upload.

## 5. What I Would Do With Another Day

1. Replace in-memory SEDA processing with Kafka for durable asynchronous processing and recovery.
2. Introduce Redis for frequently accessed upload progress and status caching.
3. Add integration and end-to-end automated tests using Testcontainers.
4. Improve job recovery and retry handling after application restarts.
5. Improve CSV parsing to report exact physical source line numbers, including multiline records.
6. Add authentication, authorization, and audit logging.
7. Add structured logging, metrics, and operational monitoring.

## 6. Delivery Priorities

The implementation prioritizes a working end-to-end workflow:

**Upload → Validate → Detect Duplicates → Persist → Track Progress → Download Errors → Re-upload Corrected File**

The solution favors a simple modular monolith and PostgreSQL-backed persistence within the four-hour timebox. 
sKafka, Redis, and advanced operational capabilities are identified as future enhancements rather than mandatory dependencies.