
package org.acme.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "first_name", nullable = false)
    public String firstName;

    @Column(name = "last_name", nullable = false)
    public String lastName;

    @Column(name = "dob", nullable = false)
    public LocalDate dob;

    @Column(name = "gender", nullable = false, length = 1)
    public String gender;

    @Column(name = "mobile", nullable = false, length = 10)
    public String mobile;

    @Column(name = "email")
    public String email;

    @Column(name = "department", nullable = false)
    public String department;

    @Column(name = "designation", nullable = false)
    public String designation;

    @Column(name = "date_of_joining", nullable = false)
    public LocalDate dateOfJoining;

    @Column(name = "identity_key", nullable = false, unique = true)
    public String identityKey;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
