package org.acme.dto;

import org.acme.entity.UploadJob;
import org.acme.entity.UploadStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UploadHistoryResponse {

    public List<UploadSummary> uploads;
    public int page;
    public int size;
    public long totalRecords;
    public long totalPages;

    public UploadHistoryResponse(
            List<UploadJob> jobs,
            int page,
            int size,
            long totalRecords) {

        this.uploads = jobs.stream()
                .map(UploadSummary::from)
                .toList();

        this.page = page;
        this.size = size;
        this.totalRecords = totalRecords;
        this.totalPages = (totalRecords + size - 1) / size;
    }

    public static class UploadSummary {

        public UUID jobId;
        public String fileName;
        public UploadStatus status;
        public int totalRows;
        public int processedRows;
        public int insertedRows;
        public int rejectedRows;
        public int duplicateRows;
        public String failureMessage;
        public LocalDateTime createdAt;

        public static UploadSummary from(UploadJob job) {

            UploadSummary summary = new UploadSummary();

            summary.jobId = job.id;
            summary.fileName = job.fileName;
            summary.status = job.status;
            summary.totalRows = job.totalRows;
            summary.processedRows = job.processedRows;
            summary.insertedRows = job.insertedRows;
            summary.rejectedRows = job.rejectedRows;
            summary.duplicateRows = job.duplicateRows;
            summary.failureMessage = job.failureMessage;
            summary.createdAt = job.createdAt;

            return summary;
        }
    }
}