package com.filipegeniselli.backendtechassignment.dealer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DealerRepository extends JpaRepository<Dealer, UUID> {
    Page<Dealer> findByNameLikeIgnoreCase(String name, Pageable pageable);

}
