package com.filipegeniselli.backendtechassignment.listings.command;

import com.filipegeniselli.backendtechassignment.listings.VehicleCondition;
import com.filipegeniselli.backendtechassignment.listings.VehicleFuelType;
import com.filipegeniselli.backendtechassignment.listings.VehicleTransmission;

import java.math.BigDecimal;

public record CreateUpdateListing(String vehicle,
                                  VehicleCondition condition,
                                  BigDecimal price,
                                  String color,
                                  VehicleTransmission transmission,
                                  Integer mileage,
                                  VehicleFuelType fuelType) {
}
