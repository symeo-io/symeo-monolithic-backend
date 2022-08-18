package io.symeo.monolithic.backend.infrastructure.github.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.PullRequest;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;

@AllArgsConstructor
@Slf4j
public class GithubAdapter implements VersionControlSystemAdapter {

    private GithubHttpClient githubHttpClient;
    private GithubProperties properties;
    private ObjectMapper objectMapper;

    @Override
    public byte[] getRawRepositories(final String vcsOrganizationName) throws SymeoException {
        int page = 1;
        GithubRepositoryDTO[] githubRepositoryDTOS =
                this.githubHttpClient.getRepositoriesForOrganizationName(
                        vcsOrganizationName, page, properties.getSize());
        if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubRepositoryDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubRepositoryDTOS));
        while (githubRepositoryDTOS.length == properties.getSize()) {
            page += 1;
            githubRepositoryDTOS =
                    this.githubHttpClient.getRepositoriesForOrganizationName(
                            vcsOrganizationName, page, properties.getSize());
            githubRepositoryDTOList.addAll(Arrays.stream(githubRepositoryDTOS).toList());
        }

        try {
            return dtoToBytes(githubRepositoryDTOList.toArray());
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
            GithubRepositoryDTO[] githubRepositoryDTOS = bytesToDto(repositoriesBytes,
                    GithubRepositoryDTO[].class);
            return Arrays.stream(githubRepositoryDTOS).map(githubRepositoryDTO -> GithubMapper.mapRepositoryDtoToDomain(githubRepositoryDTO, getName())).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getRawPullRequestsForRepository(final Repository repository,
                                                  final byte[] alreadyRawCollectedPullRequests) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                this.githubHttpClient.getPullRequestsForRepositoryAndOrganization(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    this.githubHttpClient.getPullRequestsForRepositoryAndOrganization(
                            repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
            githubPullRequestDTOList.addAll(Arrays.stream(githubPullRequestDTOS).toList());
        }

        try {
            List<GithubPullRequestDTO> githubDetailedPullRequests = null;
            if (alreadyRawCollectedPullRequests == null || alreadyRawCollectedPullRequests.length == 0) {
                githubDetailedPullRequests = new ArrayList<>(getGithubPullRequestDTOsWithParallelStream(repository,
                        githubPullRequestDTOList));
            } else {
                githubDetailedPullRequests = getIncrementalGithubPullRequests(repository,
                        alreadyRawCollectedPullRequests,
                        githubPullRequestDTOList);
            }

            return dtoToBytes(githubDetailedPullRequests.toArray());
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }

    }

    private List<GithubPullRequestDTO> getGithubPullRequestDTOsWithParallelStream(Repository repository,
                                                                                  List<GithubPullRequestDTO> githubPullRequestDTOList) {
        return githubPullRequestDTOList.parallelStream()
                .map(githubPullRequestDTO -> {
                    try {
                        return Optional.of(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                                repository.getName(), githubPullRequestDTO.getNumber()));
                    } catch (SymeoException ex) {
                        LOGGER.error("Error while getting PR from github", ex);
                    }
                    return Optional.empty();

                })
                .filter(Optional::isPresent)
                .map(o -> (GithubPullRequestDTO) o.get())
                .toList();
    }

    private List<GithubPullRequestDTO> getIncrementalGithubPullRequests(Repository repository,
                                                                        byte[] alreadyRawCollectedPullRequests,
                                                                        List<GithubPullRequestDTO> githubPullRequestDTOList) throws IOException, SymeoException {
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
                            githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                                    repository.getName(), currentGithubPullRequestDTO.getNumber())
                    );
                } else {
                    githubDetailedPullRequests.add(optionalAlreadyCollectedPR.get());
                }
            } else {
                githubDetailedPullRequests.add(
                        githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
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
            return Arrays.stream(githubPullRequestDTOS).map(githubPullRequestDTO -> GithubMapper.mapPullRequestDtoToDomain(githubPullRequestDTO, this.getName())).toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private GithubPullRequestDTO[] getGithubPullRequestDTOSFromBytes(byte[] bytes) throws IOException {
        return bytesToDto(bytes,
                GithubPullRequestDTO[].class);
    }


    public <T> byte[] dtoToBytes(T t) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(t);
    }

    public <T> T bytesToDto(byte[] bytes, Class<T> tClass) throws IOException {
        return objectMapper.readValue(bytes, tClass);
    }
}
