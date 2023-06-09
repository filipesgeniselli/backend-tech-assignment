package com.filipegeniselli.backendtechassignment.listings.query;

import com.filipegeniselli.backendtechassignment.PageInfo;
import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.exception.NotFoundException;
import com.filipegeniselli.backendtechassignment.listings.Listing;
import com.filipegeniselli.backendtechassignment.listings.ListingDealerDto;
import com.filipegeniselli.backendtechassignment.listings.ListingDto;
import com.filipegeniselli.backendtechassignment.listings.ListingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Service
public class ListingQueryHandler implements ListingQueryService {

    private final ListingRepository listingRepository;

    @Autowired
    public ListingQueryHandler(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public ListingDto handle(UUID dealerId, FindById query) {
        Listing listing = listingRepository.findByDealer_IdAndId(dealerId, query.listingId())
                .orElseThrow(() -> new NotFoundException("Could not find Listing with the requested Id"));

        return convertEntityToDto(listing);
    }

    @Override
    public PagedResult<ListingDto> handle(UUID dealerId, FindAllWithFilters query) {
        Page<Listing> result = listingRepository.findByDealer_IdAndStatus(dealerId,
                query.status(),
                query.pageRequest());

        return new PagedResult<>(result
                .get()
                .map(this::convertEntityToDto)
                .toList(),
                new PageInfo(result.getSize(),
                        result.getNumber(),
                        result.getTotalElements()));

    }

    private ListingDto convertEntityToDto(Listing entity) {
        return new ListingDto(
                new ListingDealerDto(entity.getDealer().getName(),
                        UriComponentsBuilder
                                .fromPath("/dealers/{id}")
                                .buildAndExpand(entity.getDealer().getId())
                                .toString()),
                entity.getVehicle(),
                entity.getCondition(),
                entity.getPrice(),
                entity.getColor(),
                entity.getTransmission(),
                entity.getMileage(),
                entity.getFuelType(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getPublishedAt(),
                entity.getRemovedAt(),
                UriComponentsBuilder
                        .fromPath("/{dealerId}/listings/{id}")
                        .buildAndExpand(entity.getDealer().getId(), entity.getId())
                        .toString()
        );
    }
}
