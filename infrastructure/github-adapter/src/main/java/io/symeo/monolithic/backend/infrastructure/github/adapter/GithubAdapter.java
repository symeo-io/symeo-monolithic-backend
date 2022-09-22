package io.symeo.monolithic.backend.infrastructure.github.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.domain.port.out.VersionControlSystemAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper.mapPullRequestDtoToDomain;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

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
        return dtoToBytes(githubRepositoryDTOList.toArray());

    }

    @Override
    public String getName() {
        return "github";
    }


    @Override
    public List<Repository> repositoriesBytesToDomain(final byte[] repositoriesBytes) throws SymeoException {

        GithubRepositoryDTO[] githubRepositoryDTOS = bytesToDto(repositoriesBytes,
                GithubRepositoryDTO[].class);
        return Arrays.stream(githubRepositoryDTOS)
                .map(githubRepositoryDTO -> GithubMapper.mapRepositoryDtoToDomain(githubRepositoryDTO, getName()))
                .toList();

    }

    @Override
    public byte[] getRawPullRequestsForRepository(final Repository repository,
                                                  final byte[] alreadyRawCollectedPullRequests) throws SymeoException {
        int page = 1;
        GithubPullRequestDTO[] githubPullRequestDTOS =
                this.githubHttpClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                        repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
        if (isNull(githubPullRequestDTOS) || githubPullRequestDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubPullRequestDTO> githubPullRequestDTOList =
                new ArrayList<>(List.of(githubPullRequestDTOS));
        while (githubPullRequestDTOS.length == properties.getSize()) {
            page += 1;
            githubPullRequestDTOS =
                    this.githubHttpClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(
                            repository.getVcsOrganizationName(), repository.getName(), page, properties.getSize());
            githubPullRequestDTOList.addAll(Arrays.stream(githubPullRequestDTOS).toList());
        }

        try {
            List<GithubPullRequestDTO> githubDetailedPullRequests = null;
            if (alreadyRawCollectedPullRequests == null || alreadyRawCollectedPullRequests.length == 0) {
                githubDetailedPullRequests = new ArrayList<>(getGithubPullRequestDTOs(repository,
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

    private List<GithubPullRequestDTO> getGithubPullRequestDTOs(Repository repository,
                                                                List<GithubPullRequestDTO> githubPullRequestDTOList) {
        return githubPullRequestDTOList.stream()
                .map(githubPullRequestDTO -> {
                    try {
                        return Optional.of(getPullRequestDetailsForPullRequestNumber(repository, githubPullRequestDTO));
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
                                                                        List<GithubPullRequestDTO> githubPullRequestDTOList)
            throws IOException, SymeoException {
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

    private GithubPullRequestDTO getPullRequestDetailsForPullRequestNumber(Repository repository,
                                                                           GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        final GithubPullRequestDTO pullRequestDetailsForPullRequestNumber =
                githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), currentGithubPullRequestDTO.getNumber());
        pullRequestDetailsForPullRequestNumber.setGithubCommentsDTOS(getCommentsForPullRequestNumber(repository,
                currentGithubPullRequestDTO));
        pullRequestDetailsForPullRequestNumber.setGithubCommitsDTOS(getCommitsForPullRequestNumber(repository,
                currentGithubPullRequestDTO));
        return pullRequestDetailsForPullRequestNumber;
    }

    private GithubCommentsDTO[] getCommentsForPullRequestNumber(Repository repository,
                                                                GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        // TODO : @Dorian adds pagination
        return githubHttpClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), currentGithubPullRequestDTO.getNumber());
    }

    private GithubCommitsDTO[] getCommitsForPullRequestNumber(Repository repository,
                                                              GithubPullRequestDTO currentGithubPullRequestDTO) throws SymeoException {
        // TODO : @Dorian adds pagination
        return githubHttpClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), currentGithubPullRequestDTO.getNumber());
    }


    @Override
    public List<PullRequest> pullRequestsBytesToDomain(final byte[] bytes) throws SymeoException {
        if (bytes.length == 0) {
            return List.of();
        }
        final GithubPullRequestDTO[] githubPullRequestDTOS = getGithubPullRequestDTOSFromBytes(bytes);
        return Arrays.stream(githubPullRequestDTOS)
                .map(githubPullRequestDTO -> mapPullRequestDtoToDomain(githubPullRequestDTO, this.getName()))
                .toList();
    }

    private GithubPullRequestDTO[] getGithubPullRequestDTOSFromBytes(byte[] bytes) throws SymeoException {
        return bytesToDto(bytes,
                GithubPullRequestDTO[].class);
    }


    public <T> byte[] dtoToBytes(T t) throws SymeoException {
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

    public <T> T bytesToDto(byte[] bytes, Class<T> tClass) throws SymeoException {
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


    @Override
    public byte[] getRawCommitsForPullRequestNumber(final String vcsOrganizationName,
                                                    final String repositoryName,
                                                    final int pullRequestNumber) throws SymeoException {
        final GithubCommitsDTO[] githubCommitsDTO = githubHttpClient.getCommitsForPullRequestNumber(vcsOrganizationName,
                repositoryName, pullRequestNumber);
        return dtoToBytes(githubCommitsDTO);
    }

    @Override
    public List<Commit> commitsBytesToDomain(final byte[] rawCommits) throws SymeoException {
        if (rawCommits.length == 0) {
            return List.of();
        }
        return Arrays.stream(bytesToDto(rawCommits, GithubCommitsDTO[].class))
                .map(GithubMapper::mapCommitToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Comment> commentsBytesToDomain(final byte[] rawComments) throws SymeoException {
        if (rawComments.length == 0) {
            return List.of();
        }
        return Arrays.stream(bytesToDto(rawComments, GithubCommentsDTO[].class))
                .map(
                        (GithubCommentsDTO githubCommentsDTO) ->
                                GithubMapper.mapCommentToDomain(githubCommentsDTO, this.getName())
                )
                .collect(Collectors.toList());
    }

    @Override
    public byte[] getRawComments(final String vcsOrganizationName, final String repositoryName,
                                 final Integer pullRequestNumber) throws SymeoException {
        final GithubCommentsDTO[] githubCommentsDTOS =
                githubHttpClient.getCommentsForPullRequestNumber(vcsOrganizationName,
                        repositoryName, pullRequestNumber);
        return dtoToBytes(githubCommentsDTOS);
    }

    @Override
    public byte[] getRawCommitsForRepository(final String vcsOrganizationName, final String repositoryName,
                                             final byte[] alreadyCollectedCommits) throws SymeoException {
        int page = 1;
        GithubCommitsDTO[] githubCommitsDTOS =
                githubHttpClient.getCommitsForRepositoryAndOrganization(vcsOrganizationName, repositoryName, page,
                        properties.getSize());
        if (isNull(githubCommitsDTOS) || githubCommitsDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubCommitsDTO> githubCommitsDTOList =
                new ArrayList<>(List.of(githubCommitsDTOS));
        while (githubCommitsDTOS.length == properties.getSize()) {
            page += 1;
            githubCommitsDTOS =
                    this.githubHttpClient.getCommitsForRepositoryAndOrganization(
                            vcsOrganizationName, repositoryName, page, properties.getSize());
            githubCommitsDTOList.addAll(Arrays.stream(githubCommitsDTOS).toList());
        }

        return dtoToBytes(githubCommitsDTOList.toArray());
    }

    @Override
    public byte[] getRawBranches(String vcsOrganizationName, String repositoryName) throws SymeoException {
        final GithubBranchDTO[] branchesForOrganizationAndRepository =
                githubHttpClient.getBranchesForOrganizationAndRepository(vcsOrganizationName, repositoryName);
        return dtoToBytes(branchesForOrganizationAndRepository);
    }

    @Override
    public List<Branch> branchesBytesToDomain(byte[] rawBranches) throws SymeoException {
        if (rawBranches.length == 0) {
            return List.of();
        }
        return Arrays.stream(bytesToDto(rawBranches, GithubBranchDTO[].class))
                .map(GithubMapper::mapBranchToDomain)
                .collect(Collectors.toList());
    }

    @Override
    public byte[] getRawCommitsForBranchFromLastCollectionDate(String vcsOrganizationName, String repositoryName,
                                                               String branchName, Date lastCollectionDate,
                                                               byte[] alreadyCollectedRawGithubCommitsDTOS)
            throws SymeoException {
        lastCollectionDate = getLastCollectionDateFromAlreadyCollectCommits(lastCollectionDate,
                alreadyCollectedRawGithubCommitsDTOS);
        int page = 1;
        GithubCommitsDTO[] githubCommitsDTOS = getGithubCommitsDTOSFromLastCollectionDate(lastCollectionDate,
                githubHttpClient, vcsOrganizationName, repositoryName, branchName, page, properties);
        if (isNull(githubCommitsDTOS) || githubCommitsDTOS.length == 0) {
            return new byte[0];
        }
        final List<GithubCommitsDTO> githubCommitsDTOList =
                new ArrayList<>(List.of(githubCommitsDTOS));
        while (githubCommitsDTOS.length == properties.getSize()) {
            page += 1;
            githubCommitsDTOS = getGithubCommitsDTOSFromLastCollectionDate(lastCollectionDate, githubHttpClient,
                    vcsOrganizationName, repositoryName, branchName, page, properties);
            githubCommitsDTOList.addAll(Arrays.stream(githubCommitsDTOS).toList());
        }

        if (nonNull(alreadyCollectedRawGithubCommitsDTOS) && alreadyCollectedRawGithubCommitsDTOS.length > 0) {
            final GithubCommitsDTO[] alreadyCollectedGithubCommitsDTOS =
                    bytesToDto(alreadyCollectedRawGithubCommitsDTOS,
                            GithubCommitsDTO[].class);
            githubCommitsDTOList.addAll(
                    compareAlreadyCollectedCommitsToCurrentCommitsAndReturnNotUpdatedCommits(
                            Arrays.asList(alreadyCollectedGithubCommitsDTOS), githubCommitsDTOList)
            );
        }
        return dtoToBytes(githubCommitsDTOList.toArray());

    }

    private GithubCommitsDTO[] getGithubCommitsDTOSFromLastCollectionDate(Date lastCollectionDate,
                                                                          GithubHttpClient githubHttpClient,
                                                                          String vcsOrganizationName,
                                                                          String repositoryName, String branchName,
                                                                          int page, GithubProperties properties) throws SymeoException {
        return isNull(lastCollectionDate) ?
                githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranch(vcsOrganizationName,
                        repositoryName, branchName, page, properties.getSize())
                :
                githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, branchName, lastCollectionDate, page, properties.getSize());
    }

    private Date getLastCollectionDateFromAlreadyCollectCommits(Date lastCollectionDate,
                                                                byte[] alreadyCollectedRawGithubCommitsDTOS) throws SymeoException {
        if (isNull(lastCollectionDate) && (nonNull(alreadyCollectedRawGithubCommitsDTOS)
                && alreadyCollectedRawGithubCommitsDTOS.length > 0)) {
            Date lastCollectionDateFromAlreadyCollectedCommits = null;
            for (GithubCommitsDTO githubCommitsDTO : bytesToDto(alreadyCollectedRawGithubCommitsDTOS,
                    GithubCommitsDTO[].class)) {
                final Date commitDate = githubCommitsDTO.getCommit().getCommitter().getDate();
                if (isNull(lastCollectionDateFromAlreadyCollectedCommits)) {
                    lastCollectionDateFromAlreadyCollectedCommits = commitDate;
                } else {
                    if (commitDate.after(lastCollectionDateFromAlreadyCollectedCommits)) {
                        lastCollectionDateFromAlreadyCollectedCommits = commitDate;
                    }
                }
            }
            lastCollectionDate = lastCollectionDateFromAlreadyCollectedCommits;
        }
        return lastCollectionDate;
    }

    private List<GithubCommitsDTO> compareAlreadyCollectedCommitsToCurrentCommitsAndReturnNotUpdatedCommits(
            final List<GithubCommitsDTO> alreadyCollectedGithubCommitsDTOS,
            final List<GithubCommitsDTO> githubCommitsDTOList) {
        return alreadyCollectedGithubCommitsDTOS.stream()
                .filter(githubCommitsDTO -> githubCommitsDTOList.stream()
                        .noneMatch(collectedCommit -> collectedCommit.getSha().equals(githubCommitsDTO.getSha()))
                )
                .collect(Collectors.toList());
    }

}
