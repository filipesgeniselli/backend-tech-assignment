package com.filipegeniselli.backendtechassignment.dealer;

import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.dealer.command.CreateNewDealer;
import com.filipegeniselli.backendtechassignment.dealer.command.DealerCommandService;
import com.filipegeniselli.backendtechassignment.dealer.query.DealerQueryService;
import com.filipegeniselli.backendtechassignment.dealer.query.FindAllWithFilters;
import com.filipegeniselli.backendtechassignment.dealer.query.FindById;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/dealer")
public class DealerController {

    private final DealerCommandService dealerCommandService;

    private final DealerQueryService dealerQueryService;

    @Autowired
    public DealerController(DealerCommandService dealerCommandService, DealerQueryService dealerQueryService) {
        this.dealerCommandService = dealerCommandService;
        this.dealerQueryService = dealerQueryService;
    }

    @GetMapping
    public PagedResult<DealerDto> getDealer(@RequestParam(value="name", required = false) String name,
                                            @RequestParam(value="page", defaultValue = "0") int page,
                                            @RequestParam(value="pageSize", defaultValue = "20") int pageSize) {
        return dealerQueryService.handle(new FindAllWithFilters(name, PageRequest.of(page, pageSize)));
    }

    @GetMapping("/{id}")
    public DealerDto getDealer(@PathVariable("id") UUID dealerId) {
        return dealerQueryService.handle(new FindById(dealerId));
    }

    @PostMapping
    public ResponseEntity<Void> getDealer(@RequestBody CreateNewDealer dealerBody) {
        UUID result = dealerCommandService.handle(dealerBody);

        return ResponseEntity
                .created(ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(result)
                        .toUri())
                .build();

    }

}
