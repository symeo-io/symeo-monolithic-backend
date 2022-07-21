package fr.catlean.monolithic.backend.bootstrap.job;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.DataProcessingJobExecutor;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public class AsyncDataProcessingJobExecutor implements DataProcessingJobExecutor {

    @Override
    @Async
    public void accept(DataProcessingJobAdapter dataProcessingJobAdapter, String vcsOrganizationName) {
        try {
            dataProcessingJobAdapter.start(vcsOrganizationName);
        } catch (CatleanException e) {
            LOGGER.error("Error while running data processing job for vcsOrganizationName {}", vcsOrganizationName, e);
        }
    }
}
