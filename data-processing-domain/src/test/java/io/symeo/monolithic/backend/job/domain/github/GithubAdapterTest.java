package io.symeo.monolithic.backend.job.domain.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper.mapRepositoryDtoToDomain;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GithubAdapterTest extends AbstractGithubAdapterTest {

    @Nested
    public class RepositoryCollection {
        @Test
        void should_get_repositories_given_an_organization_name() throws IOException, SymeoException {
            // Given
            final VcsOrganization vcsOrganization = VcsOrganization.builder()
                    .name(faker.rickAndMorty().character())
                    .vcsId(faker.idNumber().invalid())
                    .id(faker.number().randomNumber())
                    .externalId(faker.pokemon().name())
                    .organizationId(UUID.randomUUID())
                    .build();
            final GithubProperties githubProperties = new GithubProperties();
            githubProperties.setSize(3);
            githubProperties.setGithubAppId(faker.name().name());
            githubProperties.setPrivateKeyCertificatePath(faker.animal().name());
            final GithubApiClientAdapter githubHttpApiClient = Mockito.mock(GithubApiClientAdapter.class);
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter,
                    githubProperties, objectMapper);

            final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("github/get_repositories_for_org",
                    "get_repo_for_org_page_1_size_3" +
                            ".json", GithubRepositoryDTO[].class);
            final GithubRepositoryDTO[] githubRepositoryStubs2 = getStubsFromClassT("github/get_repositories_for_org",
                    "get_repo_for_org_page_2_size_3" +
                            ".json", GithubRepositoryDTO[].class);
            final GithubRepositoryDTO[] githubRepositoryStubs3 = getStubsFromClassT("github/get_repositories_for_org",
                    "get_repo_for_org_page_3_size_3" +
                            ".json", GithubRepositoryDTO[].class);
            when(githubHttpApiClient.getRepositoriesForVcsOrganizationName(vcsOrganization.getName(), 1, 3)).thenReturn(githubRepositoryStubs1);
            when(githubHttpApiClient.getRepositoriesForVcsOrganizationName(vcsOrganization.getName(), 2, 3)).thenReturn(githubRepositoryStubs2);
            when(githubHttpApiClient.getRepositoriesForVcsOrganizationName(vcsOrganization.getName(), 3, 3)).thenReturn(githubRepositoryStubs3);


            // When
            final List<Repository> repositoriesForVcsOrganization =
                    githubAdapter.getRepositoriesForVcsOrganization(vcsOrganization);

            // Then
            assertThat(repositoriesForVcsOrganization).isNotEmpty();
            assertThat(repositoriesForVcsOrganization).hasSize(githubRepositoryStubs1.length + githubRepositoryStubs2.length +
                    githubRepositoryStubs3.length);
            final ArrayList<GithubRepositoryDTO> githubRepositoryDTOArrayList = new ArrayList<>();
            githubRepositoryDTOArrayList.addAll(Arrays.stream(githubRepositoryStubs1).toList());
            githubRepositoryDTOArrayList.addAll(Arrays.stream(githubRepositoryStubs2).toList());
            githubRepositoryDTOArrayList.addAll(Arrays.stream(githubRepositoryStubs3).toList());
            verify(rawStorageAdapter, times(1))
                    .save(vcsOrganization.getOrganizationId(), "github", Repository.ALL,
                            dtoStubsToBytes(githubRepositoryDTOArrayList.toArray()));
        }

        @Test
        void should_map_repositories_bytes_to_domain_repositories() throws IOException, SymeoException {
            // Given
            final GithubRepositoryDTO[] githubRepositoryStubs1 = getStubsFromClassT("github/get_repositories_for_org",
                    "get_repo_for_org_page_1_size_3" +
                            ".json", GithubRepositoryDTO[].class);
            final VcsOrganization vcsOrganization = VcsOrganization.builder()
                    .name(faker.rickAndMorty().character())
                    .vcsId(faker.idNumber().invalid())
                    .id(faker.number().randomNumber())
                    .externalId(faker.pokemon().name())
                    .organizationId(UUID.randomUUID())
                    .build();

            // When
            final List<Repository> repositories =
                    Arrays.stream(githubRepositoryStubs1)
                            .map((GithubRepositoryDTO githubRepositoryDTO) -> mapRepositoryDtoToDomain(githubRepositoryDTO, "github", vcsOrganization))
                            .toList();

            // Then
            assertThat(repositories).hasSize(3);
            assertThat(repositories.get(0).getName()).isEqualTo(githubRepositoryStubs1[0].getName());
            assertThat(repositories.get(0).getVcsOrganizationId()).isEqualTo(vcsOrganization.getVcsId());
            assertThat(repositories.get(0).getVcsOrganizationName()).isEqualTo(vcsOrganization.getName());
            assertThat(repositories.get(0).getOrganizationId()).isEqualTo(vcsOrganization.getOrganizationId());
            assertThat(repositories.get(0).getDefaultBranch()).isEqualTo(githubRepositoryStubs1[0].getAdditionalProperties().get("default_branch"));
            assertThat(repositories.get(0).getId()).isEqualTo("github-" + githubRepositoryStubs1[0].getId());
        }

    }

    @Nested
    public class PullRequestCollection {


        @Test
        void should_map_github_pr_dto_to_pull_request_domain() throws IOException {
            // Given
            final Repository repository = Repository.builder()
                    .id(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .vcsOrganizationId(faker.pokemon().name())
                    .name(faker.name().firstName())
                    .build();
            final GithubPullRequestDTO pr80 = getStubsFromClassT(
                    "github/get_pull_request_details_for_pr_id", "get_pr_80.json",
                    GithubPullRequestDTO.class);
            final String githubPlatformName = "github";

            // When
            final PullRequest pullRequest = GithubMapper.mapPullRequestDtoToDomain(pr80, githubPlatformName,
                    repository);

            // Then
            assertThat(pullRequest.getId()).isEqualTo(githubPlatformName + "-" + pr80.getId());
            assertThat(pullRequest.getCommitNumber()).isEqualTo(pr80.getCommits());
            assertThat(pullRequest.getDeletedLineNumber()).isEqualTo(pr80.getDeletions());
            assertThat(pullRequest.getAddedLineNumber()).isEqualTo(pr80.getAdditions());
            assertThat(pullRequest.getCreationDate()).isEqualTo(pr80.getCreatedAt());
            assertThat(pullRequest.getLastUpdateDate()).isEqualTo(pr80.getUpdatedAt());
            assertThat(pullRequest.getMergeDate()).isEqualTo(pr80.getMergedAt());
            assertThat(pullRequest.getIsMerged()).isEqualTo(pr80.getMerged());
            assertThat(pullRequest.getIsDraft()).isEqualTo(pr80.getDraft());
            assertThat(pullRequest.getStatus()).isEqualTo(pr80.getState());
            assertThat(pullRequest.getNumber()).isEqualTo(pr80.getNumber());
            assertThat(pullRequest.getVcsUrl()).isEqualTo(pr80.getHtmlUrl());
            assertThat(pullRequest.getTitle()).isEqualTo(pr80.getTitle());
            assertThat(pullRequest.getAuthorLogin()).isEqualTo(pr80.getUser().getLogin());
            assertThat(pullRequest.getHead()).isEqualTo(pr80.getHead().getRef());
            assertThat(pullRequest.getBase()).isEqualTo(pr80.getBase().getRef());
            assertThat(pullRequest.getVcsOrganizationId()).isEqualTo(repository.getVcsOrganizationId());
        }


        @Nested
        public class EmptyRawStorage {

            @Test
            void should_get_pull_request_with_size_exactly_the_same_than_pagination_size() throws SymeoException,
                    IOException {
                // Given
                final GithubProperties properties = new GithubProperties();
                properties.setSize(1);
                final GithubApiClientAdapter githubHttpApiClient = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter,
                        properties, objectMapper);
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
                final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id",
                        "get_pr_76.json", GithubPullRequestDTO.class);
                final GithubCommitsDTO[] githubCommitsStubs1 = getStubsFromClassT("github/get_commits_for_pr_number",
                        "get_commits_for_pr_number_2_page_1_size_30.json",
                        GithubCommitsDTO[].class);
                final GithubCommentsDTO[] githubCommentsStubs1 = getStubsFromClassT("github/get_comments_for_pr_number",
                        "get_comments_for_pr_number_2_page_1_size_3.json",
                        GithubCommentsDTO[].class);

                // When
                when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(false);
                when(githubHttpApiClient.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                        .thenReturn(githubPullRequestStubs1);
                when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 80))
                        .thenReturn(githubPullRequestDetails);
                when(githubHttpApiClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 80, 1, properties.getSize()))
                        .thenReturn(githubCommitsStubs1);
                when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 80, 1, properties.getSize()))
                        .thenReturn(githubCommentsStubs1);

                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository
                                , stringToDate("1999-01-01"), stringToDate("2999-01-01"));

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange.size()).isEqualTo(githubPullRequestStubs1.length);
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }

            @Test
            void should_get_pull_requests_given_all_pr_in_date_range() throws IOException,
                    SymeoException {
                // Given
                final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubProperties properties = new GithubProperties();
                properties.setSize(3);
                final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                        rawStorageAdapter, properties, new ObjectMapper());
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final Date startDate = stringToDate("2022-01-01");
                final Date endDate = stringToDate("2022-03-01");
                final String updateDate = "2022-02-01";

                // When
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs1, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 3))
                        .thenReturn(githubPullRequestStubs1);
                final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 2, 3))
                        .thenReturn(githubPullRequestStubs2);
                final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs3, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 3, 3))
                        .thenReturn(githubPullRequestStubs3);
                when(githubApiClientAdapter.getCommentsForPullRequestNumber(anyString(), anyString(), anyInt(),
                        anyInt(), anyInt()))
                        .thenReturn(new GithubCommentsDTO[]{});
                when(githubApiClientAdapter.getCommitsForPullRequestNumber(anyString(), anyString(), anyInt(),
                        anyInt(), anyInt()))
                        .thenReturn(new GithubCommitsDTO[]{});
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs2) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs3) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }


                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                startDate, endDate);

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange)
                        .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length + githubPullRequestStubs3.length);
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }

            @Test
            void should_get_pull_requests_given_some_pr_not_in_date_range() throws IOException,
                    SymeoException {
                // Given
                final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubProperties properties = new GithubProperties();
                properties.setSize(3);
                final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                        rawStorageAdapter, properties, new ObjectMapper());
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final Date startDate = stringToDate("2022-01-01");
                final Date endDate = stringToDate("2022-03-01");
                final String updateDate = "2022-02-01";

                // When
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs1, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 3))
                        .thenReturn(githubPullRequestStubs1);
                final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 2, 3))
                        .thenReturn(githubPullRequestStubs2);
                final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs3, "2022-03-02");
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 3, 3))
                        .thenReturn(githubPullRequestStubs3);
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs2) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs3) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                startDate, endDate);

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange)
                        .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length);
                verify(githubApiClientAdapter, times(3)).getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(any(),
                        any(), any(), any());
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }

        }

        @Nested
        public class NotEmptyRawStorage {


            @Test
            void should_get_incremental_pull_requests_given_a_repository_and_already_collected_pull_requests() throws IOException, SymeoException {
                // Given
                final GithubProperties githubProperties = new GithubProperties();
                githubProperties.setSize(10);
                final GithubApiClientAdapter githubHttpApiClient = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter,
                        githubProperties, objectMapper);
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github" +
                                "/get_pull_requests_for_repo",
                        "incremental_get_pr_for_repo_page_1_size_10.json", GithubPullRequestDTO[].class);
                final GithubPullRequestDTO pr74 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_74.json",
                        GithubPullRequestDTO.class);
                final GithubPullRequestDTO pr75 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_75.json",
                        GithubPullRequestDTO.class);
                final GithubPullRequestDTO pr76 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_76.json",
                        GithubPullRequestDTO.class);
                final GithubPullRequestDTO pr77 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_77.json",
                        GithubPullRequestDTO.class);
                final GithubPullRequestDTO pr78 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_78.json",
                        GithubPullRequestDTO.class);
                final GithubPullRequestDTO pr79 = getStubsFromClassT(
                        "github/get_pull_request_details_for_pr_id", "get_pr_79.json",
                        GithubPullRequestDTO.class);
                final byte[] rawPullRequestsAlreadyCollected =
                        objectMapper.writeValueAsBytes(new GithubPullRequestDTO[]{pr76
                                , pr77, pr78});


                // When
                when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(true);
                when(rawStorageAdapter.read(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(rawPullRequestsAlreadyCollected);
                // All PRs by repo
                when(githubHttpApiClient.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 10))
                        .thenReturn(githubPullRequestStubs1);
                // PR details by PR
                when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 74))
                        .thenReturn(pr74);
                when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 75))
                        .thenReturn(pr75);
                when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 76))
                        .thenReturn(pr76);
                when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                        repository.getName(), 79))
                        .thenReturn(pr79);
                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                stringToDate("1999-01-01"), stringToDate("2999-01-01"));

                // Then
                verify(githubHttpApiClient, times(1)).getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(any(),
                        any(),
                        any(), any());
                verify(githubHttpApiClient, times(4)).getPullRequestDetailsForPullRequestNumber(anyString(), any(),
                        any());
                final List<PullRequest> expectedResults =
                        Stream.of(pr74, pr75, pr76, pr77, pr78, pr79).map(pr -> GithubMapper.mapPullRequestDtoToDomain(pr,
                                githubAdapter.getName(), repository)).toList();
                assertThat(pullRequestsForRepositoryAndDateRange).containsAll(expectedResults);
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }


            @Test
            void should_get_pull_requests_given_already_collected_pull_requests_before_date_range_and_no_pull_requests_in_date_range() throws SymeoException
                    , IOException {
                // Given
                final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubProperties properties = new GithubProperties();
                properties.setSize(3);
                final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                        rawStorageAdapter, properties, new ObjectMapper());
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final Date startDate = stringToDate("2022-01-01");
                final Date endDate = stringToDate("2022-03-01");
                final String updateDate = "2022-02-01";

                // When
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs1, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 3))
                        .thenReturn(githubPullRequestStubs1);
                final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 2, 3))
                        .thenReturn(githubPullRequestStubs2);
                final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs3, updateDate);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 3, 3))
                        .thenReturn(githubPullRequestStubs3);
                final GithubPullRequestDTO[] rawStorageGithubPullRequestStubs = getStubsFromClassT("github" +
                                "/not_empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_4_size_3.json", GithubPullRequestDTO[].class);
                when(rawStorageAdapter.exists(repository.getOrganizationId(), githubAdapter.getName(),
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(true);
                when(rawStorageAdapter.read(repository.getOrganizationId(), githubAdapter.getName(),
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(dtoStubsToBytes(rawStorageGithubPullRequestStubs));
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs2) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs3) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(
                            repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber()
                    ))
                            .thenReturn(githubPullRequestDTO);
                }
                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                startDate, endDate);

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange)
                        .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length + githubPullRequestStubs3.length + rawStorageGithubPullRequestStubs.length);
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }


            @Test
            void should_collect_pull_requests__for_second_collection_tasks_of_first_job() throws SymeoException,
                    IOException {
                // Given
                final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubProperties properties = new GithubProperties();
                properties.setSize(3);
                final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                        rawStorageAdapter, properties, new ObjectMapper());
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final Date startDate = stringToDate("2022-03-01");
                final Date endDate = stringToDate("2022-05-01");
                final String inRange = "2022-04-01";
                final String beforeRange = "2022-02-01";

                // When
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs1, inRange);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 3))
                        .thenReturn(githubPullRequestStubs1);
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                            repository.getName(), githubPullRequestDTO.getNumber()))
                            .thenReturn(githubPullRequestDTO);
                }
                final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs2, inRange);
                githubPullRequestStubs2[2].setUpdatedAt(stringToDate(beforeRange));
                githubPullRequestStubs2[2].setCreatedAt(stringToDate(beforeRange));
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 2, 3))
                        .thenReturn(githubPullRequestStubs2);
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs2) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                            repository.getName(), githubPullRequestDTO.getNumber()))
                            .thenReturn(githubPullRequestDTO);
                }
                when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(true);
                when(rawStorageAdapter.read(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(dtoStubsToBytes(new GithubPullRequestDTO[]{githubPullRequestStubs2[2]}));
                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                startDate, endDate);

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange).hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length - 1);
                verify(githubApiClientAdapter, times(2))
                        .getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(anyString(), anyString(),
                                anyInt(), anyInt());
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());

            }

            @Test
            void should_collect_pull_requests_for_first_collection_tasks_of_second_job() throws SymeoException,
                    IOException {
                // Given
                final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
                final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
                final GithubProperties properties = new GithubProperties();
                properties.setSize(3);
                final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                        rawStorageAdapter, properties, new ObjectMapper());
                final Repository repository = Repository.builder()
                        .id(faker.idNumber().valid())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationName(faker.rickAndMorty().character())
                        .vcsOrganizationId(faker.pokemon().name())
                        .name(faker.name().firstName())
                        .build();
                final Date startDate = stringToDate("2022-03-01");
                final Date endDate = stringToDate("2022-05-01");
                final String inRange = "2022-04-01";
                final String beforeRange = "2022-02-01";

                // When
                final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_1_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs1, inRange);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 1, 3))
                        .thenReturn(githubPullRequestStubs1);
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                            repository.getName(), githubPullRequestDTO.getNumber()))
                            .thenReturn(githubPullRequestDTO);
                }
                final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
                setGithubPullRequestDates(githubPullRequestStubs2, beforeRange);
                when(githubApiClientAdapter.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                        repository.getName(), 2, 3))
                        .thenReturn(githubPullRequestStubs2);
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs2) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                            repository.getName(), githubPullRequestDTO.getNumber()))
                            .thenReturn(githubPullRequestDTO);
                }
                final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                                "/get_pull_requests_for_repo",
                        "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
                final List<GithubPullRequestDTO> githubPullRequestDTOS =
                        Arrays.stream(githubPullRequestStubs3).collect(Collectors.toList());
                githubPullRequestDTOS.addAll(Arrays.stream(githubPullRequestStubs2).toList());
                when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(true);
                when(rawStorageAdapter.read(repository.getOrganizationId(), "github",
                        PullRequest.getNameFromRepositoryId(repository.getId())))
                        .thenReturn(dtoStubsToBytes(githubPullRequestDTOS.toArray()));
                for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs3) {
                    when(githubApiClientAdapter.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                            repository.getName(), githubPullRequestDTO.getNumber()))
                            .thenReturn(githubPullRequestDTO);
                }
                final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                        githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                                startDate, endDate);

                // Then
                assertThat(pullRequestsForRepositoryAndDateRange).hasSize(
                        githubPullRequestStubs1.length
                );
                verify(rawStorageAdapter, times(1)).save(any(), anyString(), anyString(), any());
            }


        }

        private static void setGithubPullRequestDates(GithubPullRequestDTO[] githubPullRequestStubs1,
                                                      String date) throws SymeoException {
            for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
                githubPullRequestDTO.setUpdatedAt(stringToDate(date));
                githubPullRequestDTO.setCreatedAt(stringToDate(date));
            }
        }
    }


    @Nested
    public class PullRequestCommentCollection {
        @Test
        void should_map_github_comments_dto_to_domain() throws IOException, ParseException {
            // Given
            final GithubCommentsDTO[] githubCommentsDTO = getStubsFromClassT("github/get_comments_for_pr_number",
                    "get_comments_for_pr_number_1.json",
                    GithubCommentsDTO[].class);
            final GithubCommentsDTO githubCommentsDTO1 = githubCommentsDTO[0];
            final String githubPlatformName = faker.rickAndMorty().character();

            // When
            final io.symeo.monolithic.backend.job.domain.model.vcs.Comment comment =
                    GithubMapper.mapCommentToDomain(githubCommentsDTO1, githubPlatformName);

            // Then
            assertThat(comment).isNotNull();
            assertThat(comment.getId()).isEqualTo(githubPlatformName + "-" + githubCommentsDTO1.getId());
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            assertThat(comment.getCreationDate()).isEqualTo(simpleDateFormat.parse("2022-08-25 " +
                    "07:51:33"));
        }

        @Test
        void should_collect_comments_given_a_pull_request_number() throws SymeoException, IOException {
            // Given
            final GithubApiClientAdapter githubHttpApiClient = mock(GithubApiClientAdapter.class);
            final GithubProperties properties = new GithubProperties();
            properties.setSize(3);
            final GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
            final Integer currentPullRequestNumber = faker.number().randomDigit();
            githubPullRequestDTO.setNumber(currentPullRequestNumber);
            final Repository repository = Repository.builder()
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .name(faker.ancient().god())
                    .vcsOrganizationId(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .id(faker.pokemon().name())
                    .build();
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter,
                    properties, new ObjectMapper());
            final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
            final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT(
                    "github/get_pull_request_details_for_pr_id",
                    "get_pr_76.json", GithubPullRequestDTO.class);
            final GithubCommentsDTO[] githubCommentsStubs1 = getStubsFromClassT("github/get_comments_for_pr_number",
                    "get_comments_for_pr_number_2_page_1_size_3.json",
                    GithubCommentsDTO[].class);
            final GithubCommentsDTO[] githubCommentsStubs2 = getStubsFromClassT("github/get_comments_for_pr_number",
                    "get_comments_for_pr_number_2_page_2_size_2.json",
                    GithubCommentsDTO[].class);

            // When
            when(githubHttpApiClient.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                    .thenReturn(githubPullRequestStubs1);
            when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80))
                    .thenReturn(githubPullRequestDetails);
            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(githubCommentsStubs1);

            final List<PullRequest> pullRequestsForRepositoryAndDateRange1 =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository
                            , stringToDate("1999-01-01"), stringToDate("2999-01-01"));
            final List<Comment> comments1 = pullRequestsForRepositoryAndDateRange1.get(0).getComments();

            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 2, properties.getSize()))
                    .thenReturn(githubCommentsStubs2);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange2 =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                            stringToDate("1999-01-01"),
                            stringToDate("2999-01-01"));
            final List<Comment> comments2 =
                    pullRequestsForRepositoryAndDateRange2.get(0).getComments();

            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(null);
            final List<PullRequest> nullCommentsPullRequestsForRepository =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository,
                            stringToDate("1999-01-01"), stringToDate("2999-01-01"));
            final List<Comment> nullCommentsResult =
                    nullCommentsPullRequestsForRepository.get(0).getComments();


            // Then
            assertThat(comments1.size()).isEqualTo(githubCommentsStubs1.length);
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
                assertThat(comments1).anyMatch(comment -> comment.getId()
                        .equals("github-" + githubCommentsDTO.getId())
                        && comment.getCreationDate().equals(githubCommentsDTO.getCreationDate())
                );
            }

            assertThat(comments2.size()).isEqualTo(githubCommentsStubs1.length + githubCommentsStubs2.length);
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
                assertThat(comments2).anyMatch(comment -> comment.getCreationDate().equals(githubCommentsDTO.getCreationDate())
                        && comment.getId().equals("github-" + githubCommentsDTO.getId()));
            }
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs2) {
                assertThat(comments2).anyMatch(comment -> comment.getId().equals("github-" + githubCommentsDTO.getId()) &&
                        comment.getCreationDate().equals(githubCommentsDTO.getCreationDate()));
            }

            assertThat(nullCommentsResult.size()).isEqualTo(0);
        }
    }

    @Nested
    public class PullRequestCommitCollection {
        @Test
        void should_map_github_commits_dto_to_domain() throws IOException, ParseException {
            // Given
            final Repository repository = Repository.builder()
                    .id(faker.idNumber().valid())
                    .vcsOrganizationName(faker.rickAndMorty().location())
                    .vcsOrganizationId(faker.rickAndMorty().character())
                    .organizationId(UUID.randomUUID())
                    .name(faker.pokemon().name())
                    .build();
            final GithubCommitsDTO[] githubCommitsDTO = getStubsFromClassT("github/get_commits_for_pr_number",
                    "get_commits_for_pr_number_1.json",
                    GithubCommitsDTO[].class);

            // When
            final Commit commit = GithubMapper.mapCommitToDomain(githubCommitsDTO[0], repository);

            // Then
            assertThat(commit).isNotNull();
            assertThat(commit.getSha()).isEqualTo("e3d2d7b72e93c3b63cd597eb3bec630fad8e000c");
            assertThat(commit.getAuthor()).isEqualTo("pierre.oucif");
            assertThat(commit.getMessage()).isEqualTo("Init for Biox");
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
            assertThat(commit.getDate()).isEqualTo(simpleDateFormat.parse("2022-08-24 " +
                    "11:05:15"));
            assertThat(commit.getRepositoryId()).isEqualTo(repository.getId());
        }

        @Test
        void should_collect_commits_given_a_pull_request_number() throws SymeoException, IOException {
            // Given
            final GithubApiClientAdapter githubHttpApiClient = mock(GithubApiClientAdapter.class);
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubProperties properties = new GithubProperties();
            properties.setSize(30);
            final GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
            final Integer currentPullRequestNumber = faker.number().randomDigit();
            githubPullRequestDTO.setNumber(currentPullRequestNumber);
            final Repository repository = Repository.builder()
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .name(faker.ancient().god())
                    .id(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .vcsOrganizationId(faker.rickAndMorty().character())
                    .vcsOrganizationName(faker.rickAndMorty().location())
                    .build();
            final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter, properties,
                    new ObjectMapper());
            final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("github" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
            final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT(
                    "github/get_pull_request_details_for_pr_id",
                    "get_pr_76.json", GithubPullRequestDTO.class);
            final GithubCommitsDTO[] githubCommitsStubs1 = getStubsFromClassT("github/get_commits_for_pr_number",
                    "get_commits_for_pr_number_2_page_1_size_30.json",
                    GithubCommitsDTO[].class);
            final GithubCommitsDTO[] githubCommitsStubs2 = getStubsFromClassT("github/get_commits_for_pr_number",
                    "get_commits_for_pr_number_2_page_2_size_28.json",
                    GithubCommitsDTO[].class);
            final Date startDate = stringToDate("1999-01-01");
            final Date endDate = stringToDate("2999-01-01");

            // When
            when(githubHttpApiClient.getPullRequestsForRepositoryAndVcsOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                    .thenReturn(githubPullRequestStubs1);
            when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80))
                    .thenReturn(githubPullRequestDetails);
            when(githubHttpApiClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(githubCommitsStubs1);

            final List<PullRequest> pullRequestsForRepositoryAndDateRange1 =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate,
                            endDate);
            final List<Commit> commits1 = pullRequestsForRepositoryAndDateRange1.get(0).getCommits();

            when(githubHttpApiClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 2, properties.getSize()))
                    .thenReturn(githubCommitsStubs2);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange2 =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate,
                            endDate);
            final List<Commit> commits2 = pullRequestsForRepositoryAndDateRange2.get(0).getCommits();

            when(githubHttpApiClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(null);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange3 =
                    githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate,
                            endDate);
            final List<Commit> commits3 = pullRequestsForRepositoryAndDateRange3.get(0).getCommits();


            // Then
            assertThat(commits1.size()).isEqualTo(githubCommitsStubs1.length);
            for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs1) {
                assertThat(commits1).anyMatch(commit -> commitIsEqualsTo(repository.getId(), commit, githubCommitsDTO));
            }

            assertThat(commits2.size()).isEqualTo(githubCommitsStubs1.length + githubCommitsStubs2.length);
            for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs1) {
                assertThat(commits2).anyMatch(commit -> commitIsEqualsTo(repository.getId(), commit, githubCommitsDTO));
            }
            for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs2) {
                assertThat(commits2).anyMatch(commit -> commitIsEqualsTo(repository.getId(), commit, githubCommitsDTO));
            }

            assertThat(commits3.size()).isEqualTo(0);
        }

        private boolean commitIsEqualsTo(final String repositoryId, final Commit commit,
                                         final GithubCommitsDTO githubCommitsDTO) {
            return commit.getSha().equals(githubCommitsDTO.getSha()) &&
                    commit.getAuthor().equals(githubCommitsDTO.getCommit().getCommitter().getName()) &&
                    commit.getMessage().equals(githubCommitsDTO.getCommit().getMessage()) &&
                    commit.getDate().equals(githubCommitsDTO.getCommit().getCommitter().getDate()) &&
                    commit.getParentShaList().equals(githubCommitsDTO.getParents()
                            .stream().map(GithubCommitsDTO.GithubCommitParentDTO::getSha).toList()) &&
                    commit.getRepositoryId().equals(repositoryId);

        }

    }

    @Nested
    public class TagCollection {


        @Test
        void should_collect_tag_given_an_organizationName_and_repository() throws IOException, SymeoException {
            // Given
            final GithubApiClientAdapter githubHttpApiClient = mock(GithubApiClientAdapter.class);
            final String urlHost = faker.gameOfThrones().character();
            final GithubProperties properties = new GithubProperties();
            properties.setSize(3);
            properties.setUrlHost(urlHost);
            final Repository repository = Repository.builder()
                    .vcsOrganizationName(faker.rickAndMorty().location())
                    .name(faker.ancient().god())
                    .organizationId(UUID.randomUUID())
                    .id(faker.rickAndMorty().character())
                    .vcsOrganizationId(faker.pokemon().name())
                    .build();
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, rawStorageAdapter,
                    properties, new ObjectMapper());
            final GithubTagDTO[] githubTagStub = getStubsFromClassT("github/get_tags_for_organization_and_repository",
                    "get_repo_1_organization_1_tags.json", GithubTagDTO[].class);

            // When
            when(githubHttpApiClient.getTagsForVcsOrganizationAndRepository(repository.getVcsOrganizationName(),
                    repository.getName()))
                    .thenReturn(githubTagStub);
            final List<Tag> tags = githubAdapter.getTags(repository);

            // Then
            assertThat(tags.size()).isEqualTo(githubTagStub.length);
            for (GithubTagDTO githubTagDTO : githubTagStub) {
                assertThat(tags).anyMatch(tag -> tag.getName().equals(githubTagDTO.getRef().replace("refs/tags/", "")) &&
                        tag.getRepositoryId().equals(repository.getId()) &&
                        tag.getVcsUrl().equals(urlHost
                                + repository.getVcsOrganizationName()
                                + "/" + repository.getName()
                                + "/tree/" + githubTagDTO.getRef().replace("refs/tags/", "")) &&
                        tag.getCommitSha().equals(githubTagDTO.getObject().getSha())
                );
            }
            verify(rawStorageAdapter, times(1)).save(repository.getOrganizationId(), "github",
                    Tag.getNameFromRepository(repository), dtoStubsToBytes(githubTagStub));
        }

        @Test
        void should_map_github_tags_dto_to_domain() throws IOException {
            final Repository repository = Repository.builder()
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .name(faker.ancient().god())
                    .vcsOrganizationId(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .id(faker.pokemon().name())
                    .build();
            final String vcsUrl = faker.gameOfThrones().character();
            final GithubTagDTO[] githubTagsDTO = getStubsFromClassT("github/get_tags_for_organization_and_repository",
                    "get_repo_1_organization_1_tags.json",
                    GithubTagDTO[].class);
            final GithubTagDTO githubTagDTO1 = githubTagsDTO[0];
            githubTagDTO1.setVcsApiUrl(vcsUrl);

            // When
            final Tag tag = GithubMapper.mapTagToDomain(githubTagDTO1, repository);

            // Then
            assertThat(tag).isNotNull();
            assertThat(tag.getName()).isEqualTo("infrastructure-08-08-2022-3");
            assertThat(tag.getCommitSha()).isEqualTo("817245d0b26d7a252327c60da4eff4469ab0d9ab");
            assertThat(tag.getVcsUrl()).isEqualTo(vcsUrl);
            assertThat(tag.getRepositoryId()).isEqualTo(repository.getId());
        }
    }


    @Nested
    public class CommitCollection {


        @Test
        void should_collect_commits_for_first_task_of_first_job() throws SymeoException, IOException {
            // Given
            final Repository repository = Repository.builder()
                    .id(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .vcsOrganizationId(faker.pokemon().name())
                    .name(faker.name().firstName())
                    .build();
            final String urlHost = faker.gameOfThrones().character();
            final GithubProperties properties = new GithubProperties();
            properties.setSize(2);
            properties.setUrlHost(urlHost);
            final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                    rawStorageAdapter, properties, new ObjectMapper());
            final String branch1 = faker.ancient().god();
            final String branch2 = faker.ancient().hero();
            final List<String> branchNames = List.of(branch1, branch2);
            final Date startDate = stringToDate("2022-03-01");
            final Date endDate = stringToDate("2022-05-01");

            // When
            final GithubCommitsDTO[] githubCommitsDTOS11 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_1_size_2_page_1.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch1, startDate, endDate, 1, properties.getSize()))
                    .thenReturn(githubCommitsDTOS11);
            final GithubCommitsDTO[] githubCommitsDTOS12 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_1_size_2_page_2.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch1, startDate, endDate, 2, properties.getSize()))
                    .thenReturn(githubCommitsDTOS12);

            final GithubCommitsDTO[] githubCommitsDTOS21 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_2_size_2_page_1.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch2, startDate, endDate, 1, properties.getSize()))
                    .thenReturn(githubCommitsDTOS21);
            final GithubCommitsDTO[] githubCommitsDTOS22 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_2_size_2_page_2.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch2, startDate, endDate, 2, properties.getSize()))
                    .thenReturn(githubCommitsDTOS22);

            when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                    Commit.getNameForRepository(repository)))
                    .thenReturn(false);
            final List<Commit> commitsForBranchesInDateRange = githubAdapter.getCommitsForBranchesInDateRange(
                    repository, branchNames, startDate, endDate
            );

            // Then
            assertThat(commitsForBranchesInDateRange).hasSize(githubCommitsDTOS11.length + githubCommitsDTOS12.length
                    + githubCommitsDTOS21.length + githubCommitsDTOS22.length);
            final List<GithubCommitsDTO> githubCommitsDTOS = new ArrayList<>();
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS11).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS12).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS21).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS22).toList());
            final ArgumentCaptor<UUID> organizationIdCaptor = ArgumentCaptor.forClass(UUID.class);
            final ArgumentCaptor<String> adapterNameCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<String> contentNameCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(rawStorageAdapter, times(1))
                    .save(organizationIdCaptor.capture(),
                            adapterNameCaptor.capture(),
                            contentNameCaptor.capture(),
                            bytesCaptor.capture());
            assertThat(organizationIdCaptor.getValue()).isEqualTo(repository.getOrganizationId());
            assertThat(adapterNameCaptor.getValue()).isEqualTo("github");
            assertThat(contentNameCaptor.getValue()).isEqualTo(Commit.getNameForRepository(repository));
            final GithubCommitsDTO[] githubCommitsDTOSCaptured = objectMapper.readValue(bytesCaptor.getValue(),
                    GithubCommitsDTO[].class);
            for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS) {
                assertThat(githubCommitsDTOSCaptured).anyMatch(githubCommitsDTO::equals);
            }
        }

        @Test
        void should_collect_commits_for_first_tasks_of_second_job() throws SymeoException, IOException {
            // Given
            final Repository repository = Repository.builder()
                    .id(faker.idNumber().valid())
                    .organizationId(UUID.randomUUID())
                    .vcsOrganizationName(faker.rickAndMorty().character())
                    .vcsOrganizationId(faker.pokemon().name())
                    .name(faker.name().firstName())
                    .build();
            final String urlHost = faker.gameOfThrones().character();
            final GithubProperties properties = new GithubProperties();
            properties.setSize(2);
            properties.setUrlHost(urlHost);
            final GithubApiClientAdapter githubApiClientAdapter = mock(GithubApiClientAdapter.class);
            final RawStorageAdapter rawStorageAdapter = mock(RawStorageAdapter.class);
            final GithubAdapter githubAdapter = new GithubAdapter(githubApiClientAdapter,
                    rawStorageAdapter, properties, new ObjectMapper());
            final String branch1 = faker.ancient().god();
            final String branch2 = faker.ancient().hero();
            final List<String> branchNames = List.of(branch1, branch2);
            final Date startDate = stringToDate("2022-03-01");
            final Date endDate = stringToDate("2022-05-01");

            // When
            final GithubCommitsDTO[] githubCommitsDTOS11 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_1_size_2_page_1.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch1, startDate, endDate, 1, properties.getSize()))
                    .thenReturn(githubCommitsDTOS11);
            final GithubCommitsDTO[] githubCommitsDTOS12 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_1_size_2_page_2.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch1, startDate, endDate, 2, properties.getSize()))
                    .thenReturn(githubCommitsDTOS12);

            final GithubCommitsDTO[] githubCommitsDTOS21 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_2_size_2_page_1.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch2, startDate, endDate, 1, properties.getSize()))
                    .thenReturn(githubCommitsDTOS21);
            final GithubCommitsDTO[] githubCommitsDTOS22 = getStubsFromClassT("github/get_commits_for_branch",
                    "get_commits_for_branch_2_size_2_page_2.json",
                    GithubCommitsDTO[].class);
            when(githubApiClientAdapter.getCommitsForVcsOrganizationAndRepositoryAndBranchInDateRange(repository.getVcsOrganizationName(),
                    repository.getName(), branch2, startDate, endDate, 2, properties.getSize()))
                    .thenReturn(githubCommitsDTOS22);

            when(rawStorageAdapter.exists(repository.getOrganizationId(), "github",
                    Commit.getNameForRepository(repository)))
                    .thenReturn(true);
            final GithubCommitsDTO[] alreadyCollectedGithubCommitsDTOS = getStubsFromClassT("github" +
                            "/get_commits_for_branch",
                    "already_collected_commits.json",
                    GithubCommitsDTO[].class);
            when(rawStorageAdapter.read(repository.getOrganizationId(), "github",
                    Commit.getNameForRepository(repository)))
                    .thenReturn(dtoStubsToBytes(alreadyCollectedGithubCommitsDTOS));
            final List<Commit> commitsForBranchesInDateRange = githubAdapter.getCommitsForBranchesInDateRange(
                    repository, branchNames, startDate, endDate
            );

            // Then
            assertThat(commitsForBranchesInDateRange).hasSize(githubCommitsDTOS11.length + githubCommitsDTOS12.length
                    + githubCommitsDTOS21.length + githubCommitsDTOS22.length + alreadyCollectedGithubCommitsDTOS.length);
            final List<GithubCommitsDTO> githubCommitsDTOS = new ArrayList<>();
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS11).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS12).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS21).toList());
            githubCommitsDTOS.addAll(Arrays.stream(githubCommitsDTOS22).toList());
            githubCommitsDTOS.addAll(Arrays.stream(alreadyCollectedGithubCommitsDTOS).toList());
            final ArgumentCaptor<UUID> organizationIdCaptor = ArgumentCaptor.forClass(UUID.class);
            final ArgumentCaptor<String> adapterNameCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<String> contentNameCaptor = ArgumentCaptor.forClass(String.class);
            final ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
            verify(rawStorageAdapter, times(1))
                    .save(organizationIdCaptor.capture(),
                            adapterNameCaptor.capture(),
                            contentNameCaptor.capture(),
                            bytesCaptor.capture());
            assertThat(organizationIdCaptor.getValue()).isEqualTo(repository.getOrganizationId());
            assertThat(adapterNameCaptor.getValue()).isEqualTo("github");
            assertThat(contentNameCaptor.getValue()).isEqualTo(Commit.getNameForRepository(repository));
            final GithubCommitsDTO[] githubCommitsDTOSCaptured = objectMapper.readValue(bytesCaptor.getValue(),
                    GithubCommitsDTO[].class);
            for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS) {
                assertThat(githubCommitsDTOSCaptured).anyMatch(githubCommitsDTO::equals);
            }
        }
    }
}
