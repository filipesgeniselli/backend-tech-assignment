package com.filipegeniselli.backendtechassignment;

import com.filipegeniselli.backendtechassignment.dealer.DealerTierLimit;
import com.filipegeniselli.backendtechassignment.listings.VehicleCondition;
import com.filipegeniselli.backendtechassignment.listings.VehicleFuelType;
import com.filipegeniselli.backendtechassignment.listings.VehicleTransmission;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class ListingsControllerTests extends ListingsBaseControllerTest{

    @Autowired
    private TierLimitConfiguration tierLimitConfiguration;

    public static Stream<Arguments> exceedingLimitListConflict() {
        return Stream.of(
                Arguments.of("freeDealerNotRemoveOldListings.json", DealerTierLimit.FREE),
                Arguments.of("basicDealerNotRemoveOldListings.json", DealerTierLimit.BASIC),
                Arguments.of("premiumDealerNotRemoveOldListings.json", DealerTierLimit.PREMIUM),
                Arguments.of("businessDealerNotRemoveOldListings.json", DealerTierLimit.BUSINESS)
        );
    }

    public static Stream<Arguments> exceedingLimitListAccepted() {
        return Stream.of(
                Arguments.of("freeDealerRemoveOldListings.json", DealerTierLimit.FREE),
                Arguments.of("basicDealerRemoveOldListings.json", DealerTierLimit.BASIC),
                Arguments.of("premiumDealerRemoveOldListings.json", DealerTierLimit.PREMIUM),
                Arguments.of("businessDealerRemoveOldListings.json", DealerTierLimit.BUSINESS)
        );
    }

    public static Stream<Arguments> pagingFilterParameters() {
        return Stream.of(
                Arguments.of("DRAFT", 0, 10, 10, 11),
                Arguments.of("DRAFT", 1, 10, 1, 11),
                Arguments.of("PUBLISHED", 1, 4, 4, 10),
                Arguments.of("PUBLISHED", 2, 4, 2, 10),
                Arguments.of("REMOVED", 0, 1, 1, 9),
                Arguments.of("REMOVED", 7, 1, 1, 9)
        );
    }

    @Test
    void getListings_ShouldReturnEmptyList() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));

        given()
                .get("/%s/listings?status=%s".formatted(dealerId, "DRAFT"))
                .then()
                .assertThat()
                .statusCode(200)
                .body("pageInfo.page", equalTo(0))
                .body("pageInfo.pageSize", equalTo(20))
                .body("pageInfo.total", equalTo(0));
    }

    @Test
    void getListingsWithNoStatus_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));

        given()
                .get("/%s/listings".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(400);
    }

    @ParameterizedTest
    @MethodSource("pagingFilterParameters")
    void getListingsListFiltering_shouldReturnOkWithCorrectPaging(String filter,
                                                                int page,
                                                                int pageSize,
                                                                int expectedPageLength,
                                                                int expectedTotal) {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("premiumDealerRemoveOldListings.json")));
        List<String> listingsList = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
            listingsList.add(createListing(dealerId, getListingsResourceAsStream("newListing.json")));
        }
        for (int i = 0; i < 19; i++) {
            String listingLocation = listingsList.get(i);

            given()
                    .body(new HashMap<String, Object>(){{
                        put("status", "PUBLISHED");
                    }})
                    .contentType(ContentType.JSON)
                    .patch(listingLocation)
                    .then()
                    .assertThat()
                    .statusCode(202);
        }

        given()
                .get("/%s/listings?status=%s&page=%s&pageSize=%s".formatted(dealerId, filter, page, pageSize))
                .then()
                .statusCode(200)
                .body("pageInfo.pageSize", equalTo(pageSize))
                .body("pageInfo.page", equalTo(page))
                .body("pageInfo.total", equalTo(expectedTotal))
                .body("data.size()", is(expectedPageLength));
    }

    @Test
    void createNewListing_ShouldReturnCreatedWithDraftListing() {
        String dealerId = extractDealerIdFromLocation(
                createDealer(
                        getDealerResourceAsStream("freeDealerNotRemoveOldListings.json")));

        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));
        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("vehicle", equalTo("2021 Tesla Model Y"))
                .body("condition", equalTo("USED"))
                .body("price", equalTo(47399.99F))
                .body("color", equalTo("white"))
                .body("transmission", equalTo("AUTOMATIC"))
                .body("mileage", equalTo(10500))
                .body("fuelType", equalTo("ELECTRIC"))
                .body("status", equalTo("DRAFT"))
                .body("createdAt", notNullValue())
                .body("publishedAt", nullValue())
                .body("removedAt", nullValue())
                .body("url", endsWith(listingLocation));
    }

    @Test
    void createNewListingWithNoVehicle_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(
                createDealer(
                        getDealerResourceAsStream("freeDealerNotRemoveOldListings.json")));

        Map<String, Object> body = new HashMap<>(){{
            put("condition", "NEW");
            put("price", "10");
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/%s/listings".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field vehicle is required"));
    }

    @Test
    void createNewInvalidListing_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(
                createDealer(
                        getDealerResourceAsStream("freeDealerNotRemoveOldListings.json")));

        Map<String, Object> body = new HashMap<>(){{
            put("color", "black");
            put("transmission", "MANUAL");
        }};

        given()
                .body(body)
                .contentType(ContentType.JSON)
                .post("/%s/listings".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("message", equalTo("The field vehicle is required" + System.lineSeparator() +
                        "The field condition is required" + System.lineSeparator() +
                        "The field price is required and needs to be greater than 0"));

    }

    @Test
    void publishListing_ShouldReturnAcceptedWithPublishedListing() {
        String dealerId = extractDealerIdFromLocation(
                createDealer(
                        getDealerResourceAsStream("freeDealerNotRemoveOldListings.json")));

        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));
        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("DRAFT"));

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("PUBLISHED"));
    }

    @ParameterizedTest
    @MethodSource("exceedingLimitListConflict")
    void publishListingExceedingLimit_ShouldReturnConflict(String dealerResourceName,
                                                           DealerTierLimit tierLimit) {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream(dealerResourceName)));
        Map<String, LocalDateTime> listings = createAndPublishMapOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));

        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));
        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("message", equalTo("You've reached the limit of published listings"));

        verifyListingStatus(listings
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, LocalDateTime>comparingByValue())
                        .findFirst().get().getKey(),
                "PUBLISHED");
    }

    @ParameterizedTest
    @MethodSource("exceedingLimitListConflict")
    void publishListingExceedingLimit_ShouldReturnConflictAndAcceptedAfterRemoval(String dealerResourceName,
                                                                                  DealerTierLimit tierLimit) {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream(dealerResourceName)));
        Map<String, LocalDateTime> listings = createAndPublishMapOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));

        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));
        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("message", equalTo("You've reached the limit of published listings"));


        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "REMOVED");
                }})
                .contentType(ContentType.JSON)
                .patch(listings.keySet().stream().findFirst().get())
                .then()
                .assertThat()
                .statusCode(202);

        verifyListingStatus(listings.keySet().stream().findFirst().get(), "REMOVED");

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        verifyListingStatus(listingLocation, "PUBLISHED");
    }

    @ParameterizedTest
    @MethodSource("exceedingLimitListAccepted")
    void publishListingExceedingLimit_ShouldReturnAccepted(String dealerResourceName,
                                                           DealerTierLimit tierLimit) {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream(dealerResourceName)));
        Map<String, LocalDateTime> listings = createAndPublishMapOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));
        String listingLocation = createAndPublishListing(dealerId, "newListing.json");

        verifyListingStatus(listingLocation, "PUBLISHED");
        verifyListingStatus(listings
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, LocalDateTime>comparingByValue())
                        .findFirst().get().getKey(),
                "REMOVED");
    }

    @Test
    void editDraftListing_ShouldReturnAccepted() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));
        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));

        given()
                .body(getListingsResourceAsStream("editListing.json"))
                .contentType(ContentType.JSON)
                .put(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("price", equalTo(54499.99F))
                .body("color", equalTo("black"));
    }

    @Test
    void editInvalidListing_ShouldReturnNotFound() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));

        given()
                .body(getListingsResourceAsStream("editListing.json"))
                .contentType(ContentType.JSON)
                .put("/%s/listings/00000000-0000-0000-0000-000000000000".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(404)
                .body("message", equalTo("Couldn't find the Listing."));
    }

    @Test
    void editPublishedListing_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));
        String listingLocation = createAndPublishListing(dealerId, "newListing.json");

        given()
                .body(getListingsResourceAsStream("editListing.json"))
                .contentType(ContentType.JSON)
                .put(listingLocation)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Operation not allowed - Only listings in Draft status can be edited."));
    }

    @Test
    void editRemovedListing_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));
        String listingLocation = createAndPublishListing(dealerId, "newListing.json");

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "REMOVED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .body(getListingsResourceAsStream("editListing.json"))
                .contentType(ContentType.JSON)
                .put(listingLocation)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Operation not allowed - Only listings in Draft status can be edited."));
    }

    @Test
    void publishInvalidListing_ShouldReturnNotFound(){
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch("/%s/listings/00000000-0000-0000-0000-000000000000".formatted(dealerId))
                .then()
                .assertThat()
                .statusCode(404)
                .body("message", equalTo("Couldn't find the Listing."));
    }

    @Test
    void publishRemovedListing_ShouldReturnBadRequest() {
        String dealerId = extractDealerIdFromLocation(createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json")));
        String listingLocation = createListing(dealerId, getListingsResourceAsStream("newListing.json"));

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "REMOVED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(400)
                .body("message", equalTo("Operation not allowed - Removed listings cannot have status changes."));
    }

    private void verifyListingStatus(String listingLocation, String expectedStatus) {
        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo(expectedStatus));
    }

    public Map<String, LocalDateTime> createAndPublishMapOfListings(String dealerId, int amount) {
        Map<String, LocalDateTime> listings = new HashMap<>();
        for (int i = 0; i < amount; i++) {
            String location = createAndPublishListing(dealerId, "newListing.json");
            String publishedDate = given()
                    .get(location)
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .body("publishedAt", notNullValue())
                    .extract()
                    .body()
                    .jsonPath()
                    .get("publishedAt");

            listings.put(location, LocalDateTime.parse(publishedDate));
        }

        return listings;
    }

    private String createAndPublishListing(String dealerId, String listingResourceName) {
        String listingLocation = createListing(dealerId, getListingsResourceAsStream(listingResourceName));
        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("DRAFT"));

        given()
                .body(new HashMap<String, Object>(){{
                    put("status", "PUBLISHED");
                }})
                .contentType(ContentType.JSON)
                .patch(listingLocation)
                .then()
                .assertThat()
                .statusCode(202);

        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("PUBLISHED"));

        return listingLocation;
    }

    private String extractDealerIdFromLocation(String dealerLocation) {
        String [] locationArray = dealerLocation.split("/");
        return locationArray[Math.max(0, locationArray.length -1)];
    }

}
