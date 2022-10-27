package io.symeo.monolithic.backend.domain.bff.model.job;

import lombok.Builder;
import lombok.Value;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Builder
@Value
public class JobView {
    Long id;
    String code;
    String status;
    Date creationDate;
    Date endDate;
    Integer progressionPercentage;
    UUID organizationId;
    List<TaskView> tasks;

    public boolean isFinished() {
        return status.equals("FINISHED");
    }

    public double getProgressionPercentage() {
        final List<TaskView> tasks = this.getTasks();
        long tasksDoneCount = tasks.stream().filter(task -> task.getStatus().equals("DONE")).count();
        return Math.round(100 * tasksDoneCount / (tasks.size() * 1.0)) / 100D;
    }
}
