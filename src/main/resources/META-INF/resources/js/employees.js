// Pagination state
let currentPage = 0;
let pageSize = 20;
let totalPages = 0;


// HTML elements
const employeeTableBody =
    document.getElementById("employeeTableBody");

const previousPageButton =
    document.getElementById("previousPage");

const nextPageButton =
    document.getElementById("nextPage");

const pageInfo =
    document.getElementById("pageInfo");

const pageSizeSelect =
    document.getElementById("pageSize");


// Load employees from backend
async function loadEmployees() {

    employeeTableBody.innerHTML = `
        <tr>
            <td colspan="6">Loading employees...</td>
        </tr>
    `;

    try {

        const response = await getEmployees(currentPage, pageSize);

        const employees = response.employees || [];

        totalPages = response.totalPages || 0;

        // Display employee records
        renderEmployees(employees);

        // Update pagination information
        updatePagination(response);

    } catch (error) {

        console.error("Employee loading failed:", error);

        employeeTableBody.innerHTML = `
            <tr>
                <td colspan="6">
                    Unable to load employees.
                </td>
            </tr>
        `;
    }
}


function renderEmployees(employees) {

    employeeTableBody.replaceChildren();

    if (employees.length === 0) {

        const row = document.createElement("tr");
        const cell = document.createElement("td");

        cell.colSpan = 8;
        cell.textContent = "No employees found.";

        row.appendChild(cell);
        employeeTableBody.appendChild(row);

        return;
    }

    employees.forEach(employee => {

        const row = document.createElement("tr");

        const values = [
            `${employee.firstName} ${employee.lastName}`,
            employee.dob,
            employee.gender,
            employee.mobile,
            employee.email || "-",
            employee.department,
            employee.designation,
            employee.dateOfJoining
        ];

        values.forEach(value => {

            const cell = document.createElement("td");

            cell.textContent = value ?? "-";

            row.appendChild(cell);
        });

        employeeTableBody.appendChild(row);
    });
}

// Update pagination controls
function updatePagination(response) {

    const totalRecords = response.totalRecords || 0;

    const displayPage = totalPages === 0
        ? 0
        : currentPage + 1;

    pageInfo.textContent =
        `Page ${displayPage} of ${totalPages} (${totalRecords} employees)`;

    previousPageButton.disabled = currentPage <= 0;

    nextPageButton.disabled =
        totalPages === 0 || currentPage >= totalPages - 1;
}


// Previous page
previousPageButton.addEventListener("click", function () {

    if (currentPage > 0) {

        currentPage--;

        loadEmployees();
    }
});


// Next page
nextPageButton.addEventListener("click", function () {

    if (currentPage < totalPages - 1) {

        currentPage++;

        loadEmployees();
    }
});


// Change page size
pageSizeSelect.addEventListener("change", function () {

    pageSize = Number(pageSizeSelect.value);

    currentPage = 0;

    loadEmployees();
});


// Initialize employee directory
function initializeEmployees() {

    loadEmployees();
}
