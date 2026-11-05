// Pagination state
let historyCurrentPage = 0;
const historyPageSize = 10;
let historyTotalPages = 0;

// HTML elements
const historyTableBody =
    document.getElementById("uploadHistoryTableBody");

const historyPreviousButton =
    document.getElementById("historyPreviousPage");

const historyNextButton =
    document.getElementById("historyNextPage");

const historyPageInfo =
    document.getElementById("historyPageInfo");


// Load upload history
async function loadUploadHistory() {

    historyTableBody.innerHTML = `
        <tr>
            <td colspan="8">Loading upload history...</td>
        </tr>
    `;

    try {

        const response = await getUploadHistory(
            historyCurrentPage,
            historyPageSize
        );

        historyTotalPages = response.totalPages || 0;

        renderUploadHistory(response.uploads || []);

        updateHistoryPagination(response);

    } catch (error) {

        console.error("Upload history error:", error);

        historyTableBody.innerHTML = `
            <tr>
                <td colspan="8">Unable to load upload history.</td>
            </tr>
        `;
    }
}


// Render upload history table
function renderUploadHistory(uploads) {

    historyTableBody.replaceChildren();

    if (uploads.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.textContent = "No upload history found.";

        row.appendChild(cell);
        historyTableBody.appendChild(row);

        return;
    }

    uploads.forEach(upload => {

        const row = document.createElement("tr");

        const values = [
            upload.fileName,
            formatUploadDate(upload.createdAt),
            upload.status,
            upload.totalRows,
            upload.insertedRows,
            upload.rejectedRows,
            upload.duplicateRows
        ];

        values.forEach((value, index) => {

            const cell = document.createElement("td");

            if (index === 2) {

                const statusBadge = document.createElement("span");

                statusBadge.className =
                    "status-badge " + getStatusClass(upload.status);

                statusBadge.textContent = value;

                cell.appendChild(statusBadge);

            } else {

                cell.textContent = value ?? "-";
            }

            row.appendChild(cell);
        });

        // Error report download action
        const actionCell = document.createElement("td");

        if (upload.rejectedRows > 0) {

            const downloadButton = document.createElement("button");

            downloadButton.textContent = "Download Errors";
            downloadButton.className = "history-download-button";

            downloadButton.addEventListener("click", async () => {

                try {
                    await downloadErrorReport(upload.jobId);
                } catch (error) {
                    alert(error.message);
                }

            });

            actionCell.appendChild(downloadButton);

        } else {

            actionCell.textContent = "-";
        }

        row.appendChild(actionCell);

        historyTableBody.appendChild(row);
    });
}


// Get CSS class based on job status
function getStatusClass(status) {

    switch (status) {

        case "COMPLETED":
            return "status-completed";

        case "COMPLETED_WITH_ERRORS":
            return "status-warning";

        case "PROCESSING":
            return "status-processing";

        case "UPLOADED":
            return "status-pending";

        case "FAILED":
            return "status-failed";

        default:
            return "";
    }
}


// Format upload date
function formatUploadDate(dateValue) {

    if (!dateValue) {
        return "-";
    }

    return new Date(dateValue).toLocaleString("en-IN");
}


// Update pagination
function updateHistoryPagination(response) {

    const totalRecords = response.totalRecords || 0;

    historyPageInfo.textContent =
        `Page ${historyTotalPages === 0 ? 0 : historyCurrentPage + 1} of ${historyTotalPages} (${totalRecords} uploads)`;

    historyPreviousButton.disabled =
        historyCurrentPage <= 0;

    historyNextButton.disabled =
        historyTotalPages === 0 ||
        historyCurrentPage >= historyTotalPages - 1;
}


// Previous page
historyPreviousButton.addEventListener("click", () => {

    if (historyCurrentPage > 0) {

        historyCurrentPage--;

        loadUploadHistory();
    }
});


// Next page
historyNextButton.addEventListener("click", () => {

    if (historyCurrentPage < historyTotalPages - 1) {

        historyCurrentPage++;

        loadUploadHistory();
    }
});


// Initialize history
function initializeUploadHistory() {
    loadUploadHistory();
}
