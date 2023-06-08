package com.filipegeniselli.backendtechassignment;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public abstract class ListingsBaseControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void configureRestAssured(){
        RestAssured.port = port;
    }

    protected String createDealer(InputStream dealerStream) {
        String location = given()
                .body(dealerStream)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String [] locationArray = location.split("/");
        return Arrays.stream(locationArray)
                .skip(Math.max(0, locationArray.length - 2))
                .map(Object::toString)
                .collect(Collectors.joining("/"));
    }

    protected String createListing(String dealerId, InputStream listingStream) {
        String location = given()
                .body(listingStream)
                .contentType(ContentType.JSON)
                .post("/%s/listings".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String [] locationArray = location.split("/");
        return Arrays.stream(locationArray)
                .skip(Math.max(0, locationArray.length - 2))
                .map(Object::toString)
                .collect(Collectors.joining("/"));
    }

    protected String createDealer(String dealerName) {
        Map<String, Object> body = new HashMap<>(){{
            put("name", dealerName);
            put("tier", "BASIC");
            put("allowRemovingOldListings", true);
        }};

        String location = given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/dealer")
                .then()
                .assertThat()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract()
                .header("Location");

        String [] locationArray = location.split("/");
        return Arrays.stream(locationArray)
                .skip(Math.max(0, locationArray.length - 2))
                .map(Object::toString)
                .collect(Collectors.joining("/"));
    }


    protected InputStream getDealerResourceAsStream(String resourceName) {
        return getResourceAsStream("dealers", resourceName);
    }

    protected InputStream getListingsResourceAsStream(String resourceName) {
        return getResourceAsStream("listings", resourceName);
    }

    private InputStream getResourceAsStream(String folderName, String resourceName) {
        return getClass().getResourceAsStream("/%s/%s".formatted(folderName, resourceName));
    }
}
