package fr.catlean.monolithic.backend.domain.job;

import lombok.*;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Job {
    private static final String CREATED = "CREATED";
    private static final String STARTED = "STARTED";
    private static final String FAILED = "FAILED";
    private static final String FINISHED = "FINISHED";

    Long id;
    @NonNull
    @Builder.Default
    String status = CREATED;
    @NonNull UUID organizationId;
    @ToString.Exclude
    JobRunnable jobRunnable;
    Date endDate;
    Job nextJob;

    public String getCode() {
        return this.jobRunnable.getCode();
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
