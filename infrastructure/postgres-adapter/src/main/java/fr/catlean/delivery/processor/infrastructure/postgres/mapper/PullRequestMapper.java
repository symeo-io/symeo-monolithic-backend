package fr.catlean.delivery.processor.infrastructure.postgres.mapper;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.infrastructure.postgres.entity.PullRequestEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;

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
                .state(pullRequest.getState())
                .authorLogin(pullRequest.getAuthorLogin())
                .vcsId(pullRequest.getNumber())
                .vcsRepository(pullRequest.getRepository())
                .team(pullRequest.getTeam())
                .vcsOrganization(pullRequest.getVcsOrganization())
                .organization(pullRequest.getOrganization())
                .size(pullRequest.getSize())
                .daysOpened(pullRequest.getDaysOpened())
                .startDateRange(pullRequest.getStartDateRange())
                .build();
    }
}
