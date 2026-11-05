
package org.acme.service;

import org.acme.entity.UploadJob;
import org.acme.repository.UploadJobRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UploadJobService {

    @Inject
    UploadJobRepository uploadJobRepository;

    @Transactional
    public UploadJob getJob(UUID jobId) {
        return uploadJobRepository.findById(jobId);
    }
    @Transactional
    public List<UploadJob> findUploadHistory(int page, int size) {
        return uploadJobRepository.findUploadHistory(page, size);
    }

    @Transactional
    public long countUploadJobs() {
        return uploadJobRepository.countUploadJobs();
    }
}
