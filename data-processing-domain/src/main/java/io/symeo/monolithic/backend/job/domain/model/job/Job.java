package io.symeo.monolithic.backend.job.domain.model.job;

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
    @ToString.Exclude
    JobRunnable jobRunnable;
    Date endDate;
    Date creationDate;
    Job nextJob;
    String code;
    String error;
    @Builder.Default
    List<Task> tasks = List.of();

    public String getCode() {
        return isNull(code) ? this.jobRunnable.getCode() : this.code;
    }

    public Job started() throws SymeoException {
        this.getJobRunnable().initializeTasks();
        return this.toBuilder().status(STARTED).tasks(this.getJobRunnable().getTasks()).build();
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

    public double getProgressionPercentage() {
        final List<Task> tasks = this.getTasks();
        long tasksDoneCount = tasks.stream().filter(task -> task.getStatus().equals(Task.DONE)).count();
        return Math.round(100 * tasksDoneCount / (tasks.size() * 1.0)) / 100D;
    }

    public void run() throws SymeoException {
        this.jobRunnable.run(this.id);
    }
}
