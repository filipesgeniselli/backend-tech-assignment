package com.filipegeniselli.backendtechassignment;

import com.filipegeniselli.backendtechassignment.dealer.DealerTierLimit;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.*;
import java.util.stream.Stream;

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
        String dealerId = createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json"));

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
        String dealerId = createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json"));

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
        String dealerId = createDealer(getDealerResourceAsStream("basicDealerRemoveOldListings.json"));
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
                .body("status", equalTo("DRAFT"));
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
        String dealerId = extractDealerIdFromLocation(createDealer(dealerResourceName));
        Set<String> listings = createAndPublishSetOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));

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

        verifyListingStatus(listings.stream().findFirst().get(), "PUBLISHED");
    }

    @ParameterizedTest
    @MethodSource("exceedingLimitListConflict")
    void publishListingExceedingLimit_ShouldReturnConflictAndAcceptedAfterRemoval(String dealerResourceName,
                                                                                  DealerTierLimit tierLimit) {
        String dealerId = extractDealerIdFromLocation(createDealer(dealerResourceName));
        Set<String> listings = createAndPublishSetOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));

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
                .patch(listings.stream().findFirst().get())
                .then()
                .assertThat()
                .statusCode(202);

        verifyListingStatus(listings.stream().findFirst().get(), "REMOVED");

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
        String dealerId = extractDealerIdFromLocation(createDealer(dealerResourceName));
        Set<String> listings = createAndPublishSetOfListings(dealerId, tierLimitConfiguration.getTierLimit(tierLimit));
        String listingLocation = createAndPublishListing(dealerId, "newListing.json");

        verifyListingStatus(listingLocation, "PUBLISHED");
        verifyListingStatus(listings.stream().findFirst().get(), "REMOVED");
    }

    private void verifyListingStatus(String listingLocation, String expectedStatus) {
        given()
                .get(listingLocation)
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo(expectedStatus));
    }

    public Set<String> createAndPublishSetOfListings(String dealerId, int amount) {
        Set<String> listings = new HashSet<>();
        for (int i = 0; i < amount; i++) {
            listings.add(createAndPublishListing(dealerId, "newListing.json"));
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
