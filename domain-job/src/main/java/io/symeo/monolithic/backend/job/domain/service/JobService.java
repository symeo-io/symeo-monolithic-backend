package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.job.domain.model.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JobService {

    private final JobExpositionStorageAdapter jobExpositionStorageAdapter;
    private final VcsJobService vcsJobService;

    public void startRepositoriesCollection(final UUID organizationId, final UUID vcsOrganizationId) {
        final VcsOrganization vcsOrganizationById =
                jobExpositionStorageAdapter.findVcsOrganizationById(vcsOrganizationId);
        vcsJobService.collectRepositoriesForVcsOrganization(vcsOrganizationById);
    }

    public void startVcsDataCollection(final UUID organizationId, final UUID vcsOrganizationId,
                                       final List<String> repositoryIds) {


    }
}
