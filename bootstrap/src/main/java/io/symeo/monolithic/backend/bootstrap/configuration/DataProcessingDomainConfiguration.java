package io.symeo.monolithic.backend.bootstrap.configuration;

import io.symeo.monolithic.backend.job.domain.github.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.job.JobManager;
import io.symeo.monolithic.backend.job.domain.model.vcs.CycleTimeDataFactory;
import io.symeo.monolithic.backend.job.domain.port.in.DataProcessingJobAdapter;
import io.symeo.monolithic.backend.job.domain.port.in.OrganizationJobFacade;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingJobStorage;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import io.symeo.monolithic.backend.job.domain.service.CycleTimeDataService;
import io.symeo.monolithic.backend.job.domain.service.DataProcessingJobService;
import io.symeo.monolithic.backend.job.domain.service.OrganizationJobService;
import io.symeo.monolithic.backend.job.domain.service.VcsDataProcessingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class DataProcessingDomainConfiguration {

    @Bean
    public DataProcessingJobAdapter dataProcessingJobAdapter(final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter,
                                                             final DataProcessingJobStorage dataProcessingJobStorage,
                                                             final VcsDataProcessingService vcsDataProcessingService,
                                                             final JobManager jobManager,
                                                             final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter,
                                                             final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter) {
        return new DataProcessingJobService(dataProcessingExpositionStorageAdapter, dataProcessingJobStorage,
                vcsDataProcessingService, jobManager, autoSymeoDataProcessingJobApiAdapter,
                vcsOrganizationStorageAdapter);
    }

    @Bean
    public VcsDataProcessingService vcsDataProcessingService(final GithubAdapter githubAdapter,
                                                             final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter,
                                                             final CycleTimeDataService cycleTimeDataService) {
        return new VcsDataProcessingService(githubAdapter, dataProcessingExpositionStorageAdapter,
                cycleTimeDataService);
    }

    @Bean
    CycleTimeDataService cycleTimeDataService(final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter,
                                              final CycleTimeDataFactory cycleTimeDataFactory) {
        return new CycleTimeDataService(dataProcessingExpositionStorageAdapter, cycleTimeDataFactory);
    }

    @Bean
    CycleTimeDataFactory cycleTimeDataFactory() {
        return new CycleTimeDataFactory();
    }

    @Bean
    public JobManager jobManager(final Executor executor,
                                 final DataProcessingJobStorage dataProcessingJobStorage) {
        return new JobManager(executor, dataProcessingJobStorage);
    }

    @Bean
    public OrganizationJobFacade organizationJobFacade(final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter,
                                                       final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter) {
        return new OrganizationJobService(vcsOrganizationStorageAdapter, autoSymeoDataProcessingJobApiAdapter);
    }
}
