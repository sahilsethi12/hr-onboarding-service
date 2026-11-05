package org.acme.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import org.acme.entity.UploadJob;
import org.acme.entity.UploadStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UploadJobRepository
        implements PanacheRepositoryBase<UploadJob, UUID> {

    @Transactional
    public UploadJob createJob(String fileName, String filePath) {

        UploadJob job = new UploadJob();

        job.fileName = fileName;
        job.filePath = filePath;
        job.status = UploadStatus.UPLOADED;

        persistAndFlush(job);

        return job;
    }

    @Transactional
    public UploadJob markProcessing(UUID jobId) {

        UploadJob job = findById(jobId);

        if (job == null) {
            throw new IllegalArgumentException(
                    "Upload job not found: " + jobId);
        }

        job.status = UploadStatus.PROCESSING;
        job.startedAt = LocalDateTime.now();

        return job;
    }

    @Transactional
    public void setTotalRows(UUID jobId, int totalRows) {

        UploadJob job = findById(jobId);

        if (job == null) {
            throw new IllegalArgumentException(
                    "Upload job not found: " + jobId);
        }

        job.totalRows = totalRows;
    }

    @Transactional
    public void completeJob(UUID jobId) {

        UploadJob job = findById(jobId);

        if (job == null) {
            throw new IllegalArgumentException(
                    "Upload job not found: " + jobId);
        }

        job.completedAt = LocalDateTime.now();

        job.status = job.rejectedRows > 0
                ? UploadStatus.COMPLETED_WITH_ERRORS
                : UploadStatus.COMPLETED;
    }
    @Transactional
    public void markFailed(UUID jobId, String failureMessage) {

        UploadJob job = findById(jobId);

        if (job == null) {
            return;
        }

        job.status = UploadStatus.FAILED;
        job.failureMessage = failureMessage;
        job.completedAt = LocalDateTime.now();
    }

    @Transactional
    public List<UploadJob> findUploadHistory(int page, int size) {
        return find("order by createdAt desc")
                .page(page, size)
                .list();
    }

    public long countUploadJobs() {
        return count();
    }


}