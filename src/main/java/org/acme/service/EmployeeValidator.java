package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.dto.CsvRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@ApplicationScoped
public class EmployeeValidator {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern MOBILE_PATTERN =
            Pattern.compile("\\d{10}");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public List<String> validate(CsvRow row) {

        List<String> errors = new ArrayList<>();

        // First name
        if (isBlank(row.firstName())) {
            errors.add("First name is mandatory");
        }

        // Last name
        if (isBlank(row.lastName())) {
            errors.add("Last name is mandatory");
        }

        // Date of birth
        if (!isValidDate(row.dob())) {
            errors.add("DOB must be a valid date in DD-MM-YYYY format");
        }

        // Gender
        if (isBlank(row.gender()) ||
                !List.of("M", "F").contains(row.gender().toUpperCase(Locale.ROOT))) {
            errors.add("Gender must be M or F");
        }

        // Mobile
        if (isBlank(row.mobile()) ||
                !MOBILE_PATTERN.matcher(row.mobile()).matches()) {
            errors.add("Mobile must contain exactly 10 digits");
        }

        // Email is optional
        if (!isBlank(row.email()) &&
                !EMAIL_PATTERN.matcher(row.email()).matches()) {
            errors.add("Email format is invalid");
        }

        // Department
        if (isBlank(row.department())) {
            errors.add("Department is mandatory");
        }

        // Designation
        if (isBlank(row.designation())) {
            errors.add("Designation is mandatory");
        }

        // Date of joining
        if (!isValidDate(row.dateOfJoining())) {
            errors.add("Date of joining must be a valid date in DD-MM-YYYY format");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidDate(String value) {

        if (isBlank(value)) {
            return false;
        }

        try {
            LocalDate.parse(value, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }
}