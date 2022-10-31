package io.symeo.monolithic.backend.domain.bff.model.job;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class TaskView {
    public static final String TO_DO = "TO_DO";
    public static final String DONE = "DONE";
    Object input;
    String status;
}
