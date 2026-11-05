
package org.acme.dto;

import org.acme.entity.UploadJob;
import org.acme.entity.UploadStatus;

import java.util.UUID;

public class UploadStatusResponse {

    public UUID jobId;
    public String fileName;
    public UploadStatus status;

    public int totalRows;
    public int processedRows;
    public int insertedRows;
    public int rejectedRows;
    public int duplicateRows;

    public String failureMessage;

    public UploadStatusResponse(UploadJob job) {

        this.jobId = job.id;
        this.fileName = job.fileName;
        this.status = job.status;

        this.totalRows = job.totalRows;
        this.processedRows = job.processedRows;
        this.insertedRows = job.insertedRows;
        this.rejectedRows = job.rejectedRows;
        this.duplicateRows = job.duplicateRows;

        this.failureMessage = job.failureMessage;
    }
}
