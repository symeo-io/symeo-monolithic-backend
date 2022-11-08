package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestWithCommentsDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.Commit;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface PullRequestMapper {

    static PullRequestEntity domainToEntity(final PullRequest pullRequest) {
        final PullRequestView pullRequestView = pullRequestToView(pullRequest);
        final PullRequestEntity pullRequestEntity = PullRequestEntity.builder()
                .id(pullRequest.getId())
                .code(pullRequest.getNumber().toString())
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
                .closeDate(isNull(pullRequest.getCloseDate()) ? null :
                        ZonedDateTime.ofInstant(pullRequest.getCloseDate().toInstant(), ZoneId.systemDefault()))
                .title(pullRequest.getTitle())
                .vcsUrl(pullRequest.getVcsUrl())
                .state(pullRequest.getStatus())
                .vcsRepositoryId(pullRequest.getRepositoryId())
                .authorLogin(pullRequest.getAuthorLogin())
                .vcsRepository(pullRequest.getRepository())
                .vcsOrganizationId(pullRequest.getVcsOrganizationId())
                .organizationId(pullRequest.getOrganizationId())
                .head(pullRequest.getHead())
                .base(pullRequest.getBase())
                .size(pullRequestView.getSize())
                .daysOpened(pullRequestView.getDaysOpened(new Date()))
                .mergeCommitSha(pullRequest.getMergeCommitSha())
                .commitShaList(pullRequest.getCommits().stream().map(Commit::getSha).toList())
                .build();
        pullRequestToCommentEntities(pullRequest)
                .forEach(pullRequestEntity::addComment);
        return pullRequestEntity;
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
                .closeDate(isNull(pullRequestEntity.getCloseDate()) ? null :
                        Date.from(pullRequestEntity.getCloseDate().toInstant()))
                .title(pullRequestEntity.getTitle())
                .vcsUrl(pullRequestEntity.getVcsUrl())
                .repositoryId(pullRequestEntity.getVcsRepositoryId())
                .authorLogin(pullRequestEntity.getAuthorLogin())
                .vcsOrganizationId(pullRequestEntity.getVcsOrganizationId())
                .organizationId(pullRequestEntity.getOrganizationId())
                .repository(pullRequestEntity.getVcsRepository())
                .head(pullRequestEntity.getHead())
                .base(pullRequestEntity.getBase())
                .number(Integer.valueOf(pullRequestEntity.getCode()))
                .mergeCommitSha(pullRequestEntity.getMergeCommitSha())
                .build();
    }

    static PullRequestView entityToDomainView(final PullRequestEntity pullRequestEntity) {
        return PullRequestView.builder()
                .id(pullRequestEntity.getId())
                .creationDate(Date.from(pullRequestEntity.getCreationDate().toInstant()))
                .mergeDate(isNull(pullRequestEntity.getMergeDate()) ? null :
                        Date.from(pullRequestEntity.getMergeDate().toInstant()))
                .status(pullRequestEntity.getState())
                .vcsUrl(pullRequestEntity.getVcsUrl())
                .title(pullRequestEntity.getTitle())
                .authorLogin(pullRequestEntity.getAuthorLogin())
                .repository(pullRequestEntity.getVcsRepository())
                .build();
    }

    static PullRequestView fullViewToDomain(final PullRequestFullViewDTO pullRequestFullViewDTO) {
        return PullRequestView.builder()
                .id(pullRequestFullViewDTO.getId())
                .mergeDate(isNull(pullRequestFullViewDTO.getMergeDate()) ? null :
                        Date.from(pullRequestFullViewDTO.getMergeDate().toInstant()))
                .creationDate(Date.from(pullRequestFullViewDTO.getCreationDate().toInstant()))
                .vcsUrl(pullRequestFullViewDTO.getVcsUrl())
                .addedLineNumber(pullRequestFullViewDTO.getAddedLineNumber())
                .deletedLineNumber(pullRequestFullViewDTO.getDeletedLineNumber())
                .closeDate(isNull(pullRequestFullViewDTO.getCloseDate()) ? null :
                        Date.from(pullRequestFullViewDTO.getCloseDate().toInstant()))
                .repository(pullRequestFullViewDTO.getVcsRepository())
                .status(pullRequestFullViewDTO.getState())
                .title(pullRequestFullViewDTO.getTitle())
                .authorLogin(pullRequestFullViewDTO.getAuthorLogin())
                .commitNumber(pullRequestFullViewDTO.getCommitNumber())
                .status(pullRequestFullViewDTO.getState())
                .mergeCommitSha(pullRequestFullViewDTO.getMergeCommitSha())
                .head(pullRequestFullViewDTO.getHead())
                .base(pullRequestFullViewDTO.getBase())
                .build();
    }

    static PullRequestView withCommitsAndCommentsToDomain(final PullRequestWithCommentsDTO pullRequestWithCommentsDTO) {
        return PullRequestView.builder()
                .id(pullRequestWithCommentsDTO.getId())
                .status(pullRequestWithCommentsDTO.getState())
                .mergeDate(isNull(pullRequestWithCommentsDTO.getMergeDate()) ? null :
                        Date.from(pullRequestWithCommentsDTO.getMergeDate().toInstant()))
                .creationDate(Date.from(pullRequestWithCommentsDTO.getCreationDate().toInstant()))
                .comments(pullRequestWithCommentsDTO.getComments().stream().map(CommentMapper::entityToDomain).toList())
                .vcsUrl(pullRequestWithCommentsDTO.getVcsUrl())
                .head(pullRequestWithCommentsDTO.getHead())
                .base(pullRequestWithCommentsDTO.getBase())
                .mergeCommitSha(pullRequestWithCommentsDTO.getMergeCommitSha())
                .commitShaList(pullRequestWithCommentsDTO.getCommitShaList())
                .build();
    }

    Map<String, String> SORTING_PARAMETERS_MAPPING = Map.of(
            "status", "state", "author", "author_login");

    static String sortingParameterToDatabaseAttribute(final String sortingParameter) {
        return SORTING_PARAMETERS_MAPPING.getOrDefault(sortingParameter, sortingParameter);
    }

    static List<CommentEntity> pullRequestToCommentEntities(PullRequest pullRequest) {
        return pullRequest.getComments().stream()
                .map(CommentMapper::domainToEntity)
                .collect(Collectors.toList());
    }

    private static PullRequestView pullRequestToView(PullRequest pullRequest) {
        return PullRequestView.builder()
                .addedLineNumber(pullRequest.getAddedLineNumber())
                .deletedLineNumber(pullRequest.getDeletedLineNumber())
                .creationDate(pullRequest.getCreationDate())
                .closeDate(pullRequest.getCloseDate())
                .mergeDate(pullRequest.getMergeDate())
                .build();
    }
}
