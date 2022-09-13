package io.symeo.monolithic.backend.domain.job;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Task {
    Object input;
    String status;
}
