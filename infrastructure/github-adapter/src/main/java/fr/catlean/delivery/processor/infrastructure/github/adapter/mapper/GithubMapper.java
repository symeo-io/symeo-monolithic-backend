package fr.catlean.delivery.processor.infrastructure.github.adapter.mapper;

import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.GithubRepositoryDTO;

public interface GithubMapper {

    static Repository mapRepositoryDtoToDomain(final GithubRepositoryDTO githubRepositoryDTO) {
        return Repository.builder().name(githubRepositoryDTO.getName()).build();
    }

    ;
}
