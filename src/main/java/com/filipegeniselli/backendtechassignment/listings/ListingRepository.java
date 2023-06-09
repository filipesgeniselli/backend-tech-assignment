package com.filipegeniselli.backendtechassignment.listings;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@java.lang.SuppressWarnings("java:S100")
@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {

    Page<Listing> findByDealer_IdAndStatus(@NonNull UUID id, @NonNull ListingStatus status, Pageable pageable);

    Listing findFirstByDealer_IdAndStatusOrderByPublishedAtAsc(UUID id, ListingStatus status);

    Optional<Listing> findByDealer_IdAndId(@NonNull UUID dealerId, @NonNull UUID listingId);

    long countByDealer_IdAndStatus(@NonNull UUID id, @NonNull ListingStatus status);

}
