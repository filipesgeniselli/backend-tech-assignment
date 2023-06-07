package com.filipegeniselli.backendtechassignment.dealer.query;

import com.filipegeniselli.backendtechassignment.PagedResult;
import com.filipegeniselli.backendtechassignment.dealer.DealerDto;

public interface DealerQueryService {

    DealerDto handle(FindById query);

    PagedResult<DealerDto> handle(FindAllWithFilters query);
}
