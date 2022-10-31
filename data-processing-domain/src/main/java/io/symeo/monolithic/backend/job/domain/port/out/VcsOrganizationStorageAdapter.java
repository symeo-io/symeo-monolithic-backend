package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;

import java.util.List;

public interface VcsOrganizationStorageAdapter {

    List<VcsOrganization> findAllVcsOrganization() throws SymeoException;
}
