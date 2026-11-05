
async function loadDashboard() {
    try {
        const [employees, uploads] = await Promise.all([
            fetchAllEmployees(),
            fetchAllUploads()
        ]);

        renderDashboardEmployees(employees);
        renderDashboardUploads(uploads);
        renderDashboardDepartments(employees);

    } catch (error) {
        console.error("Dashboard loading failed:", error);

        document.getElementById("dashboardRecentUploads").textContent =
            "Unable to load dashboard data. Please try again.";
    }
}

async function fetchAllEmployees() {
    const firstPage = await getEmployees(0, 100);

    const employees = [...(firstPage.employees || [])];

    const totalPages = firstPage.totalPages || 1;

    for (let page = 1; page < totalPages; page++) {
        const result = await getEmployees(page, 100);
        employees.push(...(result.employees || []));
    }

    return employees;
}

async function fetchAllUploads() {
    const firstPage = await getUploadHistory(0, 100);

    const uploads = [...(firstPage.uploads || [])];

    const totalPages = firstPage.totalPages || 1;

    for (let page = 1; page < totalPages; page++) {
        const result = await getUploadHistory(page, 100);
        uploads.push(...(result.uploads || []));
    }

    return uploads;
}

function renderDashboardEmployees(employees) {
    document.getElementById("dashboardEmployees").textContent =
        employees.length;
}

function renderDashboardUploads(uploads) {
    const totals = uploads.reduce((result, upload) => {
        result.inserted += upload.insertedRows || 0;
        result.rejected += upload.rejectedRows || 0;
        result.duplicates += upload.duplicateRows || 0;
        result.processed += upload.processedRows || 0;
        result.total += upload.totalRows || 0;
        return result;
    }, {
        inserted: 0,
        rejected: 0,
        duplicates: 0,
        processed: 0,
        total: 0
    });

    document.getElementById("dashboardInserted").textContent =
        totals.inserted;

    document.getElementById("dashboardRejected").textContent =
        totals.rejected;

    document.getElementById("dashboardDuplicates").textContent =
        totals.duplicates;

    const completedRecords =
        totals.inserted + totals.rejected + totals.duplicates;

    const successRate = completedRecords === 0
        ? 0
        : Math.round((totals.inserted / completedRecords) * 100);

    document.getElementById("dashboardSuccessRate").textContent =
        successRate + "%";

    document.getElementById("dashboardSuccessBar").style.width =
        successRate + "%";

    document.getElementById("dashboardSuccess").textContent =
        totals.inserted;

    document.getElementById("dashboardRejectedLegend").textContent =
        totals.rejected;

    document.getElementById("dashboardDuplicatesLegend").textContent =
        totals.duplicates;

    renderRecentUploads(uploads);
}

function renderRecentUploads(uploads) {
    const container =
        document.getElementById("dashboardRecentUploads");

    container.replaceChildren();

    if (!uploads.length) {
        container.textContent = "No uploads available yet.";
        return;
    }

    uploads
        .sort((a, b) =>
            new Date(b.createdAt) - new Date(a.createdAt)
        )
        .slice(0, 5)
        .forEach(upload => {
            const row = document.createElement("div");
            row.className = "recent-upload-row";

            const details = document.createElement("div");

            const fileName = document.createElement("strong");
            fileName.textContent = upload.fileName;

            const records = document.createElement("small");
            records.textContent =
                `${upload.processedRows || 0} / ${upload.totalRows || 0} processed`;

            details.append(fileName, records);

            const status = document.createElement("span");
            status.className =
                "upload-status " +
                String(upload.status || "").toLowerCase();

            status.textContent = String(upload.status || "UNKNOWN")
                .replaceAll("_", " ");

            row.append(details, status);
            container.appendChild(row);
        });
}

function renderDashboardDepartments(employees) {
    const container =
        document.getElementById("dashboardDepartments");

    container.replaceChildren();

    const departments = {};

    employees.forEach(employee => {
        const department = employee.department || "Unassigned";
        departments[department] = (departments[department] || 0) + 1;
    });

    const sorted = Object.entries(departments)
        .sort((a, b) => b[1] - a[1]);

    if (!sorted.length) {
        container.textContent = "No department data available.";
        return;
    }

    sorted.forEach(([name, count]) => {
        const percentage = Math.round(
            (count / employees.length) * 100
        );

        const row = document.createElement("div");
        row.className = "department-row";

        const label = document.createElement("div");
        label.className = "department-label";

        const departmentName = document.createElement("strong");
        departmentName.textContent = name;

        const departmentCount = document.createElement("span");
        departmentCount.textContent = `${count} employees`;

        label.append(departmentName, departmentCount);

        const bar = document.createElement("div");
        bar.className = "department-bar";

        const fill = document.createElement("div");
        fill.style.width = percentage + "%";

        bar.appendChild(fill);

        row.append(label, bar);
        container.appendChild(row);
    });
}

function showUploadPage() {
    showPage("upload-section");
}
