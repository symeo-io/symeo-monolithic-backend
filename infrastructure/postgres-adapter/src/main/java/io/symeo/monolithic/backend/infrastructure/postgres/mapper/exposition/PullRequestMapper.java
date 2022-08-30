package io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestFullViewDTO;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.dto.PullRequestWithCommitsAndCommentsDTO;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface PullRequestMapper {

    static PullRequestEntity domainToEntity(final PullRequest pullRequest) {
        final PullRequestView pullRequestView = pullRequest.toView();
        return PullRequestEntity.builder()
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
                .branchName(pullRequest.getBranchName())
                .size(pullRequestView.getSize())
                .daysOpened(pullRequestView.getDaysOpened(new Date()))
                .build();
    }

    static List<CommitEntity> pullRequestToCommitEntities(final PullRequest pullRequest) {
        final String pullRequestId = pullRequest.getId();
        return pullRequest.getCommits().stream()
                .map(commit -> CommitMapper.domainToEntity(commit, pullRequestId))
                .toList();
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
                .branchName(pullRequestEntity.getBranchName())
                .number(Integer.valueOf(pullRequestEntity.getCode()))
                .build();
    }

    static PullRequestView fullViewToDomain(final PullRequestFullViewDTO pullRequestFullViewDTO) {
        return PullRequestView.builder()
                .id(pullRequestFullViewDTO.getId())
                .mergeDate(isNull(pullRequestFullViewDTO.getMergeDate()) ? null :
                        Date.from(pullRequestFullViewDTO.getMergeDate().toInstant()))
                .creationDate(Date.from(pullRequestFullViewDTO.getCreationDate().toInstant()))
                .branchName(pullRequestFullViewDTO.getBranchName())
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
                .build();
    }

    static PullRequestView withCommitsAndCommentsToDomain(final PullRequestWithCommitsAndCommentsDTO pullRequestWithCommitsAndCommentsDTO) {
        return PullRequestView.builder()
                .id(pullRequestWithCommitsAndCommentsDTO.getId())
                .status(pullRequestWithCommitsAndCommentsDTO.getState())
                .mergeDate(isNull(pullRequestWithCommitsAndCommentsDTO.getMergeDate()) ? null :
                        Date.from(pullRequestWithCommitsAndCommentsDTO.getMergeDate().toInstant()))
                .creationDate(Date.from(pullRequestWithCommitsAndCommentsDTO.getCreationDate().toInstant()))
                .comments(pullRequestWithCommitsAndCommentsDTO.getComments().stream().map(CommentMapper::entityToDomain).toList())
                .commits(pullRequestWithCommitsAndCommentsDTO.getCommits().stream().map(CommitMapper::entityToDomain).toList())
                .vcsUrl(pullRequestWithCommitsAndCommentsDTO.getVcsUrl())
                .branchName(pullRequestWithCommitsAndCommentsDTO.getBranchName())
                .build();
    }

    Map<String, String> SORTING_PARAMETERS_MAPPING = Map.of(
            "status", "state", "author", "author_login");

    static String sortingParameterToDatabaseAttribute(final String sortingParameter) {
        return SORTING_PARAMETERS_MAPPING.getOrDefault(sortingParameter, sortingParameter);
    }

    static List<CommentEntity> pullRequestToCommentEntities(PullRequest pullRequest) {
        return pullRequest.getComments().stream()
                .map(comment -> CommentMapper.domainToEntity(comment, pullRequest))
                .collect(Collectors.toList());
    }
}
