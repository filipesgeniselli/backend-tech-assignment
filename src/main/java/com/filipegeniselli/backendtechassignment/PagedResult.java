package com.filipegeniselli.backendtechassignment;

import java.util.List;

public record PagedResult<T>(List<T> data, PageInfo pageInfo) {
}
