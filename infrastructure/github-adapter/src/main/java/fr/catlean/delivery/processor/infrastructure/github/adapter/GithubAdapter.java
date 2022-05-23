package fr.catlean.delivery.processor.infrastructure.github.adapter;

import fr.catlean.delivery.processor.domain.model.IRepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.model.RepositoryCommitMetrics;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.GithubRepositoryDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.mapper.GithubMapper;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class GithubAdapter implements VersionControlSystemAdapter {

  private GithubHttpClient githubHttpClient;
  private Integer size;

  @Override
  public List<Repository> getRepositoriesForOrganisationName(String organisationName) {
    Integer page = 1;
    GithubRepositoryDTO[] githubRepositoryDTOS =
        this.githubHttpClient.getRepositoriesForOrganisationName(organisationName, page, size);
    if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
      return List.of();
    }
    final List<Repository> repositories =
        new ArrayList<>(
            Arrays.stream(githubRepositoryDTOS)
                .map(GithubMapper::mapGithubRepositoryDtoToDomain)
                .toList());
    while (githubRepositoryDTOS.length == size) {
      page += 1;
      githubRepositoryDTOS =
          this.githubHttpClient.getRepositoriesForOrganisationName(organisationName, page, size);
      repositories.addAll(
          Arrays.stream(githubRepositoryDTOS)
              .map(GithubMapper::mapGithubRepositoryDtoToDomain)
              .toList());
    }
    return repositories;
  }

  @Override
  public IRepositoryCommitMetrics getCommitMetricsForRepository(String repositoryName) {
    return null;
  }

  @Override
  public RepositoryCommitMetrics mapToDomain(IRepositoryCommitMetrics IRepositoryCommitMetrics1) {
    return null;
  }
}
