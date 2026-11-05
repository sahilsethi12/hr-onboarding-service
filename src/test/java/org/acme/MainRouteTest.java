
package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MainRouteTest {

    @Test
    void shouldReturnEmployees() {
        given()
                .when()
                .get("/api/employees")
                .then()
                .statusCode(200)
                .body("employees", notNullValue());
    }

    @Test
    void shouldSupportEmployeePagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/api/employees")
                .then()
                .statusCode(200);
    }

    @Test
    void shouldRejectNegativePageNumber() {
        given()
                .queryParam("page", -1)
                .when()
                .get("/api/employees")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectInvalidPageSize() {
        given()
                .queryParam("size", 500)
                .when()
                .get("/api/employees")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldRejectInvalidEmployeeId() {
        given()
                .when()
                .get("/api/employees/invalid-uuid")
                .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn404ForUnknownEmployee() {
        given()
                .when()
                .get("/api/employees/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn404ForUnknownUploadJob() {
        given()
                .when()
                .get("/api/uploads/" + UUID.randomUUID())
                .then()
                .statusCode(404);
    }
}
