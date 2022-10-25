package io.symeo.monolithic.backend.infrastructure.github.adapter.mapper;

import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.GithubTagDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.model.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO,
                                               final String githubPlatformName) {
        return Repository.builder()
                .name(githubRepositoryDTO.getName())
                .vcsOrganizationName(githubRepositoryDTO.getOwner().getLogin())
                .id(githubPlatformName + "-" + githubRepositoryDTO.getId().toString())
                .defaultBranch((String) githubRepositoryDTO.getAdditionalProperties().get("default_branch"))
                .build();
    }

    static PullRequest mapPullRequestDtoToDomain(final GithubPullRequestDTO githubPullRequestDTO,
                                                 final String githubPlatformName) {
        return PullRequest.builder()
                .id(githubPlatformName + "-" + githubPullRequestDTO.getId().toString())
                .repository(githubPullRequestDTO.getBase().getRepo().getName())
                .repositoryId(githubPlatformName + "-" + githubPullRequestDTO.getBase().getRepo().getId().toString())
                .title(githubPullRequestDTO.getTitle())
                .number(githubPullRequestDTO.getNumber())
                .isDraft(githubPullRequestDTO.getDraft())
                .isMerged(githubPullRequestDTO.getMerged())
                .addedLineNumber(githubPullRequestDTO.getAdditions())
                .deletedLineNumber(githubPullRequestDTO.getDeletions())
                .commitNumber(githubPullRequestDTO.getCommits())
                .creationDate(githubPullRequestDTO.getCreatedAt())
                .mergeDate(githubPullRequestDTO.getMergedAt())
                .closeDate(githubPullRequestDTO.getClosedAt())
                .lastUpdateDate(githubPullRequestDTO.getUpdatedAt())
                .vcsUrl(githubPullRequestDTO.getHtmlUrl())
                .authorLogin(githubPullRequestDTO.getUser().getLogin())
                .head(githubPullRequestDTO.getHead().getRef())
                .base(githubPullRequestDTO.getBase().getRef())
                .mergeCommitSha(githubPullRequestDTO.getMergeCommitSha())
                .commits(isNull(githubPullRequestDTO.getGithubCommitsDTOS()) ? List.of() :
                        Arrays.stream(githubPullRequestDTO.getGithubCommitsDTOS())
                                .map(GithubMapper::mapCommitToDomain).collect(Collectors.toList()))
                .comments(isNull(githubPullRequestDTO.getGithubCommentsDTOS()) ? List.of() :
                        Arrays.stream(githubPullRequestDTO.getGithubCommentsDTOS())
                                .map(githubCommentsDTO -> mapCommentToDomain(githubCommentsDTO, githubPlatformName)).collect(Collectors.toList()))
                .build();
    }

    static Commit mapCommitToDomain(final GithubCommitsDTO githubCommitsDTO) {
        final GithubCommitsDTO.GithubCommitterDTO committer = githubCommitsDTO.getCommit().getCommitter();
        return Commit.builder()
                .author(committer.getName())
                .sha(githubCommitsDTO.getSha())
                .date(committer.getDate())
                .message(githubCommitsDTO.getCommit().getMessage())
                .parentShaList(isNull(githubCommitsDTO.getParents()) ? List.of() :
                        githubCommitsDTO.getParents().stream().map(GithubCommitsDTO.GithubCommitParentDTO::getSha).toList())
                .build();

    }

    static Comment mapCommentToDomain(final GithubCommentsDTO githubCommentsDTO, final String githubPlatformName) {
        return Comment.builder()
                .id(githubPlatformName + "-" + githubCommentsDTO.getId())
                .creationDate(githubCommentsDTO.getCreationDate())
                .build();
    }

    static Branch mapBranchToDomain(GithubBranchDTO githubBranchDTO) {
        return Branch.builder().name(githubBranchDTO.getName()).build();
    }

    static Tag mapTagToDomain(GithubTagDTO githubTagDTO) {
        return Tag.builder()
                .name(githubTagDTO.getRef().replace("refs/tags/", ""))
                .commitSha(githubTagDTO.getObject().getSha())
                .vcsUrl(githubTagDTO.getVcsApiUrl())
                .build();
    }
}
