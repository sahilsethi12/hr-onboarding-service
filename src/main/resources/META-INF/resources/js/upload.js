// Get HTML elements
const uploadForm = document.getElementById("uploadForm");
const csvFileInput = document.getElementById("csvFile");
const uploadButton = document.getElementById("uploadButton");

const selectedFileName = document.getElementById("selectedFileName");
const uploadStatusSection = document.getElementById("uploadStatusSection");

const jobFileName = document.getElementById("jobFileName");
const jobStatus = document.getElementById("jobStatus");

const progressBar = document.getElementById("progressBar");
const progressText = document.getElementById("progressText");

const totalRows = document.getElementById("totalRows");
const processedRows = document.getElementById("processedRows");
const insertedRows = document.getElementById("insertedRows");
const rejectedRows = document.getElementById("rejectedRows");
const duplicateRows = document.getElementById("duplicateRows");

const downloadErrorsButton =
    document.getElementById("downloadErrorsButton");

const uploadMessage = document.getElementById("uploadMessage");


// Keep track of the current upload job
let currentJobId = null;
let pollingTimer = null;


// Display selected filename
csvFileInput.addEventListener("change", function () {

    const file = csvFileInput.files[0];

    selectedFileName.textContent = file
        ? file.name
        : "No file selected";
});


// Handle CSV upload
uploadForm.addEventListener("submit", async function (event) {

    event.preventDefault();

    const file = csvFileInput.files[0];

    if (!file) {
        alert("Please select a CSV file.");
        return;
    }

    if (!file.name.toLowerCase().endsWith(".csv")) {
        alert("Only CSV files are supported.");
        return;
    }

    // Stop any previous polling
    stopPolling();

    uploadButton.disabled = true;
    uploadButton.textContent = "Uploading...";

    uploadMessage.textContent = "";

    downloadErrorsButton.classList.add("hidden");

    uploadStatusSection.classList.remove("hidden");

    resetUploadStatistics();

    try {

        // Call backend upload API
        const result = await uploadCsv(file);

        currentJobId = result.jobId;

        jobFileName.textContent = "File: " + result.fileName;

        jobStatus.textContent = "Status: " + result.status;

        uploadMessage.textContent =
            result.message || "Upload accepted successfully.";

        // Start polling for processing status
        startPolling(currentJobId);

    } catch (error) {

        uploadMessage.textContent = error.message;

        uploadMessage.style.color = "red";

    } finally {

        uploadButton.disabled = false;
        uploadButton.textContent = "Upload Employees";
    }
});


// Start polling
function startPolling(jobId) {

    stopPolling();

    // Fetch immediately
    pollUploadStatus(jobId);

    // Then check every 2 seconds
    pollingTimer = setInterval(function () {
        pollUploadStatus(jobId);
    }, 2000);
}


// Fetch latest job status
async function pollUploadStatus(jobId) {

    try {

        const job = await getUploadStatus(jobId);

        updateUploadDashboard(job);

        const terminalStatuses = [
            "COMPLETED",
            "COMPLETED_WITH_ERRORS",
            "FAILED"
        ];

        // Stop polling when processing finishes
        if (terminalStatuses.includes(job.status)) {

            stopPolling();

            if (job.status === "COMPLETED") {
                uploadMessage.textContent =
                    "All employee records processed successfully.";
                uploadMessage.style.color = "green";
            }

            if (job.status === "COMPLETED_WITH_ERRORS") {
                uploadMessage.textContent =
                    "Processing completed. Some rows were rejected.";
                uploadMessage.style.color = "#b45309";
            }

            if (job.status === "FAILED") {
                uploadMessage.textContent =
                    job.failureMessage || "Upload processing failed.";
                uploadMessage.style.color = "red";
            }

            return;
        }

    } catch (error) {

        console.error("Status polling failed:", error);

        uploadMessage.textContent =
            "Unable to retrieve processing status. Retrying...";

        uploadMessage.style.color = "red";
    }
}


// Update dashboard using backend response
function updateUploadDashboard(job) {

    jobStatus.textContent = "Status: " + job.status;

    totalRows.textContent = job.totalRows || 0;

    processedRows.textContent = job.processedRows || 0;

    insertedRows.textContent = job.insertedRows || 0;

    rejectedRows.textContent = job.rejectedRows || 0;

    duplicateRows.textContent = job.duplicateRows || 0;

    const total = job.totalRows || 0;
    const processed = job.processedRows || 0;

    const percentage = total > 0
        ? Math.round((processed / total) * 100)
        : 0;

    progressBar.style.width = percentage + "%";

    progressText.textContent =
        percentage + "% processed (" + processed + "/" + total + ")";

    // Show error report button only when rows are rejected
    if (job.rejectedRows > 0) {
        downloadErrorsButton.classList.remove("hidden");
    } else {
        downloadErrorsButton.classList.add("hidden");
    }
}


// Download error report
downloadErrorsButton.addEventListener("click", async function () {

    if (!currentJobId) {
        return;
    }

    try {

        await downloadErrorReport(currentJobId);

    } catch (error) {

        alert(error.message);
    }
});


// Reset counters
function resetUploadStatistics() {

    totalRows.textContent = "0";
    processedRows.textContent = "0";
    insertedRows.textContent = "0";
    rejectedRows.textContent = "0";
    duplicateRows.textContent = "0";

    progressBar.style.width = "0%";
    progressText.textContent = "0% processed";

}


// Stop polling
function stopPolling() {

    if (pollingTimer !== null) {

        clearInterval(pollingTimer);

        pollingTimer = null;
    }
}

function handleBulkUploadPageOpened() {

    // If an upload is still processing, preserve its progress
    if (pollingTimer !== null) {
        uploadStatusSection.classList.remove("hidden");
        return;
    }

    // Previous upload is finished; prepare a fresh screen
    uploadStatusSection.classList.add("hidden");

    uploadForm.reset();

    selectedFileName.textContent = "No file selected";

    uploadMessage.textContent = "";
    uploadMessage.style.color = "";

    currentJobId = null;

    resetUploadStatistics();

    jobStatus.textContent = "Status: Waiting";
    jobFileName.textContent = "File: -";

    downloadErrorsButton.classList.add("hidden");
}
