package com.filipegeniselli.backendtechassignment.dealer;

public record DealerDto(String name, DealerTierLimit tier, Boolean allowRemovingOldListings, String url) {
}
