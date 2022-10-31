package io.symeo.monolithic.backend.domain.helper.pagination;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Pagination {
    int start;
    int end;
}
