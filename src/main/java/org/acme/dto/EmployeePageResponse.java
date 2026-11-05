package org.acme.dto;

import org.acme.entity.Employee;
import java.util.List;

public class EmployeePageResponse {

    public List<EmployeeResponse> employees;
    public int page;
    public int size;
    public long totalRecords;
    public long totalPages;

    public EmployeePageResponse(
            List<Employee> employees,
            int page,
            int size,
            long totalRecords) {

        this.employees = employees.stream()
                .map(EmployeeResponse::from)
                .toList();

        this.page = page;
        this.size = size;
        this.totalRecords = totalRecords;
        this.totalPages = (totalRecords + size - 1) / size;
    }
}