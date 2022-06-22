package fr.catlean.monolithic.backend.infrastructure.github.adapter.mapper;

import fr.catlean.monolithic.backend.domain.model.PullRequest;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO) {
        return Repository.builder()
                .name(githubRepositoryDTO.getName())
                .organizationName(githubRepositoryDTO.getOwner().getLogin())
                .build();
    }

    static PullRequest mapPullRequestDtoToDomain(final GithubPullRequestDTO githubPullRequestDTO,
                                                 final String githubPlatformName) {
        return PullRequest.builder()
                .id(githubPlatformName + "-" + githubPullRequestDTO.getId().toString())
                .repository(githubPullRequestDTO.getBase().getRepo().getName())
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
                .build();
    }

}
