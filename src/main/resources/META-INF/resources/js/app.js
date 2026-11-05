document.addEventListener("DOMContentLoaded", function () {

    console.log("HR Employee Dashboard initialized");

    const navigationLinks =
        document.querySelectorAll(".nav-link");

    const dashboardPages =
        document.querySelectorAll(".dashboard-page");


    // Show selected page
    function showPage(pageId) {

        dashboardPages.forEach(page => {
            page.classList.remove("active-page");
        });

        navigationLinks.forEach(link => {
            link.classList.remove("active");
        });

        const selectedPage =
            document.getElementById(pageId);

        if (selectedPage) {
            selectedPage.classList.add("active-page");
        }

        const activeLink =
            document.querySelector(`.nav-link[href="#${pageId}"]`);

        if (activeLink) {
            activeLink.classList.add("active");
        }
                if (pageId === "upload-section") {
                    handleBulkUploadPageOpened();
                }

        // Load data when opening the relevant page
        if (pageId === "employees-section") {
            loadEmployees();
        }

        if (pageId === "history-section") {
            loadUploadHistory();
        }

    }


    // Handle sidebar navigation
    navigationLinks.forEach(link => {

        link.addEventListener("click", function (event) {

            event.preventDefault();

            const pageId =
                this.getAttribute("href").substring(1);

            showPage(pageId);

            window.history.replaceState(null, "", "#" + pageId);
        });
    });


    // Open dashboard by default
    const initialPage = window.location.hash.substring(1);

    const validPages = [
        "dashboard",
        "upload-section",
        "history-section",
        "employees-section"
    ];

    showPage(
        validPages.includes(initialPage)
            ? initialPage
            : "dashboard"
    );

});
