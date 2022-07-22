package fr.catlean.monolithic.backend.bootstrap.job;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.port.in.DataProcessingJobAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;

import java.util.function.BiConsumer;

@Slf4j
public class AsyncDataProcessingJobExecutor implements BiConsumer<DataProcessingJobAdapter, String> {

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
