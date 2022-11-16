package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.job.Job;
import io.symeo.monolithic.backend.job.domain.model.job.Task;

import java.util.List;

public interface DataProcessingJobStorage {
    Job createJob(Job job) throws SymeoException;

    Job updateJob(Job jobStarted) throws SymeoException;

    void updateJobWithTasksForJobId(Long jobId, List<Task> tasks) throws SymeoException;

}
