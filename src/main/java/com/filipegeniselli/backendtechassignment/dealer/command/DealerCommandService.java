package com.filipegeniselli.backendtechassignment.dealer.command;

import java.util.UUID;

public interface DealerCommandService {

    UUID handle(CreateUpdateDealer command);

    void handle(UUID dealerId, CreateUpdateDealer command);

}
