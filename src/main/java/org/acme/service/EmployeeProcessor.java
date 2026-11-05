package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.dto.CsvRow;
import org.acme.entity.Employee;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

@ApplicationScoped
public class EmployeeProcessor {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public Employee toEmployee(CsvRow row) {

        Employee employee = new Employee();

        employee.firstName = row.firstName().trim();
        employee.lastName = row.lastName().trim();

        employee.dob = LocalDate.parse(row.dob(), DATE_FORMATTER);

        employee.gender = row.gender().trim().toUpperCase(Locale.ROOT);
        employee.mobile = row.mobile().trim();

        employee.email = row.email() == null || row.email().isBlank()
                ? null
                : row.email().trim();

        employee.department = row.department().trim();
        employee.designation = row.designation().trim();

        employee.dateOfJoining =
                LocalDate.parse(row.dateOfJoining(), DATE_FORMATTER);

        // Generate normalized identity key for duplicate detection
        employee.identityKey = String.join("|",
                employee.firstName.toLowerCase(Locale.ROOT),
                employee.lastName.toLowerCase(Locale.ROOT),
                employee.dob.toString(),
                employee.mobile
        );

        return employee;
    }
}