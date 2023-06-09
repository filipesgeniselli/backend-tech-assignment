package com.filipegeniselli.backendtechassignment.listings.command;

import com.filipegeniselli.backendtechassignment.TierLimitConfiguration;
import com.filipegeniselli.backendtechassignment.dealer.Dealer;
import com.filipegeniselli.backendtechassignment.dealer.DealerRepository;
import com.filipegeniselli.backendtechassignment.exception.BadRequestException;
import com.filipegeniselli.backendtechassignment.exception.ConflictException;
import com.filipegeniselli.backendtechassignment.exception.NotFoundException;
import com.filipegeniselli.backendtechassignment.listings.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ListingCommandHandler implements ListingCommandService {

    private final ListingRepository listingRepository;
    private final DealerRepository dealerRepository;

    private final TierLimitConfiguration tierLimitConfiguration;

    @Autowired
    public ListingCommandHandler(ListingRepository listingRepository, DealerRepository dealerRepository, TierLimitConfiguration tierLimitConfiguration) {
        this.listingRepository = listingRepository;
        this.dealerRepository = dealerRepository;
        this.tierLimitConfiguration = tierLimitConfiguration;
    }

    @Override
    @Transactional
    public UUID handle(UUID dealerId, CreateUpdateListing command) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new NotFoundException("Couldn't find the Dealer"));

        Listing listing = Listing.ListingBuilder.aListing()
                .id(UUID.randomUUID())
                .dealer(dealer)
                .vehicle(command.vehicle())
                .condition(command.condition())
                .price(command.price())
                .color(command.color())
                .transmission(command.transmission())
                .mileage(command.mileage())
                .fuelType(command.fuelType())
                .status(ListingStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .build();

        listingRepository.save(listing);

        return listing.getId();
    }

    @Override
    @Transactional
    public void handle(UUID dealerId, UUID listingId, CreateUpdateListing command) {
        Listing listing = listingRepository.findByDealer_IdAndId(dealerId, listingId)
                .orElseThrow(() -> new NotFoundException("Couldn't find the Listing."));

        if(listing.getStatus() != ListingStatus.DRAFT) {
            throw new BadRequestException("Operation not allowed - Only listings in Draft status can be edited.");
        }

        listing.setVehicle(command.vehicle());
        listing.setCondition(command.condition());
        listing.setPrice(command.price());
        listing.setColor(command.color());
        listing.setTransmission(command.transmission());
        listing.setMileage(command.mileage());
        listing.setFuelType(command.fuelType());

        listingRepository.save(listing);
    }

    @Override
    @Transactional
    public void handle(UUID dealerId, UUID listingId, PublishListing command) {
        Listing listing = listingRepository.findByDealer_IdAndId(dealerId, listingId)
                .orElseThrow(() -> new NotFoundException("Couldn't find the Listing."));

        if(listing.getStatus() == ListingStatus.REMOVED) {
            throw new BadRequestException("Operation not allowed - Removed listings cannot have status changes.");
        }

        if (command.status() == ListingStatus.PUBLISHED){
            validateTierLimit(dealerId);
            listing.setPublishedAt(LocalDateTime.now());
        }

        if (command.status() == ListingStatus.REMOVED){
            listing.setRemovedAt(LocalDateTime.now());
        }
        listing.setStatus(command.status());

        listingRepository.save(listing);
    }

    private void validateTierLimit(UUID dealerId) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new NotFoundException("Couldn't find Dealer with the selected Id."));

        int limitAmount = tierLimitConfiguration.getTierLimit(dealer.getTier());
        long publishedListings = listingRepository.countByDealer_IdAndStatus(dealerId, ListingStatus.PUBLISHED);

        if (publishedListings + 1 > limitAmount) {
            handleExceedLimitDealer(dealer);
        }
    }

    private void handleExceedLimitDealer(Dealer dealer) {
        if (Boolean.FALSE.equals(dealer.getAllowRemovingOldListings())) {
            throw new ConflictException("You've reached the limit of published listings");
        }

        Listing oldestPublishedListing = listingRepository.findFirstByDealer_IdAndStatusOrderByPublishedAtAsc(dealer.getId(), ListingStatus.PUBLISHED);
        oldestPublishedListing.setStatus(ListingStatus.REMOVED);
        oldestPublishedListing.setRemovedAt(LocalDateTime.now());

        listingRepository.save(oldestPublishedListing);
    }

}
