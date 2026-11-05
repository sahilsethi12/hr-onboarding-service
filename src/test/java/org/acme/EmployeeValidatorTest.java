
package org.acme;

import org.acme.dto.CsvRow;
import org.acme.service.EmployeeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeValidatorTest {

    private EmployeeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmployeeValidator();
    }

    private CsvRow validRow() {
        return new CsvRow(
                "Sahil",
                "Sharma",
                "15-06-1992",
                "M",
                "9876543210",
                "sahil@example.com",
                "IT",
                "Software Engineer",
                "01-01-2020"
        );
    }

    @Test
    void shouldAcceptValidEmployee() {
        List<String> errors = validator.validate(validRow());

        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldRejectMissingFirstName() {
        CsvRow row = new CsvRow(
                "", "Sharma", "15-06-1992", "M",
                "9876543210", "sahil@example.com",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains("First name is mandatory"));
    }

    @Test
    void shouldRejectInvalidDateOfBirth() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "31-02-1990", "M",
                "9876543210", "sahil@example.com",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains(
                "DOB must be a valid date in DD-MM-YYYY format"
        ));
    }

    @Test
    void shouldRejectInvalidGender() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "Other",
                "9876543210", "sahil@example.com",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains("Gender must be M or F"));
    }

    @Test
    void shouldRejectInvalidMobileNumber() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "M",
                "987654", "sahil@example.com",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains(
                "Mobile must contain exactly 10 digits"
        ));
    }

    @Test
    void shouldAcceptBlankOptionalEmail() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "M",
                "9876543210", "",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "M",
                "9876543210", "invalid-email",
                "IT", "Engineer", "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains("Email format is invalid"));
    }

    @Test
    void shouldRejectMissingDepartmentAndDesignation() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "M",
                "9876543210", "", "", "",
                "01-01-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains("Department is mandatory"));
        assertTrue(errors.contains("Designation is mandatory"));
    }

    @Test
    void shouldRejectInvalidDateOfJoining() {
        CsvRow row = new CsvRow(
                "Sahil", "Sharma", "15-06-1992", "M",
                "9876543210", "", "IT", "Engineer",
                "31-02-2020"
        );

        List<String> errors = validator.validate(row);

        assertTrue(errors.contains(
                "Date of joining must be a valid date in DD-MM-YYYY format"
        ));
    }

    @Test
    void shouldReturnAllValidationErrorsForInvalidRow() {
        CsvRow row = new CsvRow(
                "", "", "31-02-1990", "X",
                "123", "invalid-email", "", "",
                "invalid-date"
        );

        List<String> errors = validator.validate(row);

        assertEquals(9, errors.size());
    }
}
