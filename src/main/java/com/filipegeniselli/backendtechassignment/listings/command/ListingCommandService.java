package com.filipegeniselli.backendtechassignment.listings.command;

import java.util.UUID;

public interface ListingCommandService {

    UUID handle(UUID dealerId, CreateUpdateListing command);

    void handle(UUID dealerId, UUID listingId, CreateUpdateListing command);

    void handle(UUID dealerId, UUID listingId, PublishListing command);

}
