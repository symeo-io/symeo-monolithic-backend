package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.JobExpositionStorageAdapter;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
public class VcsJobService {

    private final GithubAdapter githubAdapter;
    private final JobExpositionStorageAdapter jobExpositionStorageAdapter;

    public void collectRepositoriesForVcsOrganization(VcsOrganization vcsOrganization) throws SymeoException {
        final List<Repository> repositoriesForVcsOrganizationName =
                githubAdapter.getRepositoriesForVcsOrganization(vcsOrganization);
        jobExpositionStorageAdapter.saveRepositories(repositoriesForVcsOrganizationName);
    }

    public void collectVcsDataForRepositoryAndDateRange(
            Repository repository, Date startDate,
            Date endDate) {

    }
}
