package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;

import java.util.List;

public class AbstractTasksRunnable<T> {

    List<Task> tasks;

    public List<Task> getTasks() {
        return this.tasks;
    }

    protected void executeAllTasks(final SymeoConsumer<T> symeoConsumer, final JobStorage jobStorage,
                                   final Long jobId) throws SymeoException {
        for (int i = 0; i < this.tasks.size(); i++) {
            final Task task = this.tasks.get(i);
            if (task.getStatus().equals(Task.TO_DO)) {
                final T t = (T) task.getInput();
                symeoConsumer.accept(t);
                this.tasks.set(i, task.done());
                jobStorage.updateJobWithTasksForJobId(jobId, this.tasks);
            }
        }
    }
}
