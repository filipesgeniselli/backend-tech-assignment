package com.filipegeniselli.backendtechassignment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DealerControllerTests {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured(){
        RestAssured.port = port;
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


    @Test
    void getDealer_ShouldReturnOk() {
        //File createDealerBody = new File("/src/test/resources/dealers/basicDealerRemoveOldListings.json");
        InputStream createDealerBody = getClass().getResourceAsStream("/dealers/basicDealerRemoveOldListings.json");
        String location = given()
                .body(createDealerBody)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String [] locationArray = location.split("/");
        location = Arrays.stream(locationArray)
                .skip(Math.max(0, locationArray.length - 2))
                .map(Object::toString)
                .collect(Collectors.joining("/"));

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

}
