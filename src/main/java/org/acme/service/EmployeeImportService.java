
package org.acme.service;

import lombok.extern.slf4j.Slf4j;
import org.acme.dto.CsvRow;
import org.acme.entity.Employee;
import org.acme.entity.UploadJob;
import org.acme.entity.UploadRowResult;
import org.acme.repository.EmployeeRepository;
import org.acme.repository.UploadJobRepository;
import org.acme.repository.UploadRowResultRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class EmployeeImportService {

    @Inject
    EmployeeRepository employeeRepository;

    @Inject
    UploadJobRepository uploadJobRepository;

    @Inject
    UploadRowResultRepository rowResultRepository;

    @Transactional
    public String processRow(
            UUID jobId,
            int rowNumber,
            CsvRow csvRow,
            Employee employee,
            List<String> validationErrors) {

        UploadJob job = uploadJobRepository.findById(jobId);

        if (job == null) {
            throw new IllegalArgumentException(
                    "Upload job not found: " + jobId
            );
        }

        UploadRowResult result = new UploadRowResult();

        result.jobId = jobId;
        result.inputRowNumber = rowNumber;

        result.firstName = csvRow.firstName();
        result.lastName = csvRow.lastName();
        result.dob = csvRow.dob();
        result.gender = csvRow.gender();
        result.mobile = csvRow.mobile();
        result.email = csvRow.email();
        result.department = csvRow.department();
        result.designation = csvRow.designation();
        result.dateOfJoining = csvRow.dateOfJoining();

        String outcome;

        // 1. Validation failed
        if (validationErrors != null && !validationErrors.isEmpty()) {

            outcome = "REJECTED";
            result.errorDetails = String.join("; ", validationErrors);

            job.rejectedRows++;

        }
        // 2. Employee already exists
        else if (employeeRepository.existsByIdentityKey(employee.identityKey)) {

            outcome = "DUPLICATE";
            result.errorDetails = "Employee already exists";

            job.duplicateRows++;

        }
        // 3. Valid new employee
        else {

            employeeRepository.persistAndFlush(employee);

            outcome = "INSERTED";
            job.insertedRows++;
        }

        job.processedRows++;

        result.resultStatus = outcome;
        rowResultRepository.persist(result);

        log.info("ROW RESULT BEFORE SAVE: firstName={}, lastName={}, mobile={}, department={}",
                result.firstName,
                result.lastName,
                result.mobile,
                result.department);

        return outcome;
    }
}
