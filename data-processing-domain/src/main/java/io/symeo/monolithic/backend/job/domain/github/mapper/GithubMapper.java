package io.symeo.monolithic.backend.job.domain.github.mapper;

import io.symeo.monolithic.backend.job.domain.github.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO,
                                               final String githubPlatformName,
                                               final VcsOrganization vcsOrganization) {
        return Repository.builder()
                .name(githubRepositoryDTO.getName())
                .vcsOrganizationName(githubRepositoryDTO.getOwner().getLogin())
                .id(githubPlatformName + "-" + githubRepositoryDTO.getId().toString())
                .defaultBranch((String) githubRepositoryDTO.getAdditionalProperties().get("default_branch"))
                .vcsOrganizationId(vcsOrganization.getVcsId())
                .organizationId(vcsOrganization.getOrganizationId())
                .vcsOrganizationName(vcsOrganization.getName())
                .build();
    }

    static PullRequest mapPullRequestDtoToDomain(final GithubPullRequestDTO githubPullRequestDTO,
                                                 final String githubPlatformName, final Repository repository) {
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
                .vcsOrganizationId(repository.getVcsOrganizationId())
                .commits(isNull(githubPullRequestDTO.getGithubCommitsDTOS()) ? List.of() :
                        Arrays.stream(githubPullRequestDTO.getGithubCommitsDTOS())
                                .map((GithubCommitsDTO githubCommitsDTO) -> mapCommitToDomain(githubCommitsDTO,
                                        repository)).collect(Collectors.toList()))
                .comments(isNull(githubPullRequestDTO.getGithubCommentsDTOS()) ? List.of() :
                        Arrays.stream(githubPullRequestDTO.getGithubCommentsDTOS())
                                .map(githubCommentsDTO -> mapCommentToDomain(githubCommentsDTO, githubPlatformName)).collect(Collectors.toList()))
                .build();
    }

    static Commit mapCommitToDomain(final GithubCommitsDTO githubCommitsDTO, final Repository repository) {
        final GithubCommitsDTO.GithubCommitterDTO committer = githubCommitsDTO.getCommit().getCommitter();
        return Commit.builder()
                .repositoryId(repository.getId())
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

    static Tag mapTagToDomain(final GithubTagDTO githubTagDTO, final Repository repository) {
        return Tag.builder()
                .name(githubTagDTO.getRef().replace("refs/tags/", ""))
                .commitSha(githubTagDTO.getObject().getSha())
                .vcsUrl(githubTagDTO.getVcsApiUrl())
                .repositoryId(repository.getId())
                .build();
    }
}
