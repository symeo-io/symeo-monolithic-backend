package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.port.in.JobAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class JobService implements JobAdapter {

    private final JobExpositionStorageAdapter jobExpositionStorageAdapter;
    private final VcsJobService vcsJobService;

    @Override
    public void startToCollectRepositoriesForOrganizationIdAndVcsOrganizationId(UUID organizationId,
                                                                                String vcsOrganizationId) throws SymeoException {

    }

    @Override
    public void startToCollectVcsDataForOrganizationIdAndVcsOrganizationId(UUID organizationId) throws SymeoException {

    }

    @Override
    public void startAll() throws SymeoException {

    }
}
