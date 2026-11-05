
package org.acme.service;

import org.acme.entity.UploadRowResult;
import org.acme.repository.UploadRowResultRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ErrorReportService {

    @Inject
    UploadRowResultRepository rowResultRepository;


    @Transactional
    public String generateErrorReport(UUID jobId) {

        List<UploadRowResult> rejectedRows =
                rowResultRepository.findRejectedRows(jobId);

        StringBuilder csv = new StringBuilder();

        // CSV header
        csv.append(
                "row_number,first_name,last_name,dob,gender,mobile,email,department,designation,date_of_joining,error_details\n"
        );

        for (UploadRowResult row : rejectedRows) {

            csv.append(row.inputRowNumber).append(",");
            csv.append(escapeCsv(row.firstName)).append(",");
            csv.append(escapeCsv(row.lastName)).append(",");
            csv.append(escapeCsv(row.dob)).append(",");
            csv.append(escapeCsv(row.gender)).append(",");
            csv.append(escapeCsv(row.mobile)).append(",");
            csv.append(escapeCsv(row.email)).append(",");
            csv.append(escapeCsv(row.department)).append(",");
            csv.append(escapeCsv(row.designation)).append(",");
            csv.append(escapeCsv(row.dateOfJoining)).append(",");
            csv.append(escapeCsv(row.errorDetails)).append("\n");
        }

        return csv.toString();
    }

    private String escapeCsv(String value) {

        if (value == null) {
            return "";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

}
