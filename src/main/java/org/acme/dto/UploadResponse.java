package org.acme.dto;

import org.acme.entity.UploadStatus;

import java.util.UUID;

public class UploadResponse {

    public UUID jobId;
    public String fileName;
    public UploadStatus status;
    public String message;

    public UploadResponse(
            UUID jobId,
            String fileName,
            UploadStatus status,
            String message) {

        this.jobId = jobId;
        this.fileName = fileName;
        this.status = status;
        this.message = message;
    }
}