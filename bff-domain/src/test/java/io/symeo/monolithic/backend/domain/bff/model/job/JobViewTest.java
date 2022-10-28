package io.symeo.monolithic.backend.domain.bff.model.job;

import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.bff.model.job.TaskView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JobViewTest {

    @Test
    void should_return_job_progression_from_tasks() {
        // Given
        final JobView job = JobView.builder()
                .organizationId(UUID.randomUUID())
                .tasks(
                        List.of(
                                TaskView.builder().status("DONE").input(1L).build(),
                                TaskView.builder().status("DONE").input(2L).build(),
                                TaskView.builder().status("TO_DO").input(3L).build()
                        )
                )
                .build();

        // When
        final double progressionPercentage = job.getProgressionPercentage();

        // Then
        assertThat(progressionPercentage).isEqualTo(0.67);
    }
}
