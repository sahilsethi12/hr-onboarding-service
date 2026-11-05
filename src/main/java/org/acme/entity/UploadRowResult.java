
package org.acme.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "upload_row_results",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_job_row",
                        columnNames = {"job_id", "input_row_number"}
                )
        }
)
public class UploadRowResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "job_id", nullable = false)
    public UUID jobId;

    @Column(name = "input_row_number", nullable = false)
    public int inputRowNumber;

    @Column(name = "result_status", nullable = false)
    public String resultStatus;

    @Column(name = "error_details", columnDefinition = "TEXT")
    public String errorDetails;

    @Column(name = "processed_at", nullable = false)
    public LocalDateTime processedAt;

    @Column(name = "first_name", length = 100)
    public String firstName;

    @Column(name = "last_name", length = 100)
    public String lastName;

    @Column(name = "dob", length = 20)
    public String dob;

    @Column(name = "gender", length = 1)
    public String gender;

    @Column(name = "mobile", length = 10)
    public String mobile;

    @Column(name = "email", length = 255)
    public String email;

    @Column(name = "department", length = 150)
    public String department;

    @Column(name = "designation", length = 150)
    public String designation;

    @Column(name = "date_of_joining", length = 20)
    public String dateOfJoining;

    @PrePersist
    void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
