package io.symeo.monolithic.backend.domain.model.core;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Page<T> {

    List<T> content;
    int totalPageNumber;
}
