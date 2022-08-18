package io.symeo.monolithic.backend.domain.job;

import lombok.*;

import java.util.Date;
import java.util.UUID;

import static java.util.Objects.isNull;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Job {
    public static final String CREATED = "CREATED";
    public static final String STARTED = "STARTED";
    public static final String FAILED = "FAILED";
    public static final String FINISHED = "FINISHED";

    Long id;
    @NonNull
    @Builder.Default
    String status = CREATED;
    @NonNull UUID organizationId;
    @ToString.Exclude
    JobRunnable jobRunnable;
    Date endDate;
    Date creationDate;
    Job nextJob;
    String code;

    public String getCode() {
        return isNull(code) ? this.jobRunnable.getCode() : this.code;
    }

    public Job started() {
        return this.toBuilder().status(STARTED).build();
    }

    public Job failed() {
        return this.toBuilder().status(FAILED).endDate(new Date()).build();
    }

    public Job finished() {
        return this.toBuilder().status(FINISHED).endDate(new Date()).build();
    }
}
