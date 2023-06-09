package com.filipegeniselli.backendtechassignment.listings;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ListingDto(ListingDealerDto dealer,
                         String vehicle,
                         VehicleCondition condition,
                         BigDecimal price,
                         String color,
                         VehicleTransmission transmission,
                         Integer mileage,
                         VehicleFuelType fuelType,
                         ListingStatus status,
                         LocalDateTime createdAt,
                         LocalDateTime publishedAt,
                         LocalDateTime removedAt,
                         String url) {
}
