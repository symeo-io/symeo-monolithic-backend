package io.symeo.monolithic.backend.domain.bff.model.job;

import lombok.Builder;
import lombok.Value;

import java.util.Date;

@Builder
@Value
public class JobView {
    Long id;
    String code;
    String status;
    Date creationDate;
    Date endDate;
    Integer progressionPercentage;

    public boolean isFinished() {
        return false;
    }
}
