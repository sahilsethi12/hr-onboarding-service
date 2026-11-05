const API_BASE_URL = "/api";

// 1. Upload CSV file
async function uploadCsv(file) {

    const formData = new FormData();

    formData.append("file", file);

    const response = await fetch(`${API_BASE_URL}/upload`, {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "CSV upload failed");
    }

    return await response.json();
}


// 2. Get upload processing status
async function getUploadStatus(jobId) {

    const response = await fetch(
        `${API_BASE_URL}/uploads/${jobId}`
    );

    if (!response.ok) {
        throw new Error("Unable to fetch upload status");
    }

    return await response.json();
}


// 3. Download error report
async function downloadErrorReport(jobId) {

    const response = await fetch(
        `${API_BASE_URL}/uploads/${jobId}/errors`
    );

    if (!response.ok) {
        throw new Error("Unable to download error report");
    }

    const blob = await response.blob();

    const url = window.URL.createObjectURL(blob);

    const link = document.createElement("a");

    link.href = url;
    link.download = `error-report-${jobId}.csv`;

    document.body.appendChild(link);

    link.click();

    link.remove();

    window.URL.revokeObjectURL(url);
}


// 4. Get employees with pagination
async function getEmployees(page = 0, size = 20) {

    const response = await fetch(
        `${API_BASE_URL}/employees?page=${page}&size=${size}`
    );

    if (!response.ok) {
        throw new Error("Unable to fetch employees");
    }

    return await response.json();
}


// 5. Get employee by ID
async function getEmployeeById(id) {

    const response = await fetch(
        `${API_BASE_URL}/employees/${id}`
    );

    if (!response.ok) {
        throw new Error("Unable to fetch employee details");
    }

    return await response.json();
}

async function getUploadHistory(page = 0, size = 10) {

    const response = await fetch(
        `${API_BASE_URL}/uploads?page=${page}&size=${size}`
    );

    if (!response.ok) {
        throw new Error("Unable to fetch upload history");
    }

    return await response.json();
}