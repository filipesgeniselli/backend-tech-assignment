package com.filipegeniselli.backendtechassignment.dealer.query;

import org.springframework.data.domain.PageRequest;

public record FindAllWithFilters(String name, PageRequest pageRequest) {
}
