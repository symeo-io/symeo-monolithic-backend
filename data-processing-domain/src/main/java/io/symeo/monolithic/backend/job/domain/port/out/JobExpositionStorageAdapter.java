package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobExpositionStorageAdapter {
    Optional<VcsOrganization> findVcsOrganizationByIdAndOrganizationId(Long vcsOrganizationId, UUID organizationId) throws SymeoException;

    void savePullRequestDetailsWithLinkedComments(List<PullRequest> pullRequests) throws SymeoException;

    void saveRepositories(List<Repository> repositories) throws SymeoException;

    void saveCommits(List<Commit> commits) throws SymeoException;

    void saveTags(List<Tag> tags) throws SymeoException;

    List<Repository> findAllRepositoriesByIds(List<String> repositoryIds) throws SymeoException;
}
