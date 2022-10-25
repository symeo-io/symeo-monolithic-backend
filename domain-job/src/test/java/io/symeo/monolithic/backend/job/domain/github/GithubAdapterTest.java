package io.symeo.monolithic.backend.job.domain.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.github.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.job.domain.github.mapper.GithubMapper;
import io.symeo.monolithic.backend.job.domain.github.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.Comment;
import io.symeo.monolithic.backend.job.domain.model.vcs.PullRequest;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import io.symeo.monolithic.backend.job.domain.port.out.GithubApiClientAdapter;
import io.symeo.monolithic.backend.job.domain.port.out.RawStorageAdapter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class GithubAdapterTest extends AbstractGithubAdapterTest {


    @Nested
    public class EmptyRawStorage {

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
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 1, 3))
                    .thenReturn(githubPullRequestStubs1);
            final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 2, 3))
                    .thenReturn(githubPullRequestStubs2);
            final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs3, updateDate);
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 3, 3))
                    .thenReturn(githubPullRequestStubs3);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository, startDate, endDate);

            // Then
            assertThat(pullRequestsForRepositoryAndDateRange)
                    .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length + githubPullRequestStubs3.length);
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
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 1, 3))
                    .thenReturn(githubPullRequestStubs1);
            final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 2, 3))
                    .thenReturn(githubPullRequestStubs2);
            final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs3, "2022-03-02");
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 3, 3))
                    .thenReturn(githubPullRequestStubs3);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository, startDate, endDate);

            // Then
            assertThat(pullRequestsForRepositoryAndDateRange)
                    .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length);
            verify(githubApiClientAdapter, times(3)).getPullRequestsForRepositoryAndOrganizationOrderByDescDate(any(),
                    any(), any(), any());
        }

    }

    @Nested
    public class NotEmptyRawStorage {


        @Test
        void should_get_pull_requests_given_already_collected_pull_requests_before_date_range() throws SymeoException
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
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 1, 3))
                    .thenReturn(githubPullRequestStubs1);
            final GithubPullRequestDTO[] githubPullRequestStubs2 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_2_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs2, updateDate);
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
                    repository.getName(), 2, 3))
                    .thenReturn(githubPullRequestStubs2);
            final GithubPullRequestDTO[] githubPullRequestStubs3 = getStubsFromClassT("github/empty_raw_storage" +
                            "/get_pull_requests_for_repo",
                    "get_pr_for_repo_page_3_size_3.json", GithubPullRequestDTO[].class);
            setGithubPullRequestDates(githubPullRequestStubs3, updateDate);
            when(githubApiClientAdapter.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(),
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
            setGithubPullRequestDates(githubPullRequestStubs3, updateDate);

            final List<PullRequest> pullRequestsForRepositoryAndDateRange =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository, startDate, endDate);

            // Then
            assertThat(pullRequestsForRepositoryAndDateRange)
                    .hasSize(githubPullRequestStubs1.length + githubPullRequestStubs2.length + githubPullRequestStubs3.length);
        }
    }


    private static void setGithubPullRequestDates(GithubPullRequestDTO[] githubPullRequestStubs1,
                                                  String date) throws SymeoException {
        for (GithubPullRequestDTO githubPullRequestDTO : githubPullRequestStubs1) {
            githubPullRequestDTO.setUpdatedAt(stringToDate(date));
            githubPullRequestDTO.setCreatedAt(stringToDate(date));
        }
    }


    @Nested
    public class CommentCollection {
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
            final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, mock(RawStorageAdapter.class),
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
            when(githubHttpApiClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                    .thenReturn(githubPullRequestStubs1);
            when(githubHttpApiClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80))
                    .thenReturn(githubPullRequestDetails);
            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(githubCommentsStubs1);

            final List<PullRequest> pullRequestsForRepositoryAndDateRange1 =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository
                            , stringToDate("1999-01-01"), stringToDate("2999-01-01"));
            final List<Comment> comments1 = pullRequestsForRepositoryAndDateRange1.get(0).getComments();

            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 2, properties.getSize()))
                    .thenReturn(githubCommentsStubs2);
            final List<PullRequest> pullRequestsForRepositoryAndDateRange2 =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository, stringToDate("1999-01-01"),
                            stringToDate("2999-01-01"));
            final List<Comment> comments2 =
                    pullRequestsForRepositoryAndDateRange2.get(0).getComments();

            when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                    repository.getName(), 80, 1, properties.getSize()))
                    .thenReturn(null);
            final List<PullRequest> nullCommentsPullRequestsForRepository =
                    githubAdapter.getPullRequestsForRepositoryAndDateRange(repository,
                            stringToDate("1999-01-01"), stringToDate("2999-01-01"));
            final List<Comment> nullCommentsResult =
                    nullCommentsPullRequestsForRepository.get(0).getComments();


            // Then
            assertThat(comments1.size()).isEqualTo(githubCommentsStubs1.length);
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
                assertThat(comments1).anyMatch(comment -> comment.getId()
                        .equals(githubCommentsDTO.getId())
                        && comment.getCreationDate().equals(githubCommentsDTO.getCreationDate())
                );
            }

            assertThat(pullRequestsForRepositoryAndDateRange2.size()).isEqualTo(githubCommentsStubs1.length + githubCommentsStubs2.length);
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
                assertThat(comments2).anyMatch(comment -> comment.getCreationDate().equals(githubCommentsDTO.getCreationDate())
                        && comment.getId().equals(githubCommentsDTO.getId()));
            }
            for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs2) {
                assertThat(comments2).anyMatch(comment -> comment.getId().equals(githubCommentsDTO.getId()) &&
                        comment.getCreationDate().equals(githubCommentsDTO.getCreationDate()));
            }

            assertThat(nullCommentsResult.size()).isEqualTo(0);

        }
    }
}
