
package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.dto.*;
import org.acme.entity.Employee;
import org.acme.entity.UploadJob;
import org.acme.exception.DuplicateEmployeeException;
import org.acme.repository.UploadJobRepository;
import org.acme.service.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.camel.dataformat.csv.CsvDataFormat;
import org.acme.entity.Employee;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MainRoute extends RouteBuilder {

    @Inject
    EmployeeService employeeService;

    @Inject
    UploadJobRepository uploadJobRepository;

    @Inject
    EmployeeValidator employeeValidator;

    @Inject
    EmployeeProcessor employeeProcessor;

    @Inject
    EmployeeImportService employeeImportService;

    @Inject
    UploadJobService uploadJobService;

    @Inject
    ErrorReportService errorReportService;


    @Override
    public void configure() {

        CsvDataFormat csvDataFormat = new CsvDataFormat();

        csvDataFormat.setUseMaps(true);
        csvDataFormat.setSkipHeaderRecord(true);


        // Excpetions

        // Duplicate employee: HTTP 409
        onException(DuplicateEmployeeException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(409))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .process(exchange -> {
                    Exception exception = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT, Exception.class);

                    ApiError error = new ApiError(
                            409,
                            "Conflict",
                            exception.getMessage()
                    );

                    exchange.getMessage().setBody(error);
                })
                .marshal().json(JsonLibrary.Jackson);

// Invalid input: HTTP 400
        onException(IllegalArgumentException.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .process(exchange -> {
                    Exception exception = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT, Exception.class);

                    ApiError error = new ApiError(
                            400,
                            "Bad Request",
                            exception.getMessage()
                    );

                    exchange.getMessage().setBody(error);
                })
                .marshal().json(JsonLibrary.Jackson);

        // Unexpected error: HTTP 500
        onException(Exception.class)
                .handled(true)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .log("Exception ${exception.message}")
                .process(exchange -> {
                    ApiError error = new ApiError(
                            500,
                            "Internal Server Error",
                            "An unexpected error occurred"
                    );

                    exchange.getMessage().setBody(error);
                })
                .marshal().json(JsonLibrary.Jackson);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        JacksonDataFormat employeeJsonFormat =
                new JacksonDataFormat(objectMapper, Employee.class);

        JacksonDataFormat employeeListJsonFormat =
                new JacksonDataFormat(objectMapper, List.class);

        JacksonDataFormat employeePageJsonFormat =
                new JacksonDataFormat(objectMapper, EmployeePageResponse.class);

        JacksonDataFormat employeeResponseJsonFormat =
                new JacksonDataFormat(objectMapper, EmployeeResponse.class);
        JacksonDataFormat uploadHistoryJsonFormat =
                new JacksonDataFormat(
                        objectMapper,
                        UploadHistoryResponse.class);


        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        restConfiguration()
                .component("platform-http")
                .contextPath("/api")
                .port(8080);


        rest("/upload")
                .post()
                .description("Upload an attachment")
                .consumes("multipart/form-data")
                .to("direct:uploadCsv");

        rest("/uploads")
                .get()
                .description("Get paginated upload history")
                .to("direct:getUploadHistory")
                .get("/{jobId}")
                .description("Get upload processing status")
                .to("direct:getUploadStatus");

        // Create Employee
        rest("/employees")
                .post()
                .to("direct:createEmployee");

        // Retrieve Employees
        rest("/employees")
                .get()
                .to("direct:getEmployees");

        rest("/employees")
                .get("/{id}")
                .to("direct:getEmployeeById");

        rest("/uploads")
                .get("/{jobId}/errors")
                .description("Download CSV error report")
                .produces("text/csv")
                .to("direct:downloadErrorReport");


        from("direct:getUploadHistory")
                .routeId("get-upload-history-route")
                .process(exchange -> {

                    Integer page = exchange.getIn()
                            .getHeader("page", Integer.class);

                    Integer size = exchange.getIn()
                            .getHeader("size", Integer.class);

                    page = page == null ? 0 : page;
                    size = size == null ? 10 : size;

                    if (page < 0) {
                        throw new IllegalArgumentException(
                                "Page must be >= 0");
                    }

                    if (size < 1 || size > 100) {
                        throw new IllegalArgumentException(
                                "Size must be between 1 and 100");
                    }

                    List<UploadJob> jobs =
                            uploadJobService.findUploadHistory(page, size);

                    long totalRecords =
                            uploadJobService.countUploadJobs();

                    UploadHistoryResponse response =
                            new UploadHistoryResponse(
                                    jobs, page, size, totalRecords);

                    exchange.getMessage().setBody(response);
                })
                .marshal(uploadHistoryJsonFormat);



        from("direct:getEmployeeById")
                .routeId("get-employee-by-id-route")
                .process(exchange -> {
                    String id = exchange.getIn().getHeader("id", String.class);

                    UUID employeeId;
                    try {
                        employeeId = UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid employee ID");
                    }

                    Employee employee = employeeService.findById(employeeId);

                    if (employee == null) {
                        exchange.getMessage().setHeader(
                                Exchange.HTTP_RESPONSE_CODE, 404
                        );
                        exchange.getMessage().setBody(
                                "{\"message\":\"Employee not found\"}"
                        );
                        return;
                    }

                    exchange.getMessage().setBody(EmployeeResponse.from(employee));                })
                .choice()
                .when(body().isInstanceOf(EmployeeResponse.class))
                .marshal(employeeResponseJsonFormat)
                .end();
        from("direct:downloadErrorReport")
                .routeId("download-error-report-route")

                .log("Generating error report for job: ${header.jobId}")

                .process(exchange -> {

                    // Extract job ID
                    String jobIdValue = exchange.getIn()
                            .getHeader("jobId", String.class);

                    UUID jobId = UUID.fromString(jobIdValue);

                    // Verify upload job exists
                    UploadJob job = uploadJobService.getJob(jobId);

                    if (job == null) {
                        exchange.getMessage().setHeader(
                                Exchange.HTTP_RESPONSE_CODE, 404);

                        exchange.getMessage().setBody(
                                "Upload job not found: " + jobId);

                        return;
                    }

                    // Generate CSV report
                    String report =
                            errorReportService.generateErrorReport(jobId);

                    // Configure download response
                    exchange.getMessage().setBody(report);

                    exchange.getMessage().setHeader(
                            Exchange.CONTENT_TYPE, "text/csv");

                    exchange.getMessage().setHeader(
                            Exchange.HTTP_RESPONSE_CODE, 200);

                    exchange.getMessage().setHeader(
                            "Content-Disposition",
                            "attachment; filename=\"error-report-" +
                                    jobId + ".csv\"");

                    log.info("Error report generated for job: {}", jobId);
                });



        from("direct:getUploadStatus")
                .routeId("get-upload-status-route")

                .log("Fetching upload status for job: ${header.jobId}")

                .process(exchange -> {

                    // Extract job ID from REST path parameter
                    String jobIdValue = exchange.getIn()
                            .getHeader("jobId", String.class);

                    UUID jobId = UUID.fromString(jobIdValue);

                    // Retrieve upload job
                    UploadJob job = uploadJobService.getJob(jobId);

                    if (job == null) {

                        exchange.getMessage().setHeader(
                                Exchange.HTTP_RESPONSE_CODE, 404);

                        exchange.getMessage().setBody(
                                new ApiError(
                                        404,
                                        "Not Found",
                                        "Upload job not found: " + jobId
                                )
                        );

                    } else {

                        UploadStatusResponse response =
                                new UploadStatusResponse(job);

                        exchange.getMessage().setHeader(
                                Exchange.HTTP_RESPONSE_CODE, 200);

                        exchange.getMessage().setBody(response);
                    }
                })

                .marshal().json();




        from("direct:uploadCsv")
                .routeId("upload-csv-route")

                .log("Received CSV upload request")

                .process(exchange -> {

                    // 1. Extract uploaded file
                    File uploadedFile = exchange.getIn().getBody(File.class);

                    if (uploadedFile == null || !uploadedFile.isFile()) {
                        throw new IllegalArgumentException(
                                "Please upload a valid CSV file");
                    }

                    // 2. Get original filename
                    String originalFileName =
                            exchange.getIn().getHeader("CamelFileName", String.class);

                    if (originalFileName == null || originalFileName.isBlank()) {
                        throw new IllegalArgumentException(
                                "Uploaded filename is missing");
                    }

                    // Sanitize filename
                    originalFileName = originalFileName.replace("\\", "/");

                    originalFileName = originalFileName.substring(
                            originalFileName.lastIndexOf('/') + 1);

                    if (!originalFileName.toLowerCase().endsWith(".csv")) {
                        throw new IllegalArgumentException(
                                "Only CSV files are allowed");
                    }

                    // 3. Validate file size
                    if (uploadedFile.length() == 0) {
                        throw new IllegalArgumentException(
                                "Uploaded CSV file is empty");
                    }

                    long maxFileSize = 20 * 1024 * 1024; // 20 MB

                    if (uploadedFile.length() > maxFileSize) {
                        throw new IllegalArgumentException(
                                "CSV file size cannot exceed 20 MB");
                    }

                    // 4. Create persistent upload directory
                    Path uploadDirectory = Path.of("uploads")
                            .toAbsolutePath()
                            .normalize();

                    Files.createDirectories(uploadDirectory);

                    // 5. Generate unique storage filename
                    String storedFileName = UUID.randomUUID() + ".csv";

                    Path destination = uploadDirectory
                            .resolve(storedFileName)
                            .normalize();

                    // 6. Copy temporary file to persistent storage
                    Files.copy(
                            uploadedFile.toPath(),
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    // 7. Create UploadJob in PostgreSQL
                    UploadJob job = uploadJobRepository.createJob(
                            originalFileName,
                            destination.toString()
                    );

                    // 8. Prepare API response
                    UploadResponse response = new UploadResponse(
                            job.id,
                            job.fileName,
                            job.status,
                            "File uploaded successfully. Processing will start shortly."
                    );

                    // Store response separately
                    exchange.setProperty("uploadResponse", response);

                    // Set job UUID as message body for SEDA
                    exchange.getMessage().setBody(job.id);

                    log.info("Upload job created: {}", job.id);

                })

                // 9. Send job ID to asynchronous SEDA consumer
                .to("seda:employee-upload?waitForTaskToComplete=Never")

                // 10. Restore original response
                .setBody(exchangeProperty("uploadResponse"))

                // 11. Convert response to JSON
                .marshal().json(JsonLibrary.Jackson)

                // 12. Return HTTP 202
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(202))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))

                .log("Upload request accepted successfully");




        from("seda:employee-upload?concurrentConsumers=2")
                .routeId("employee-upload-processing-route")

                .log("Received upload job: ${body}")

                .doTry()

                // The producer sends UUID, not UploadJob
                .process(exchange -> {
                    UUID jobId = exchange.getIn().getBody(UUID.class);
                    exchange.setProperty("uploadJobId", jobId);
                })

                // UUID is passed to markProcessing()
                // Repository returns the UploadJob entity
                .bean(uploadJobRepository, "markProcessing")

                .log("Job status updated to PROCESSING")

                .log("File to process: ${body.filePath}")

                // Body is now UploadJob
                .to("direct:parseCsv")

                .doCatch(Exception.class)

                .process(exchange -> {
                    UUID jobId = exchange.getProperty(
                            "uploadJobId", UUID.class);

                    Exception exception = exchange.getProperty(
                            Exchange.EXCEPTION_CAUGHT, Exception.class);

                    String message = exception != null
                            ? exception.getMessage()
                            : "Unknown processing error";

                    uploadJobRepository.markFailed(jobId, message);

                    log.error("Upload job {} failed: {}", jobId, message);
                })

                .end();




        from("direct:parseCsv")
                .routeId("csv-parse-route")

                .log("Starting CSV parsing for job: ${body.id}")

                // 1. Preserve job information and read CSV file
                .process(exchange -> {

                    UploadJob job = exchange.getIn().getBody(UploadJob.class);

                    exchange.setProperty("uploadJobId", job.id);
                    exchange.setProperty("uploadFileName", job.fileName);

                    exchange.getMessage().setBody(
                            Files.newInputStream(Path.of(job.filePath))
                    );
                })

                // 2. Convert CSV into List<Map<String, String>>
                .unmarshal(csvDataFormat)

                // 3. Update total row count
                .process(exchange -> {

                    List<?> records = exchange.getMessage().getBody(List.class);

                    UUID jobId = exchange.getProperty(
                            "uploadJobId", UUID.class);

                    uploadJobRepository.setTotalRows(jobId, records.size());

                    log.info("Total CSV records found: {}", records.size());
                })

                .log("CSV parsing completed for job: ${exchangeProperty.uploadJobId}")

                // 4. Split and process each CSV record
                .split(body())
                .streaming()

                // Calculate original CSV row number
                .process(exchange -> {

                    Integer splitIndex = exchange.getProperty(
                            "CamelSplitIndex", Integer.class);

                    int inputRowNumber = splitIndex + 2;

                    exchange.setProperty(
                            "inputRowNumber",
                            inputRowNumber
                    );
                })

                // 5. Validate and persist individual employee
                .to("direct:validateEmployeeRow")

                .end()

                // 6. All records processed
                .process(exchange -> {

                    UUID jobId = exchange.getProperty(
                            "uploadJobId", UUID.class);

                    uploadJobRepository.completeJob(jobId);

                    log.info("Upload job completed: {}", jobId);
                });

        from("direct:validateEmployeeRow")
                .routeId("validate-employee-row-route")

                .process(exchange -> {

                    // 1. Extract CSV record
                    @SuppressWarnings("unchecked")
                    Map<String, String> csvMap =
                            exchange.getIn().getBody(Map.class);

                    // 2. Convert to DTO
                    CsvRow csvRow = CsvRow.from(csvMap);
                    log.info("CSV MAP: {}", csvMap);
                    log.info("CSV DTO: {}", csvRow);

                    // 3. Validate
                    List<String> errors =
                            employeeValidator.validate(csvRow);

                    // 4. Convert only valid records
                    Employee employee = null;

                    if (errors.isEmpty()) {
                        employee = employeeProcessor.toEmployee(csvRow);
                    }

                    // 5. Get job and original CSV row number
                    UUID jobId = exchange.getProperty(
                            "uploadJobId", UUID.class);

                    Integer rowNumber = exchange.getProperty(
                            "inputRowNumber", Integer.class);

                    // 6. Persist result and update counters
                    String outcome = employeeImportService.processRow(
                            jobId,
                            rowNumber,
                            csvRow,
                            employee,
                            errors
                    );

                    // 7. Store outcome for logging
                    exchange.setProperty("rowOutcome", outcome);
                    exchange.setProperty("validationErrors", errors);

                    exchange.getMessage().setBody(outcome);
                })

                .choice()

                .when(exchangeProperty("rowOutcome").isEqualTo("INSERTED"))
                .log("EMPLOYEE INSERTED - Row ${exchangeProperty.inputRowNumber}")

                .when(exchangeProperty("rowOutcome").isEqualTo("DUPLICATE"))
                .log("DUPLICATE EMPLOYEE - Row ${exchangeProperty.inputRowNumber}")

                .otherwise()
                .log("REJECTED ROW ${exchangeProperty.inputRowNumber}: ${exchangeProperty.validationErrors}")

                .end();





        from("direct:createEmployee")
                .routeId("create-employee-route")
                .log("Received employee creation request")
                // Convert JSON request body into Employee object
                .unmarshal(employeeJsonFormat)
                .log("Employee received: ${body.firstName} ${body.lastName}")
                // Invoke business service
                .bean(employeeService, "create")
                // Serialize Employee response into JSON
                .marshal(employeeJsonFormat)                // HTTP response code
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
                .log("Employee created successfully");


        // --------------------------------------------------
        // 5. GET ALL EMPLOYEES ROUTE
        // --------------------------------------------------

        from("direct:getEmployees")
                .routeId("get-employees-route")
                .process(exchange -> {

                    Integer page = exchange.getIn().getHeader("page", Integer.class);
                    Integer size = exchange.getIn().getHeader("size", Integer.class);

                    page = page == null ? 0 : page;
                    size = size == null ? 20 : size;

                    if (page < 0) {
                        throw new IllegalArgumentException("Page must be >= 0");
                    }

                    if (size < 1 || size > 100) {
                        throw new IllegalArgumentException("Size must be between 1 and 100");
                    }

                    List<Employee> employees =
                            employeeService.findEmployees(page, size);

                    long totalRecords = employeeService.countEmployees();

                    EmployeePageResponse response =
                            new EmployeePageResponse(employees, page, size, totalRecords);

                    exchange.getMessage().setBody(response);
                })
                .marshal(employeePageJsonFormat)
                .log("Employees retrieved successfully");
    }
}
