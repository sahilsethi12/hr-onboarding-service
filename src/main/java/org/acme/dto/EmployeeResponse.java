package org.acme.dto;

import org.acme.entity.Employee;

import java.time.LocalDate;
import java.util.UUID;

public class EmployeeResponse {

    public UUID id;
    public String firstName;
    public String lastName;
    public LocalDate dob;
    public String gender;
    public String mobile;
    public String email;
    public String department;
    public String designation;
    public LocalDate dateOfJoining;

    public static EmployeeResponse from(Employee employee) {

        EmployeeResponse response = new EmployeeResponse();

        response.id = employee.id;
        response.firstName = employee.firstName;
        response.lastName = employee.lastName;
        response.dob = employee.dob;
        response.gender = employee.gender;
        response.mobile = employee.mobile;
        response.email = employee.email;
        response.department = employee.department;
        response.designation = employee.designation;
        response.dateOfJoining = employee.dateOfJoining;

        return response;
    }
}