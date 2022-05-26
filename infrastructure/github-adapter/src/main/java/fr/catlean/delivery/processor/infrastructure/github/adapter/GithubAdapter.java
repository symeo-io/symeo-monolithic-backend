package fr.catlean.delivery.processor.infrastructure.github.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.Repository;
import fr.catlean.delivery.processor.domain.port.out.VersionControlSystemAdapter;
import fr.catlean.delivery.processor.infrastructure.github.adapter.client.GithubHttpClient;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import fr.catlean.delivery.processor.infrastructure.github.adapter.mapper.GithubMapper;
import fr.catlean.delivery.processor.infrastructure.github.adapter.properties.GithubProperties;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static java.util.Objects.isNull;

@AllArgsConstructor
public class GithubAdapter implements VersionControlSystemAdapter {

    private GithubHttpClient githubHttpClient;
    private GithubProperties properties;

    @Override
    public byte[] getRawRepositories(String organisation) {
        int page = 1;
        GithubRepositoryDTO[] githubRepositoryDTOS =
                this.githubHttpClient.getRepositoriesForOrganisationName(
                        organisation, page, properties.getSize(), properties.getToken());
        if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubRepositoryDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubRepositoryDTOS));
        while (githubRepositoryDTOS.length == properties.getSize()) {
            page += 1;
            githubRepositoryDTOS =
                    this.githubHttpClient.getRepositoriesForOrganisationName(
                            organisation, page, properties.getSize(), properties.getToken());
            githubRepositoryDTOList.addAll(Arrays.stream(githubRepositoryDTOS).toList());
        }

        try {
            return githubHttpClient.dtoToBytes(githubRepositoryDTOList.toArray());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "github";
    }


    @Override
    public List<Repository> repositoriesBytesToDomain(byte[] repositoriesBytes) {
        try {
            GithubRepositoryDTO[] githubRepositoryDTOS = githubHttpClient.bytesToDto(repositoriesBytes,
                    GithubRepositoryDTO[].class);
            return Arrays.stream(githubRepositoryDTOS).map(GithubMapper::mapRepositoryDtoToDomain).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getRawPullRequestsForRepository(Repository repository) {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                this.githubHttpClient.getPullRequestsForRepositoryAndOrganisation(
                        repository.getOrganisationName(), repository.getName(), page, properties.getSize(),
                        properties.getToken());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubPullRequestDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    this.githubHttpClient.getPullRequestsForRepositoryAndOrganisation(
                            repository.getOrganisationName(), repository.getName(), page, properties.getSize(),
                            properties.getToken());
            githubRepositoryDTOList.addAll(Arrays.stream(githubPullRequestDTOS).toList());
        }

        try {
            ForkJoinPool customThreadPool = new ForkJoinPool(2);
            final List<GithubPullRequestDTO> githubDetailedPullRequests =
                    customThreadPool.submit(() -> githubRepositoryDTOList.parallelStream()
                            .map(githubPullRequestDTO -> githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getOrganisationName(),
                                    repository.getName(), githubPullRequestDTO.getNumber(), properties.getToken())).toList()).get();
            return githubHttpClient.dtoToBytes(githubDetailedPullRequests.toArray());
        } catch (JsonProcessingException | InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<PullRequest> pullRequestsBytesToDomain(byte[] bytes) {
        try {
            if (bytes.length == 0) {
                return List.of();
            }
            final GithubPullRequestDTO[] githubPullRequestDTOS = githubHttpClient.bytesToDto(bytes,
                    GithubPullRequestDTO[].class);
            return Arrays.stream(githubPullRequestDTOS).map(GithubMapper::mapPullRequestDtoToDomain).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}