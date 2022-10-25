package io.symeo.monolithic.backend.job.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;

import java.util.List;
import java.util.UUID;

public interface JobExpositionStorageAdapter {
    VcsOrganization findVcsOrganizationById(UUID vcsOrganizationId);

    void savePullRequestDetailsWithLinkedComments(List<PullRequest> pullRequests);

    void saveRepositories(List<Repository> repositories);

    void saveCommits(List<Commit> commits) throws SymeoException;

    void saveTags(List<Tag> tags) throws SymeoException;
}
