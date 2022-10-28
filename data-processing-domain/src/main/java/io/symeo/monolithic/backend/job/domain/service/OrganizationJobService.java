package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.in.OrganizationJobFacade;
import io.symeo.monolithic.backend.job.domain.port.out.AutoSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OrganizationJobService implements OrganizationJobFacade {

    private final VcsOrganizationStorageAdapter vcsOrganizationStorageAdapter;
    private final AutoSymeoDataProcessingJobApiAdapter autoSymeoDataProcessingJobApiAdapter;

    @Override
    public void startAll() throws SymeoException {
        for (VcsOrganization vcsOrganization : vcsOrganizationStorageAdapter.findAllVcsOrganization()) {
            autoSymeoDataProcessingJobApiAdapter.autoStartDataProcessingJobForOrganizationIdAndVcsOrganizationId(
                    vcsOrganization.getOrganizationId(), vcsOrganization.getId()
            );
        }
    }
}
