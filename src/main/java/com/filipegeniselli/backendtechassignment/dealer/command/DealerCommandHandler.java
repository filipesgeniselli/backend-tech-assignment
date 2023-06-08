package com.filipegeniselli.backendtechassignment.dealer.command;

import com.filipegeniselli.backendtechassignment.dealer.Dealer;
import com.filipegeniselli.backendtechassignment.dealer.DealerRepository;
import com.filipegeniselli.backendtechassignment.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DealerCommandHandler implements DealerCommandService{

    private final DealerRepository dealerRepository;

    @Autowired
    public DealerCommandHandler(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }


    @Override
    public UUID handle(CreateUpdateDealer command) {
        Dealer dealer = Dealer.DealerBuilder.aDealer()
                .id(UUID.randomUUID())
                .name(command.name())
                .tier(command.tier())
                .allowRemovingOldListings(command.allowRemovingOldListings())
                .build();

        dealerRepository.save(dealer);

        return dealer.getId();
    }

    @Override
    public void handle(UUID dealerId, CreateUpdateDealer command) {

        Dealer dealer = dealerRepository
                .findById(dealerId)
                .orElseThrow(() -> new NotFoundException("Couldn't find the dealer"));

        dealer.setName(command.name());
        dealer.setTier(command.tier());
        dealer.setAllowRemovingOldListings(command.allowRemovingOldListings());

        dealerRepository.save(dealer);
    }
}
