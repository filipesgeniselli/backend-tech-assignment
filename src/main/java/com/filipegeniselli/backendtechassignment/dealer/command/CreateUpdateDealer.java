package com.filipegeniselli.backendtechassignment.dealer.command;

import com.filipegeniselli.backendtechassignment.dealer.DealerTierLimit;

public record CreateUpdateDealer(String name, DealerTierLimit tier, Boolean allowRemovingOldListings) {
}
