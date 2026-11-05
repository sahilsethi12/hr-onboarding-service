package org.acme.dto;

import java.util.Map;

public record CsvRow(
        String firstName,
        String lastName,
        String dob,
        String gender,
        String mobile,
        String email,
        String department,
        String designation,
        String dateOfJoining
) {

    public static CsvRow from(Map<String, String> row) {

        return new CsvRow(
                value(row, "first_name"),
                value(row, "last_name"),
                value(row, "dob"),
                value(row, "gender"),
                value(row, "mobile"),
                value(row, "email"),
                value(row, "department"),
                value(row, "designation"),
                value(row, "date_of_joining")
        );
    }

    private static String value(Map<String, String> row, String key) {
        String value = row.get(key);
        return value == null ? null : value.trim();
    }
}