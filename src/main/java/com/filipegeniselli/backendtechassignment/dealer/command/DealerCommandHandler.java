package com.filipegeniselli.backendtechassignment.dealer.command;

import com.filipegeniselli.backendtechassignment.dealer.Dealer;
import com.filipegeniselli.backendtechassignment.dealer.DealerRepository;
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
    public UUID handle(CreateNewDealer command) {
        Dealer dealer = Dealer.DealerBuilder.aDealer()
                .id(UUID.randomUUID())
                .name(command.name())
                .tier(command.tier())
                .allowRemovingOldListings(command.allowRemovingOldListings())
                .build();

        dealerRepository.save(dealer);

        return dealer.getId();
    }
}
