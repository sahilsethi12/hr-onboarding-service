package org.acme.repository;

import org.acme.entity.UploadRowResult;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UploadRowResultRepository
        implements PanacheRepositoryBase<UploadRowResult, UUID> {

    public List<UploadRowResult> findRejectedRows(UUID jobId) {
        return list(
                "jobId = ?1 AND resultStatus = ?2 ORDER BY inputRowNumber",
                jobId,
                "REJECTED"
        );
    }
}