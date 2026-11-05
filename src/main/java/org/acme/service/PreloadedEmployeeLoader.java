package org.acme.service;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.dto.CsvRow;
import org.acme.entity.Employee;
import org.acme.service.EmployeeProcessor;
import org.acme.service.EmployeeValidator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.jboss.logging.Logger;

@ApplicationScoped
public class PreloadedEmployeeLoader {

    private static final Logger LOG =
            Logger.getLogger(PreloadedEmployeeLoader.class);

    @Inject
    EmployeeService employeeService;

    @Inject
    EmployeeValidator employeeValidator;

    @Inject
    EmployeeProcessor employeeProcessor;

    void onStart(@Observes StartupEvent event) {

        LOG.info("Starting preloaded employee initialization");

        try (InputStream inputStream =
                     Thread.currentThread()
                             .getContextClassLoader()
                             .getResourceAsStream(
                                     "data/preloaded-employees.csv")) {

            if (inputStream == null) {
                throw new IllegalStateException(
                        "preloaded-employees.csv not found");
            }

            try (BufferedReader reader =
                         new BufferedReader(
                                 new InputStreamReader(inputStream))) {

                CSVFormat format = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .withTrim();

                try (CSVParser parser = format.parse(reader)) {

                    int inserted = 0;
                    int skipped = 0;

                    for (CSVRecord record : parser) {

                        CsvRow csvRow = new CsvRow(
                                record.get("first_name"),
                                record.get("last_name"),
                                record.get("dob"),
                                record.get("gender"),
                                record.get("mobile"),
                                record.get("email"),
                                record.get("department"),
                                record.get("designation"),
                                record.get("date_of_joining")
                        );

                        List<String> errors =
                                employeeValidator.validate(csvRow);

                        if (!errors.isEmpty()) {
                            throw new IllegalStateException(
                                    "Invalid preloaded CSV record "
                                            + record.getRecordNumber()
                                            + ": "
                                            + String.join("; ", errors));
                        }

                        Employee employee =
                                employeeProcessor.toEmployee(csvRow);

                        boolean created =
                                employeeService.createIfNotExists(employee);

                        if (created) {
                            inserted++;
                        } else {
                            skipped++;
                        }
                    }

                    LOG.infof(
                            "Preloaded employees initialized. Inserted: %d, Skipped: %d",
                            inserted, skipped
                    );
                }
            }

        } catch (Exception e) {
            LOG.error("Failed to load preloaded employees", e);
            throw new IllegalStateException(
                    "Preloaded employee initialization failed", e);
        }
    }
}