package io.symeo.monolithic.backend.job.domain.github;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.GithubApiClientAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.RawStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

import static io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper.mapRepositoryDtoToDomain;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@AllArgsConstructor
@Slf4j
public class GithubAdapter {

    private static final String ADAPTER_NAME = "github";
    private final GithubApiClientAdapter githubApiClientAdapter;
    private final RawStorageAdapter rawStorageAdapter;
    private GithubProperties properties;
    private ObjectMapper objectMapper;

    public String getName() {
        return ADAPTER_NAME;
    }

    public List<Repository> getRepositoriesForVcsOrganization(final VcsOrganization vcsOrganization) throws SymeoException {
        int page = 1;
        GithubRepositoryDTO[] githubRepositoryDTOS =
                githubApiClientAdapter.getRepositoriesForOrganizationName(
                        vcsOrganization.getName(), page, properties.getSize());
        if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubRepositoryDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubRepositoryDTOS));
        while (githubRepositoryDTOS.length == properties.getSize()) {
            page += 1;
            githubRepositoryDTOS =
                    githubApiClientAdapter.getRepositoriesForOrganizationName(
                            vcsOrganization.getName(), page, properties.getSize());
            githubRepositoryDTOList.addAll(Arrays.stream(githubRepositoryDTOS).toList());
        }
        rawStorageAdapter.save(vcsOrganization.getOrganizationId(), ADAPTER_NAME, Repository.ALL,
                dtoToBytes(githubRepositoryDTOList));
        return githubRepositoryDTOList.stream()
                .map(githubRepositoryDTO -> mapRepositoryDtoToDomain(githubRepositoryDTO, ADAPTER_NAME))
                .toList();
    }


    public List<PullRequest> getPullRequestsForRepositoryAndDateRange(final Repository repository,
                                                                      final Date startDate,
                                                                      final Date endDate) throws SymeoException {


        final byte[] alreadyRawCollectedPullRequests = getAlreadyRawCollectedPullRequests(repository);

        if (isNull(alreadyRawCollectedPullRequests)) {
            return getPullRequestWithoutAlreadyCollectedPullRequests(repository, startDate, endDate);
        } else {
            final GithubPullRequestDTO[] githubPullRequestDTOSAlreadyCollected =
                    getGithubPullRequestDTOSFromBytes(alreadyRawCollectedPullRequests);
            if (isNull(githubPullRequestDTOSAlreadyCollected) || githubPullRequestDTOSAlreadyCollected.length == 0) {
                return getPullRequestWithoutAlreadyCollectedPullRequests(repository, startDate, endDate);
            } else {
                return getPullRequestsForRepositoryAndAlreadyCollectedPullRequests(repository,
                        alreadyRawCollectedPullRequests);
            }
        }
    }

    private List<PullRequest> getPullRequestWithoutAlreadyCollectedPullRequests(Repository repository, Date startDate
            , Date endDate) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new ArrayList<>();
        }

        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (nonNull(githubPullRequestDTOS) && githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                            repository.getVcsOrganizationName(), repository.getName(), page,
                            properties.getSize());
            if (nonNull(githubPullRequestDTOS)) {
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestDTOS) {
                    if (githubPullRequestDTO.getUpdatedAt().after(startDate)
                            && githubPullRequestDTO.getUpdatedAt().before(endDate)) {
                        githubPullRequestDTOList.add(githubPullRequestDTO);
                    }
                    if (githubPullRequestDTO.getCreatedAt().before(startDate)) {
                        break;
                    }
                }
            }
        }
        return githubPullRequestDTOList.stream()
                .map(githubPullRequestDTO -> GithubMapper.mapPullRequestDtoToDomain(githubPullRequestDTO,
                        ADAPTER_NAME))
                .toList();
    }

    private List<PullRequest> getPullRequestsForRepositoryAndAlreadyCollectedPullRequests(final Repository repository,
                                                                                         final byte[] alreadyRawCollectedPullRequests) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (nonNull(githubPullRequestDTOS) && githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                            repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
            if (nonNull(githubPullRequestDTOS)) {
                githubPullRequestDTOList.addAll(Arrays.stream(githubPullRequestDTOS).toList());
            }
        }
        return getIncrementalGithubPullRequests(repository,
                alreadyRawCollectedPullRequests,
                githubPullRequestDTOList).stream()
                .map(githubPullRequestDTO -> GithubMapper.mapPullRequestDtoToDomain(githubPullRequestDTO,
                        ADAPTER_NAME))
                .toList();

    }

    private byte[] getAlreadyRawCollectedPullRequests(Repository repository) throws SymeoException {
        byte[] alreadyRawCollectedPullRequests = null;
        if (rawStorageAdapter.exists(repository.getOrganizationId(), ADAPTER_NAME,
                PullRequest.getNameFromRepositoryId(repository.getId()))) {
            alreadyRawCollectedPullRequests = rawStorageAdapter.read(repository.getOrganizationId(), ADAPTER_NAME,
                    PullRequest.getNameFromRepositoryId(repository.getId()));
        }
        return alreadyRawCollectedPullRequests;
    }

    private List<GithubPullRequestDTO> getIncrementalGithubPullRequests(Repository repository,
                                                                        byte[] alreadyRawCollectedPullRequests,
                                                                        List<GithubPullRequestDTO> githubPullRequestDTOList)
            throws SymeoException {
        List<GithubPullRequestDTO> githubDetailedPullRequests;
        final GithubPullRequestDTO[] alreadyCollectedGithubPullRequestDTOS =
                getGithubPullRequestDTOSFromBytes(alreadyRawCollectedPullRequests);
        githubDetailedPullRequests = new ArrayList<>();

        for (GithubPullRequestDTO currentGithubPullRequestDTO : githubPullRequestDTOList) {
            final Optional<GithubPullRequestDTO> optionalAlreadyCollectedPR =
                    Arrays.stream(alreadyCollectedGithubPullRequestDTOS).filter(
                            alreadyCollectedGithubPullRequestDTO -> alreadyCollectedGithubPullRequestDTO.getId()
                                    .equals(currentGithubPullRequestDTO.getId())
                    ).findFirst();
            if (optionalAlreadyCollectedPR.isPresent()) {
                if (optionalAlreadyCollectedPR.get().getUpdatedAt().before(currentGithubPullRequestDTO.getUpdatedAt())) {
                    githubDetailedPullRequests.add(
                            getPullRequestDetailsForPullRequestNumber(repository, currentGithubPullRequestDTO)
                    );
                } else {
                    githubDetailedPullRequests.add(optionalAlreadyCollectedPR.get());
                }
            } else {
                githubDetailedPullRequests.add(
                        getPullRequestDetailsForPullRequestNumber(repository, currentGithubPullRequestDTO)
                );
            }
        }
        return githubDetailedPullRequests;
    }


    private GithubPullRequestDTO[] getGithubPullRequestDTOSFromBytes(byte[] bytes) throws SymeoException {
        return bytesToDto(bytes,
                GithubPullRequestDTO[].class);
    }


    private GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(Repository repository,
                                                                           GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        final GithubPullRequestDTO pullRequestDetailsForPullRequestNumber =
                githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), currentGithubPullRequestDTO.getNumber());
        pullRequestDetailsForPullRequestNumber.setGithubCommentsDTOS(getCommentsForPullRequestNumber(repository,
                currentGithubPullRequestDTO));
        pullRequestDetailsForPullRequestNumber.setGithubCommitsDTOS(getCommitsForPullRequestNumber(repository,
                currentGithubPullRequestDTO));
        return pullRequestDetailsForPullRequestNumber;
    }


    private GithubCommentsDTO[] getCommentsForPullRequestNumber(Repository repository,
                                                                GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        int page = 1;
        GithubCommentsDTO[] githubCommentsDTOS =
                githubApiClientAdapter.getCommentsForPullRequestNumber(
                        repository.getVcsOrganizationName(), repository.getName(),
                        currentGithubPullRequestDTO.getNumber(), page, properties.getSize());
        if (isNull(githubCommentsDTOS) || githubCommentsDTOS.length == 0) {
            return new GithubCommentsDTO[0];
        }
        final List<GithubCommentsDTO> githubCommentsDTOList =
                new ArrayList<>(List.of(githubCommentsDTOS));
        while (nonNull(githubCommentsDTOS) && githubCommentsDTOS.length == properties.getSize()) {
            page += 1;
            githubCommentsDTOS = githubApiClientAdapter.getCommentsForPullRequestNumber(
                    repository.getVcsOrganizationName(), repository.getName(),
                    currentGithubPullRequestDTO.getNumber(), page, properties.getSize()
            );
            if (nonNull(githubCommentsDTOS)) {
                githubCommentsDTOList.addAll(Arrays.stream(githubCommentsDTOS).toList());
            }
        }
        return bytesToDto(dtoToBytes(githubCommentsDTOList.toArray()), GithubCommentsDTO[].class);
    }

    private GithubCommitsDTO[] getCommitsForPullRequestNumber(Repository repository,
                                                              GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        int page = 1;
        GithubCommitsDTO[] githubCommitsDTOS =
                githubApiClientAdapter.getCommitsForPullRequestNumber(
                        repository.getVcsOrganizationName(), repository.getName(),
                        currentGithubPullRequestDTO.getNumber(), page, properties.getSize());
        if (isNull(githubCommitsDTOS) || githubCommitsDTOS.length == 0) {
            return new GithubCommitsDTO[0];
        }
        final List<GithubCommitsDTO> githubCommitsDTOList =
                new ArrayList<>(List.of(githubCommitsDTOS));
        while (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length == properties.getSize()) {
            page += 1;
            githubCommitsDTOS = githubApiClientAdapter.getCommitsForPullRequestNumber(
                    repository.getVcsOrganizationName(), repository.getName(),
                    currentGithubPullRequestDTO.getNumber(), page, properties.getSize()
            );
            if (nonNull(githubCommitsDTOS)) {
                githubCommitsDTOList.addAll(Arrays.stream(githubCommitsDTOS).toList());
            }
        }
        return bytesToDto(dtoToBytes(githubCommitsDTOList.toArray()), GithubCommitsDTO[].class);
    }

    private <T> T bytesToDto(byte[] bytes, Class<T> tClass) throws SymeoException {
        try {
            return objectMapper.readValue(bytes, tClass);
        } catch (IOException e) {
            final String message = String.format("Failed to map byte[] to class %s", tClass.getName());
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.GITHUB_JSON_MAPPING_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }


    private <T> byte[] dtoToBytes(T t) throws SymeoException {
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            final String message = String.format("Failed to serialize class %s to byte[]", t.getClass().getName());
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.GITHUB_JSON_MAPPING_ERROR)
                    .message(message)
                    .rootException(e)
                    .build();
        }
    }
}
