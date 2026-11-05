package org.acme.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "upload_jobs")
public class UploadJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "file_name", nullable = false)
    public String fileName;

    @Column(name = "file_path", nullable = false, length = 1000)
    public String filePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    public UploadStatus status;

    @Column(name = "total_rows")
    public int totalRows;

    @Column(name = "processed_rows")
    public int processedRows;

    @Column(name = "inserted_rows")
    public int insertedRows;

    @Column(name = "rejected_rows")
    public int rejectedRows;

    @Column(name = "duplicate_rows")
    public int duplicateRows;

    @Column(name = "failure_message", length = 2000)
    public String failureMessage;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;

    @Column(name = "started_at")
    public LocalDateTime startedAt;

    @Column(name = "completed_at")
    public LocalDateTime completedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();

        if (status == null) {
            status = UploadStatus.UPLOADED;
        }
    }
}