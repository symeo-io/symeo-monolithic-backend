package fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition;

import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static java.util.Objects.isNull;

public interface PullRequestMapper {

    static PullRequestEntity domainToEntity(final PullRequest pullRequest) {
        return PullRequestEntity.builder()
                .id(pullRequest.getId())
                .isDraft(pullRequest.getIsDraft())
                .isMerged(pullRequest.getIsMerged())
                .commitNumber(pullRequest.getCommitNumber())
                .addedLineNumber(pullRequest.getAddedLineNumber())
                .deletedLineNumber(pullRequest.getDeletedLineNumber())
                .creationDate(ZonedDateTime.ofInstant(pullRequest.getCreationDate().toInstant(),
                        ZoneId.systemDefault()))
                .lastUpdateDate(ZonedDateTime.ofInstant(pullRequest.getLastUpdateDate().toInstant(),
                        ZoneId.systemDefault()))
                .mergeDate(isNull(pullRequest.getMergeDate()) ? null :
                        ZonedDateTime.ofInstant(pullRequest.getMergeDate().toInstant(), ZoneId.systemDefault()))
                .title(pullRequest.getTitle())
                .vcsUrl(pullRequest.getVcsUrl())
                .state(pullRequest.getStatus())
                .vcsRepositoryId(pullRequest.getRepositoryId())
                .authorLogin(pullRequest.getAuthorLogin())
                .vcsRepository(pullRequest.getRepository())
                .vcsOrganization(pullRequest.getVcsOrganization())
                .organizationId(pullRequest.getOrganizationId())
                .size(pullRequest.getSize())
                .daysOpened(pullRequest.getDaysOpened())
                .startDateRange(pullRequest.getStartDateRange())
                .build();
    }

    static PullRequest entityToDomain(final PullRequestEntity pullRequestEntity) {
        return PullRequest.builder()
                .id(pullRequestEntity.getId())
                .isDraft(pullRequestEntity.getIsDraft())
                .isMerged(pullRequestEntity.getIsMerged())
                .commitNumber(pullRequestEntity.getCommitNumber())
                .addedLineNumber(pullRequestEntity.getAddedLineNumber())
                .deletedLineNumber(pullRequestEntity.getDeletedLineNumber())
                .creationDate(Date.from(pullRequestEntity.getCreationDate().toInstant()))
                .lastUpdateDate(Date.from(pullRequestEntity.getLastUpdateDate().toInstant()))
                .mergeDate(isNull(pullRequestEntity.getMergeDate()) ? null :
                        Date.from(pullRequestEntity.getMergeDate().toInstant()))
                .title(pullRequestEntity.getTitle())
                .vcsUrl(pullRequestEntity.getVcsUrl())
                .repositoryId(pullRequestEntity.getVcsRepositoryId())
                .authorLogin(pullRequestEntity.getAuthorLogin())
                .vcsOrganization(pullRequestEntity.getVcsOrganization())
                .organizationId(pullRequestEntity.getOrganizationId())
                .repository(pullRequestEntity.getVcsRepository())
                .build();
    }
}
