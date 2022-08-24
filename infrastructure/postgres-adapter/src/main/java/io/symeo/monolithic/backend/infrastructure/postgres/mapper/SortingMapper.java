package io.symeo.monolithic.backend.infrastructure.postgres.mapper;

import java.util.Map;

public interface SortingMapper {

    Map<String, String> SORTING_DIRECTION_MAPPING = Map.of("asc", "asc", "desc", "desc");

    static String directionToPostgresSortingValue(final String sortingDirection) {
        return SORTING_DIRECTION_MAPPING.getOrDefault(sortingDirection, sortingDirection);
    }
}
