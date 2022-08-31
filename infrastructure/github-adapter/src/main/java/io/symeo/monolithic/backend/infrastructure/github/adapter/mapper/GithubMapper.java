package io.symeo.monolithic.backend.infrastructure.github.adapter.mapper;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO,
                                               final String githubPlatformName) {
        return Repository.builder()
                .name(githubRepositoryDTO.getName())
                .vcsOrganizationId(githubRepositoryDTO.getOwner().getId().toString())
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
                .branchName(githubPullRequestDTO.getHead().getRef())
                .build();
    }

    static Commit mapCommitToDomain(final GithubCommitsDTO githubCommitsDTO) {
        final GithubCommitsDTO.GithubCommitterDTO committer = githubCommitsDTO.getCommit().getCommitter();
        return Commit.builder()
                .author(committer.getName())
                .sha(githubCommitsDTO.getSha())
                .date(committer.getDate())
                .message(githubCommitsDTO.getCommit().getMessage())
                .build();

    }

    static Comment mapCommentToDomain(final GithubCommentsDTO githubCommentsDTO, final String githubPlatformName) {
        return Comment.builder()
                .id(githubPlatformName + "-" + githubCommentsDTO.getId())
                .creationDate(githubCommentsDTO.getCreationDate())
                .build();
    }

}
