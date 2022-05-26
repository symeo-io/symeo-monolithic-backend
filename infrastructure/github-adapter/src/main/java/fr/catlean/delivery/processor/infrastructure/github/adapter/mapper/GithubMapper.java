package fr.catlean.delivery.processor.infrastructure.github.adapter.mapper;

import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO) {
        return Repository.builder()
                .name(githubRepositoryDTO.getName())
                .organisationName(githubRepositoryDTO.getOwner().getLogin())
                .build();
    }

    static PullRequest mapPullRequestDtoToDomain(final GithubPullRequestDTO githubPullRequestDTO) {
        return PullRequest.builder()
                .id(githubPullRequestDTO.getId())
                .build();
    }

}
