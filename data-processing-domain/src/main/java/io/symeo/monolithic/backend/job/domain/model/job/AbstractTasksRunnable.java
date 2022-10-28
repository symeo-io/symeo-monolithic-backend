package io.symeo.monolithic.backend.job.domain.model.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.function.SymeoConsumer;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;

import java.util.List;

public class AbstractTasksRunnable<T> {

    List<Task> tasks;

    public List<Task> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    protected void executeAllTasks(final SymeoConsumer<T> symeoConsumer, final DataProcessingJobStorage dataProcessingJobStorage,
                                   final Long jobId) throws SymeoException {
        for (int i = 0; i < this.tasks.size(); i++) {
            final Task task = this.tasks.get(i);
            if (task.getStatus().equals(Task.TO_DO)) {
                final T t = (T) task.getInput();
                symeoConsumer.accept(t);
                this.tasks.set(i, task.done());
                dataProcessingJobStorage.updateJobWithTasksForJobId(jobId, this.tasks);
            }
        }
    }
}
