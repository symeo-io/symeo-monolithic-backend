package fr.catlean.monolithic.backend.domain.job;

import lombok.*;

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

    UUID id;
    @NonNull
    String code;
    @NonNull
    @Builder.Default
    String status = CREATED;
    @NonNull UUID organizationId;
    @ToString.Exclude
    JobRunnable jobRunnable;

    public Job started() {
        return this.toBuilder().status(STARTED).build();
    }

    public Job failed() {
        return this.toBuilder().status(FAILED).build();
    }

    public Job finished() {
        return this.toBuilder().status(FINISHED).build();
    }
}
