package com.filipegeniselli.backendtechassignment.listings;

import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.listings.command.CreateUpdateListing;
import com.filipegeniselli.backendtechassignment.listings.command.ListingCommandService;
import com.filipegeniselli.backendtechassignment.listings.command.PublishListing;
import com.filipegeniselli.backendtechassignment.listings.query.FindAllWithFilters;
import com.filipegeniselli.backendtechassignment.listings.query.FindById;
import com.filipegeniselli.backendtechassignment.listings.query.ListingQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/{dealerId}/listings")
public class ListingController {

    private final ListingQueryService listingQueryService;
    private final ListingCommandService listingCommandService;

    @Autowired
    public ListingController(ListingQueryService listingQueryService, ListingCommandService listingCommandService) {
        this.listingQueryService = listingQueryService;
        this.listingCommandService = listingCommandService;
    }

    @GetMapping
    public PagedResult<ListingDto> getListings(@PathVariable("dealerId") UUID dealerId,
                                               @RequestParam(required = true) ListingStatus status,
                                               @RequestParam(value="page", defaultValue = "0") int page,
                                               @RequestParam(value="pageSize", defaultValue = "20") int pageSize) {
        return listingQueryService.handle(dealerId, new FindAllWithFilters(status, PageRequest.of(page, pageSize)));
    }

    @GetMapping("/{listingId}")
    public ListingDto getListing(@PathVariable("dealerId") UUID dealerId,
                                 @PathVariable("listingId") UUID listingId) {

        return listingQueryService.handle(dealerId, new FindById(listingId));
    }

    @PostMapping
    public ResponseEntity<Void> createListing(@PathVariable("dealerId") UUID dealerId,
                                              @RequestBody CreateUpdateListing newListing) {
        UUID result = listingCommandService.handle(dealerId, newListing);

        return ResponseEntity
                .created(ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(result)
                        .toUri())
                .build();
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<Void> updateListing(@PathVariable("dealerId") UUID dealerId,
                                              @PathVariable("listingId") UUID listingId,
                                              @RequestBody CreateUpdateListing listing) {
        listingCommandService.handle(dealerId, listingId, listing);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping("/{listingId}")
    public ResponseEntity<Void> publishListing(@PathVariable("dealerId") UUID dealerId,
                                               @PathVariable("listingId") UUID listingId,
                                               @RequestBody PublishListing publishListing) {
        listingCommandService.handle(dealerId, listingId, publishListing);
        return ResponseEntity.accepted().build();
    }

}
