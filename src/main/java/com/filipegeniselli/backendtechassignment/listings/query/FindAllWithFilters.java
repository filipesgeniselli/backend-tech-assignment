package com.filipegeniselli.backendtechassignment.listings.query;

import com.filipegeniselli.backendtechassignment.listings.ListingStatus;
import org.springframework.data.domain.PageRequest;

public record FindAllWithFilters(ListingStatus status, PageRequest pageRequest) {
}
