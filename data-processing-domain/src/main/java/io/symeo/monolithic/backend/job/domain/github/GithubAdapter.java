package io.symeo.monolithic.backend.job.domain.github;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.GithubTagDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.GithubApiClientAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.RawStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper.mapCommitToDomain;
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
                githubApiClientAdapter.getRepositoriesForVcsOrganizationName(
                        vcsOrganization.getName(), page, properties.getSize());
        if (isNull(githubRepositoryDTOS) || githubRepositoryDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubRepositoryDTO> githubRepositoryDTOList =
                new ArrayList<>(List.of(githubRepositoryDTOS));
        while (githubRepositoryDTOS.length == properties.getSize()) {
            page += 1;
            githubRepositoryDTOS =
                    githubApiClientAdapter.getRepositoriesForVcsOrganizationName(
                            vcsOrganization.getName(), page, properties.getSize());
            githubRepositoryDTOList.addAll(Arrays.stream(githubRepositoryDTOS).toList());
        }
        rawStorageAdapter.save(vcsOrganization.getOrganizationId(), ADAPTER_NAME, Repository.ALL,
                dtoToBytes(githubRepositoryDTOList));
        return githubRepositoryDTOList.stream()
                .map(githubRepositoryDTO -> mapRepositoryDtoToDomain(githubRepositoryDTO, ADAPTER_NAME,
                        vcsOrganization))
                .toList();
    }


    public List<PullRequest> getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(final Repository repository,
                                                                                        final Date startDate,
                                                                                        final Date endDate) throws SymeoException {


        final byte[] alreadyRawCollectedPullRequests = getAlreadyRawCollectedPullRequests(repository);
        List<GithubPullRequestDTO> githubPullRequestDTOList;
        if (isNull(alreadyRawCollectedPullRequests)) {
            githubPullRequestDTOList = getPullRequestWithoutAlreadyCollectedPullRequests(repository, startDate,
                    endDate);
        } else {
            final GithubPullRequestDTO[] githubPullRequestDTOSAlreadyCollected =
                    getGithubPullRequestDTOSFromBytes(alreadyRawCollectedPullRequests);
            if (isNull(githubPullRequestDTOSAlreadyCollected) || githubPullRequestDTOSAlreadyCollected.length == 0) {
                githubPullRequestDTOList = getPullRequestWithoutAlreadyCollectedPullRequests(repository, startDate,
                        endDate);
            } else {
                githubPullRequestDTOList = getPullRequestsForRepositoryAndAlreadyCollectedPullRequests(repository,
                        alreadyRawCollectedPullRequests, startDate, endDate);
            }
        }
        rawStorageAdapter.save(repository.getOrganizationId(), this.getName(),
                PullRequest.getNameFromRepositoryId(repository.getId()),
                dtoToBytes(githubPullRequestDTOList));
        return githubPullRequestDTOList
                .stream()
                .map(githubPullRequestDTO -> GithubMapper.mapPullRequestDtoToDomain(githubPullRequestDTO,
                        ADAPTER_NAME, repository))
                .toList();
    }

    public List<Tag> getTags(final Repository repository) throws SymeoException {
        final GithubTagDTO[] githubTagDTOS =
                githubApiClientAdapter.getTagsForVcsOrganizationAndRepository(repository.getVcsOrganizationName(),
                        repository.getName());
        for (GithubTagDTO githubTagDTO : githubTagDTOS) {
            githubTagDTO.setVcsApiUrl(
                    properties.getUrlHost() + repository.getVcsOrganizationName() + "/" + repository.getName() +
                            "/tree/" + githubTagDTO.getRef().replace("refs/tags/", "")
            );
        }
        rawStorageAdapter.save(repository.getOrganizationId(), this.getName(), Tag.getNameFromRepository(repository),
                dtoToBytes(githubTagDTOS));
        return Arrays.stream(githubTagDTOS)
                .map(githubTagDTO -> GithubMapper.mapTagToDomain(githubTagDTO, repository))
                .toList();
    }

    public List<Branch> getBranches(final Repository repository) throws SymeoException {

        int page = 1;
        GithubBranchDTO[] githubBranchDTOS =
                this.githubApiClientAdapter.getBranchesForVcsOrganizationAndRepository(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubBranchDTOS) || githubBranchDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubBranchDTO> githubBranchDTOList =
                new ArrayList<>(List.of(githubBranchDTOS));
        while (githubBranchDTOS.length == properties.getSize()) {
            page += 1;
            githubBranchDTOS =
                    this.githubApiClientAdapter.getBranchesForVcsOrganizationAndRepository(
                            repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
            githubBranchDTOList.addAll(Arrays.stream(githubBranchDTOS).toList());
        }
        rawStorageAdapter.save(repository.getOrganizationId(), this.getName(), Branch.getNameFromRepository(repository),
                dtoToBytes(githubBranchDTOList));
        return githubBranchDTOList.stream()
                .map(GithubMapper::mapBranchToDomain)
                .toList();
    }

    private List<GithubPullRequestDTO> getPullRequestWithoutAlreadyCollectedPullRequests(Repository repository,
                                                                                         Date startDate
            , Date endDate) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>();
        for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestDTOS) {
            if (githubPullRequestDTO.getUpdatedAt().after(startDate)
                    && githubPullRequestDTO.getUpdatedAt().before(endDate)) {
                githubPullRequestDTOList.add(githubPullRequestDTO);
            }
        }
        while (nonNull(githubPullRequestDTOS) && githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(
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
        return getGithubPullRequestDTOs(repository, githubPullRequestDTOList);
    }

    public List<Commit> getCommitsForBranchesInDateRange(final Repository repository,
                                                         final List<String> branchNames,
                                                         final Date startDate,
                                                         final Date endDate)
            throws SymeoException {
        final Set<GithubCommitsDTO> deduplicatedGithubCommitsDTOs = new HashSet<>();
        for (String branchName : branchNames) {
            int page = 1;
            GithubCommitsDTO[] githubCommitsDTOS =
                    githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(
                            repository.getVcsOrganizationName(), repository.getName(), branchName, startDate, endDate
                            , page,
                            properties.getSize()
                    );

            if (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length > 0) {
                deduplicatedGithubCommitsDTOs.addAll(Arrays.stream(githubCommitsDTOS).toList());
            }

            while (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length == properties.getSize()) {
                page += 1;
                githubCommitsDTOS =
                        githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(
                                repository.getVcsOrganizationName(), repository.getName(), branchName, startDate,
                                endDate
                                , page,
                                properties.getSize()
                        );
                if (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length > 0) {
                    deduplicatedGithubCommitsDTOs.addAll(Arrays.stream(githubCommitsDTOS).toList());
                }
            }
        }
        if (rawStorageAdapter.exists(repository.getOrganizationId(), this.getName(),
                Commit.getNameForRepository(repository))) {
            final GithubCommitsDTO[] githubCommitsDTOS =
                    bytesToDto(rawStorageAdapter.read(repository.getOrganizationId(), this.getName(),
                                    Commit.getNameForRepository(repository)),
                            GithubCommitsDTO[].class);
            deduplicatedGithubCommitsDTOs.addAll(Arrays.stream(githubCommitsDTOS).toList());
        }
        rawStorageAdapter.save(repository.getOrganizationId(), this.getName(), Commit.getNameForRepository(repository),
                dtoToBytes(deduplicatedGithubCommitsDTOs.toArray()));
        return deduplicatedGithubCommitsDTOs.stream()
                .map(githubCommitsDTO -> mapCommitToDomain(githubCommitsDTO, repository))
                .toList();
    }

    public List<Commit> getCommitsForBranches(final Repository repository, final List<String> branchNames) throws SymeoException {
        final Set<GithubCommitsDTO> deduplicatedGithubCommitsDTOs = new HashSet<>();
        try {
            new ForkJoinPool(2).submit(() -> {
                branchNames.parallelStream()
                        .map(branchName -> {
                            try {
                                return getCommitsForBranch(repository, branchName);
                            } catch (SymeoException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .forEach(deduplicatedGithubCommitsDTOs::addAll);
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        rawStorageAdapter.save(repository.getOrganizationId(), this.getName(), Commit.getNameForRepository(repository),
                dtoToBytes(deduplicatedGithubCommitsDTOs.toArray()));
        return deduplicatedGithubCommitsDTOs.stream()
                .map(githubCommitsDTO -> mapCommitToDomain(githubCommitsDTO, repository))
                .toList();
    }

    private List<GithubCommitsDTO> getCommitsForBranch(Repository repository, final String branchName) throws SymeoException {
        int page = 1;
        final List<GithubCommitsDTO> allGithubCommitsDTOS = new ArrayList<>();
        GithubCommitsDTO[] githubCommitsDTOS =
                githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranch(
                        repository.getVcsOrganizationName(), repository.getName(), branchName, page,
                        properties.getSize()
                );

        if (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length > 0) {
            allGithubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS).toList());
        }

        while (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length == properties.getSize()) {
            page += 1;
            githubCommitsDTOS =
                    githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranch(
                            repository.getVcsOrganizationName(), repository.getName(), branchName, page,
                            properties.getSize()
                    );
            if (nonNull(githubCommitsDTOS) && githubCommitsDTOS.length > 0) {
                allGithubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS).toList());
            }
        }
        return allGithubCommitsDTOS;
    }

    private List<GithubPullRequestDTO> getPullRequestsForRepositoryAndAlreadyCollectedPullRequests(final Repository repository,
                                                                                                   final byte[] alreadyRawCollectedPullRequests,
                                                                                                   final Date startDate,
                                                                                                   final Date endDate) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new ArrayList<>();
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>();
        for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestDTOS) {
            if (githubPullRequestDTO.getUpdatedAt().after(startDate)
                    && githubPullRequestDTO.getUpdatedAt().before(endDate)) {
                githubPullRequestDTOList.add(githubPullRequestDTO);
            }
        }
        boolean pullRequestInDateRange = true;
        while (nonNull(githubPullRequestDTOS) && githubPullRequestDTOS.length == properties.getSize() && pullRequestInDateRange) {
            page += 1;
            githubPullRequestDTOS =
                    githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(
                            repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
            if (nonNull(githubPullRequestDTOS)) {
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestDTOS) {
                    if (githubPullRequestDTO.getUpdatedAt().after(startDate)
                            && githubPullRequestDTO.getUpdatedAt().before(endDate)) {
                        githubPullRequestDTOList.add(githubPullRequestDTO);
                    }
                    if (githubPullRequestDTO.getCreatedAt().before(startDate)) {
                        pullRequestInDateRange = false;
                        break;
                    }

                }
            }
        }
        return getIncrementalGithubPullRequests(repository,
                alreadyRawCollectedPullRequests,
                githubPullRequestDTOList);

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
        final List<GithubPullRequestDTO> githubDetailedPullRequests = new ArrayList<>();
        final GithubPullRequestDTO[] alreadyCollectedGithubPullRequestDTOS =
                getGithubPullRequestDTOSFromBytes(alreadyRawCollectedPullRequests);

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

        for (GithubPullRequestDTO alreadyCollectedGithubPullRequestDTO : alreadyCollectedGithubPullRequestDTOS) {
            if (githubDetailedPullRequests.stream()
                    .noneMatch(githubPullRequestDTO -> githubPullRequestDTO.getId()
                            .equals(alreadyCollectedGithubPullRequestDTO.getId()))) {
                githubDetailedPullRequests.add(alreadyCollectedGithubPullRequestDTO);
            }
        }
        return githubDetailedPullRequests;
    }

    private List<GithubPullRequestDTO> getGithubPullRequestDTOs(Repository repository,
                                                                List<GithubPullRequestDTO> githubPullRequestDTOList) {
        final List<GithubPullRequestDTO> githubPullRequestDTOs = new ArrayList<>();
        try {
            new ForkJoinPool(2).submit(() -> {
                githubPullRequestDTOList.parallelStream()
                        .map(githubPullRequestDTO -> {
                            try {
                                return Optional.of(getPullRequestDetailsForPullRequestNumber(repository,
                                        githubPullRequestDTO));
                            } catch (SymeoException ex) {
                                LOGGER.error("Error while getting PR from github", ex);
                            }
                            return Optional.empty();

                        })
                        .filter(Optional::isPresent)
                        .map(o -> (GithubPullRequestDTO) o.get())
                        .forEach(githubPullRequestDTOs::add);
            }).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        return githubPullRequestDTOs;
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
