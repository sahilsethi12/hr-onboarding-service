package org.acme.repository;

import org.acme.entity.Employee;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class EmployeeRepository
        implements PanacheRepositoryBase<Employee, UUID> {

    public Employee findByIdentityKey(String identityKey) {
        return find("identityKey", identityKey).firstResult();
    }

    public boolean existsByIdentityKey(String identityKey) {
        return count("identityKey", identityKey) > 0;
    }

    public List<Employee> findEmployees(int page, int size) {
        return findAll()
                .page(page, size)
                .list();
    }

    public long countEmployees() {
        return count();
    }
}