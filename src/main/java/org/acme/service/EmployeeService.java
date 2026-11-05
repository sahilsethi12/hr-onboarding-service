
package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.acme.entity.Employee;
import org.acme.exception.DuplicateEmployeeException;
import org.acme.repository.EmployeeRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class EmployeeService {

    @Inject
    EmployeeRepository employeeRepository;

    @Transactional
    public Employee create(Employee employee) {

        employee.identityKey = buildIdentityKey(employee);

        if (employeeRepository.existsByIdentityKey(employee.identityKey)) {
            throw new DuplicateEmployeeException(
                    "Employee already exists with the same identity details"
            );
        }

        employeeRepository.persist(employee);

        return employee;
    }

    @Transactional
    public List<Employee> findAll() {
        return employeeRepository.listAll();
    }

    @Transactional
    public Employee findById(UUID id) {
        return employeeRepository.findById(id);
    }

    @Transactional
    public List<Employee> findEmployees(int page, int size) {
        return employeeRepository.findEmployees(page, size);
    }

    @Transactional
    public long countEmployees() {
        return employeeRepository.countEmployees();
    }

    public long count() {
        return employeeRepository.count();
    }

    private String buildIdentityKey(Employee employee) {
        return String.join("|",
                employee.firstName.trim().toLowerCase(Locale.ROOT),
                employee.lastName.trim().toLowerCase(Locale.ROOT),
                employee.dob.toString(),
                employee.mobile.trim()
        );
    }
    @Transactional
    public boolean createIfNotExists(Employee employee) {

        employee.identityKey = String.join("|",
                employee.firstName.trim().toLowerCase(Locale.ROOT),
                employee.lastName.trim().toLowerCase(Locale.ROOT),
                employee.dob.toString(),
                employee.mobile.trim()
        );

        if (employeeRepository.existsByIdentityKey(employee.identityKey)) {
            return false;
        }

        employeeRepository.persistAndFlush(employee);
        return true;
    }
}
