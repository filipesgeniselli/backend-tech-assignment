package com.filipegeniselli.backendtechassignment.dealer.query;

import com.filipegeniselli.backendtechassignment.PageInfo;
import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.dealer.Dealer;
import com.filipegeniselli.backendtechassignment.dealer.DealerDto;
import com.filipegeniselli.backendtechassignment.dealer.DealerRepository;
import com.filipegeniselli.backendtechassignment.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DealerQueryHandler implements DealerQueryService {

    private final DealerRepository dealerRepository;

    @Autowired
    public DealerQueryHandler(DealerRepository dealerRepository) {
        this.dealerRepository = dealerRepository;
    }

    @Override
    public DealerDto handle(FindById query) {
        Dealer dealer = dealerRepository
                .findById(query.dealerId())
                .orElseThrow(() -> new NotFoundException("Could not find Dealer with the requested Id"));

        return convertEntityToDto(dealer);
    }

    @Override
    public PagedResult<DealerDto> handle(FindAllWithFilters query) {
        Page<Dealer> result = dealerRepository.findByNameLikeIgnoreCase(query.name(), query.pageRequest());

        return new PagedResult<>(result
                .get()
                .map(this::convertEntityToDto)
                .toList(),
                new PageInfo(result.getSize(),
                        result.getNumber(),
                        result.getTotalElements()));

    }

    private DealerDto convertEntityToDto(Dealer entity) {
        return new DealerDto(
                entity.getName(),
                entity.getTier(),
                entity.getAllowRemovingOldListings(),
                UriComponentsBuilder
                        .fromPath("/dealer/{id}")
                        .buildAndExpand(entity.getId())
                        .toString()
        );
    }
}
