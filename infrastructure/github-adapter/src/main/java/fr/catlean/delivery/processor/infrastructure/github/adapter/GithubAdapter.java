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
import java.util.Optional;

import static fr.catlean.delivery.processor.infrastructure.github.adapter.mapper.GithubMapper.mapPullRequestDtoToDomain;
import static java.util.Objects.isNull;

@AllArgsConstructor
public class GithubAdapter implements VersionControlSystemAdapter {

    private GithubHttpClient githubHttpClient;
    private GithubProperties properties;

    @Override
    public byte[] getRawRepositories(final String organisation) {
        int page = 1;
        GithubRepositoryDTO[] githubRepositoryDTOS =
                this.githubHttpClient.getRepositoriesForOrganisationName(
                        organisation, page, properties.getSize());
        if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubRepositoryDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubRepositoryDTOS));
        while (githubRepositoryDTOS.length == properties.getSize()) {
            page += 1;
            githubRepositoryDTOS =
                    this.githubHttpClient.getRepositoriesForOrganisationName(
                            organisation, page, properties.getSize());
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
    public List<Repository> repositoriesBytesToDomain(final byte[] repositoriesBytes) {
        try {
            GithubRepositoryDTO[] githubRepositoryDTOS = githubHttpClient.bytesToDto(repositoriesBytes,
                    GithubRepositoryDTO[].class);
            return Arrays.stream(githubRepositoryDTOS).map(GithubMapper::mapRepositoryDtoToDomain).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getRawPullRequestsForRepository(final Repository repository,
                                                  final byte[] alreadyRawCollectedPullRequests) {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                this.githubHttpClient.getPullRequestsForRepositoryAndOrganisation(
                        repository.getOrganisationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    this.githubHttpClient.getPullRequestsForRepositoryAndOrganisation(
                            repository.getOrganisationName(), repository.getName(), page, properties.getSize());
            githubPullRequestDTOList.addAll(Arrays.stream(githubPullRequestDTOS).toList());
        }

        try {
            List<GithubPullRequestDTO> githubDetailedPullRequests = null;
            if (alreadyRawCollectedPullRequests == null || alreadyRawCollectedPullRequests.length == 0) {
                githubDetailedPullRequests = githubPullRequestDTOList.stream().map(githubPullRequestDTO ->
                        githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getOrganisationName(),
                                repository.getName(), githubPullRequestDTO.getNumber())
                ).toList();
            } else {
                githubDetailedPullRequests = getIncrementalGithubPullRequests(repository,
                        alreadyRawCollectedPullRequests,
                        githubPullRequestDTOList);
            }

            return githubHttpClient.dtoToBytes(githubDetailedPullRequests.toArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<GithubPullRequestDTO> getIncrementalGithubPullRequests(Repository repository,
                                                                        byte[] alreadyRawCollectedPullRequests,
                                                                        List<GithubPullRequestDTO> githubPullRequestDTOList) throws IOException {
        List<GithubPullRequestDTO> githubDetailedPullRequests;
        final GithubPullRequestDTO[] alreadyCollectedGithubPullRequestDTOS =
                getGithubPullRequestDTOSFromBytes(alreadyRawCollectedPullRequests);
        githubDetailedPullRequests = new ArrayList<>();

        for (GithubPullRequestDTO currentGithubPullRequestDTO : githubPullRequestDTOList) {
            final Optional<GithubPullRequestDTO> optionalAlreadyCollectedPR =
                    Arrays.stream(alreadyCollectedGithubPullRequestDTOS).filter(
                            alreadyCollectedGithubPullRequestDTO -> alreadyCollectedGithubPullRequestDTO.getId().equals(currentGithubPullRequestDTO.getId())
                    ).findFirst();
            if (optionalAlreadyCollectedPR.isPresent()) {
                if (optionalAlreadyCollectedPR.get().getUpdatedAt().before(currentGithubPullRequestDTO.getUpdatedAt())) {
                    githubDetailedPullRequests.add(
                            githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getOrganisationName(),
                                    repository.getName(), currentGithubPullRequestDTO.getNumber())
                    );
                } else {
                    githubDetailedPullRequests.add(optionalAlreadyCollectedPR.get());
                }
            } else {
                githubDetailedPullRequests.add(
                        githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getOrganisationName(),
                                repository.getName(), currentGithubPullRequestDTO.getNumber())
                );
            }
        }
        return githubDetailedPullRequests;
    }


    @Override
    public List<PullRequest> pullRequestsBytesToDomain(final byte[] bytes) {
        try {
            if (bytes.length == 0) {
                return List.of();
            }
            final GithubPullRequestDTO[] githubPullRequestDTOS = getGithubPullRequestDTOSFromBytes(bytes);
            return Arrays.stream(githubPullRequestDTOS).map(githubPullRequestDTO -> mapPullRequestDtoToDomain(githubPullRequestDTO, this.getName())).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GithubPullRequestDTO[] getGithubPullRequestDTOSFromBytes(byte[] bytes) throws IOException {
        return githubHttpClient.bytesToDto(bytes,
                GithubPullRequestDTO[].class);
    }
}
