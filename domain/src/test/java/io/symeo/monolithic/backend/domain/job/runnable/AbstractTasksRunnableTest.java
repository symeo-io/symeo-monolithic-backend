package io.symeo.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AbstractTasksRunnableTest {

    private final static Faker faker = new Faker();

    @Test
    void should_execute_all_tasks_given_tasks_and_symeo_consumer() throws SymeoException {
        // Given
        final List<Task> tasks = new ArrayList<>(List.of(
                Task.builder()
                        .input(Repository.builder().id(faker.rickAndMorty().character()).build())
                        .build(),
                Task.builder()
                        .input(Repository.builder().id(faker.dragonBall().character()).build())
                        .build())
        );
        final SymeoConsumer<Repository> symeoConsumer = mock(SymeoConsumer.class);
        final SimpleTasksRunnableImplementation simpleTasksRunnableImplementation =
                new SimpleTasksRunnableImplementation(tasks);

        // When
        simpleTasksRunnableImplementation.run(symeoConsumer);

        // Then
        verify(symeoConsumer, times(1)).accept((Repository) tasks.get(0).getInput());
        verify(symeoConsumer, times(1)).accept((Repository) tasks.get(1).getInput());
        final List<Task> executedTasks = simpleTasksRunnableImplementation.getTasks();
        assertThat(executedTasks).hasSize(tasks.size());
        executedTasks.forEach(task -> assertThat(task.getStatus()).isEqualTo(Task.DONE));
    }

    private static class SimpleTasksRunnableImplementation extends AbstractTasksRunnable<Repository> {

        public SimpleTasksRunnableImplementation(List<Task> tasks) {
            this.tasks = tasks;
        }

        public void run(SymeoConsumer<Repository> symeoConsumer) throws SymeoException {
            executeAllTasks(symeoConsumer);
        }

    }
}
