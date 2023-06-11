package com.filipegeniselli.backendtechassignment.dealer;

/**
 * I followed the Configuration Property solution to set the tier limits
 * But a better solution would be to have a new entity with all the possible tiers and its maximum listings
 */
public enum DealerTierLimit {
    FREE,
    BASIC,
    PREMIUM,
    BUSINESS
}
