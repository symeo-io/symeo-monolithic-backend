package io.symeo.monolithic.backend.domain.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Job {
    public static final String CREATED = "CREATED";
    public static final String STARTED = "STARTED";
    public static final String FAILED = "FAILED";
    public static final String FINISHED = "FINISHED";
    public static final String RESTARTED = "RESTARTED";

    Long id;
    @NonNull
    @Builder.Default
    String status = CREATED;
    @NonNull UUID organizationId;
    UUID teamId;
    @ToString.Exclude
    JobRunnable jobRunnable;
    Date endDate;
    Date creationDate;
    Job nextJob;
    String code;
    String error;
    @NonNull
    List<Task> tasks;

    public String getCode() {
        return isNull(code) ? this.jobRunnable.getCode() : this.code;
    }

    public Job started() {
        return this.toBuilder().status(STARTED).build();
    }

    public Job restarted() {
        return this.toBuilder().status(RESTARTED).build();
    }

    public Job failed(final SymeoException symeoException) {
        return this.toBuilder()
                .status(FAILED)
                .endDate(new Date())
                .tasks(jobRunnable.getTasks())
                .error(symeoException.toString()).build();
    }

    public Job finished() {
        return this.toBuilder()
                .status(FINISHED)
                .endDate(new Date())
                .tasks(jobRunnable.getTasks())
                .build();
    }
}
