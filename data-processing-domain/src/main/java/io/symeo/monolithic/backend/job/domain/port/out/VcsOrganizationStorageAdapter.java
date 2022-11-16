package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.organization.OrganizationSettingsView;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;

import java.util.List;
import java.util.UUID;

public interface VcsOrganizationStorageAdapter {

    List<VcsOrganization> findAllVcsOrganization() throws SymeoException;

    OrganizationSettingsView findOrganizationSettingsViewForOrganizationId(UUID organizationId) throws SymeoException;
}
