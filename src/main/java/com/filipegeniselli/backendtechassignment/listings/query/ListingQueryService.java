package com.filipegeniselli.backendtechassignment.listings.query;

import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.listings.ListingDto;

import java.util.UUID;

public interface ListingQueryService {

    PagedResult<ListingDto> handle(UUID dealerId, FindAllWithFilters query);

    ListingDto handle(UUID dealerId, FindById query);

}
