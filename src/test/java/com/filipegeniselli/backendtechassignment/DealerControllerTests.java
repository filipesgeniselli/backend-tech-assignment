package com.filipegeniselli.backendtechassignment;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class DealerControllerTests extends ListingsBaseControllerTest {

    public static Stream<Arguments> pagingParameters() {
        return Stream.of(
                Arguments.of(0, 10, 10),
                Arguments.of(2, 10, 10),
                Arguments.of(3, 5, 5),
                Arguments.of(4, 7, 2),
                Arguments.of(0, 30, 30),
                Arguments.of(1, 30, 0)
        );
    }

    public static Stream<Arguments> pagingFilterParameters() {
        return Stream.of(
                Arguments.of("DEALER", 0, 10, 10, 30),
                Arguments.of("dealer", 2, 10, 10, 30),
                Arguments.of("dealer 1", 1, 5, 5, 11),
                Arguments.of("dealer 1", 2, 5, 1, 11),
                Arguments.of("Dealer 20", 0, 7, 1, 1)
        );
    }

    @Test
    void getDealers_ShouldReturnEmptyList() {
        given()
                .get("/dealer")
                .then()
                .assertThat()
                .statusCode(200)
                .body("pageInfo.page", equalTo(0))
                .body("pageInfo.pageSize", equalTo(20))
                .body("pageInfo.total", equalTo(0));
    }

    @ParameterizedTest
    @MethodSource("pagingFilterParameters")
    void getDealerListFiltering_shouldReturnOkWithCorrectPaging(String filter,
                                                                int page,
                                                                int pageSize,
                                                                int expectedPageLength,
                                                                int expectedTotal) {
        for(int i = 0; i < 30; i++) {
            createDealer("Dealer %s".formatted(i));
        }

        given()
                .get("/dealer?name=%s&page=%s&pageSize=%s".formatted(filter, page, pageSize))
                .then()
                .statusCode(200)
                .body("pageInfo.pageSize", equalTo(pageSize))
                .body("pageInfo.page", equalTo(page))
                .body("pageInfo.total", equalTo(expectedTotal))
                .body("data.size()", is(expectedPageLength));
    }

    @ParameterizedTest
    @MethodSource("pagingParameters")
    void getDealerList_shouldReturnOkWithCorrectPaging(int page, int pageSize, int expectedPageLength) {
        for(int i = 0; i < 30; i++) {
            createDealer("Dealer %s".formatted(i));
        }

        given()
                .get("/dealer?page=%s&pageSize=%s".formatted(page, pageSize))
                .then()
                .statusCode(200)
                .body("pageInfo.pageSize", equalTo(pageSize))
                .body("pageInfo.page", equalTo(page))
                .body("pageInfo.total", equalTo(30))
                .body("data.size()", is(expectedPageLength));
    }

    @Test
    void getDealer_ShouldReturnOk() {
        String location = createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json"));

        given()
                .get(location)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Test Basic dealer"))
                .body("tier", equalTo("BASIC"))
                .body("allowRemovingOldListings", equalTo(true))
                .body("url", endsWithIgnoringCase(location));
    }

    @Test
    void getInvalidDealer_ShouldReturnNotFound() {
        given()
                .get("/dealer/00000000-0000-0000-0000-000000000000")
                .then()
                .assertThat()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("message", equalTo("Could not find Dealer with the requested Id"));
    }

    @Test
    void getDealerWithNonUuid_ShouldReturnBadRequest() {
        given()
                .get("/dealer/1")
                .then()
                .assertThat()
                .statusCode(400);
    }

    @Test
    void updateDealer_ShouldReturnAccepted() {
        String location = createDealer(getDealerResourceAsStream("businessDealerNotRemoveOldListings.json"));
        given()
                .get(location)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Test Business dealer"))
                .body("tier", equalTo("BUSINESS"))
                .body("allowRemovingOldListings", equalTo(false))
                .body("url", endsWithIgnoringCase(location));

        given()
                .body(getDealerResourceAsStream("businessDealerRemoveOldListings.json"))
                .contentType(ContentType.JSON)
                .put(location)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .get(location)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Test Business dealer"))
                .body("tier", equalTo("BUSINESS"))
                .body("allowRemovingOldListings", equalTo(true))
                .body("url", endsWithIgnoringCase(location));
    }

    @Test
    void createDealerWithInvalidData_ShouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>(){{
            put("invalidName", "invalid name");
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field name is required" + System.lineSeparator() +
                        "The field tier is required" + System.lineSeparator() +
                        "The field allowRemovingOldListings is required"));
    }

    @Test
    void createDealerWithNoName_ShouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>(){{
            put("tier", "BASIC");
            put("allowRemovingOldListings", true);
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field name is required"));
    }

    @Test
    void createDealerWithNoTier_ShouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>(){{
            put("name", "dealer name");
            put("allowRemovingOldListings", true);
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field tier is required"));
    }

    @Test
    void createDealerWithNoAllowRemovingOldListings_ShouldReturnBadRequest() {
        Map<String, Object> body = new HashMap<>(){{
            put("name", "dealer name");
            put("tier", "BASIC");
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field allowRemovingOldListings is required"));
    }

    @Test
    void updateDealerWithInvalidData_ShouldReturnBadRequest() {
        String location = createDealer(getDealerResourceAsStream("businessDealerNotRemoveOldListings.json"));
        given()
                .get(location)
                .then()
                .assertThat()
                .statusCode(200)
                .body("name", equalTo("Test Business dealer"))
                .body("tier", equalTo("BUSINESS"))
                .body("allowRemovingOldListings", equalTo(false))
                .body("url", endsWithIgnoringCase(location));

        Map<String, Object> body = new HashMap<>(){{
            put("name", "dealer name");
            put("tier", "BASIC");
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .put(location)
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field allowRemovingOldListings is required"));
    }

}
