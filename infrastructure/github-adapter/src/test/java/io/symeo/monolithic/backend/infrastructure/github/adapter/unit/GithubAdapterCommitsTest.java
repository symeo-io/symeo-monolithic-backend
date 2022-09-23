package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubAdapterCommitsTest extends AbstractGithubAdapterTest {


    @Test
    void should_map_github_commits_dto_to_domain() throws IOException, ParseException {
        // Given
        final GithubCommitsDTO[] githubCommitsDTO = getStubsFromClassT("get_commits_for_pr_number",
                "get_commits_for_pr_number_1.json",
                GithubCommitsDTO[].class);

        // When
        final Commit commit = GithubMapper.mapCommitToDomain(githubCommitsDTO[0]);

        // Then
        assertThat(commit).isNotNull();
        assertThat(commit.getSha()).isEqualTo("e3d2d7b72e93c3b63cd597eb3bec630fad8e000c");
        assertThat(commit.getAuthor()).isEqualTo("pierre.oucif");
        assertThat(commit.getMessage()).isEqualTo("Init for Biox");
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
        assertThat(commit.getDate()).isEqualTo(simpleDateFormat.parse("2022-08-24 " +
                "11:05:15"));
    }

    @Test
    void should_collect_commits_given_a_repository_and_no_already_collected_commits_and_no_collection_date() throws SymeoException,
            IOException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        final int size = 2;
        properties.setSize(size);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
                properties, new ObjectMapper());
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String repositoryName = faker.ancient().god();
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_1.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_2.json",
                GithubCommitsDTO[].class);

        // When
        when(githubHttpClient.getCommitsForOrganizationAndRepository(vcsOrganizationName, repositoryName,
                1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepository(vcsOrganizationName, repositoryName,
                2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForRepositoryFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, null, new byte[0]);

        // Then
        final GithubCommitsDTO[] githubCommitsDTOSResult =
                githubAdapter.bytesToDto(rawCommitsForBranchFromLastCollectionDate, GithubCommitsDTO[].class);
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS1) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS2) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
    }


    @Test
    void should_collect_commits_given_a_repository_and_no_already_collected_commits_and_a_collection_date() throws SymeoException,
            IOException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        final int size = 2;
        properties.setSize(size);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
                properties, new ObjectMapper());
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String repositoryName = faker.ancient().god();
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_1.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_2.json",
                GithubCommitsDTO[].class);
        final Date lastCollectionDate = stringToDate("2020-01-01");

        // When
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryFromLastCollectionDate(vcsOrganizationName,
                repositoryName,
                lastCollectionDate, 1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryFromLastCollectionDate(vcsOrganizationName,
                repositoryName,
                lastCollectionDate, 2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForRepositoryFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, lastCollectionDate, new byte[0]);

        // Then
        final GithubCommitsDTO[] githubCommitsDTOSResult =
                githubAdapter.bytesToDto(rawCommitsForBranchFromLastCollectionDate, GithubCommitsDTO[].class);
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS1) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS2) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
    }

    @Test
    void should_collect_commits_given_a_repository_and_already_collected_commits_and_a_collection_date() throws SymeoException,
            IOException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        final int size = 2;
        properties.setSize(size);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
                properties, new ObjectMapper());
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String repositoryName = faker.ancient().god();
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_1.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_2.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] alreadyCollectedGithubCommitsDTOS = getStubsFromClassT("get_commits_for_branch",
                "already_collected_commits.json",
                GithubCommitsDTO[].class);
        final Date lastCollectionDate = stringToDate("2020-01-01");

        // When
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryFromLastCollectionDate(vcsOrganizationName,
                repositoryName,
                lastCollectionDate, 1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryFromLastCollectionDate(vcsOrganizationName,
                repositoryName,
                lastCollectionDate, 2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForRepositoryFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, lastCollectionDate,
                        githubAdapter.dtoToBytes(alreadyCollectedGithubCommitsDTOS));

        // Then
        final GithubCommitsDTO[] githubCommitsDTOSResult =
                githubAdapter.bytesToDto(rawCommitsForBranchFromLastCollectionDate, GithubCommitsDTO[].class);
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS1) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsDTOS2) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
        for (GithubCommitsDTO githubCommitsDTO : alreadyCollectedGithubCommitsDTOS) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
    }

    @Test
    void should_collect_commits_given_a_pull_request_number() throws SymeoException, IOException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        properties.setSize(30);
        final GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
        final Integer currentPullRequestNumber = faker.number().randomDigit();
        githubPullRequestDTO.setNumber(currentPullRequestNumber);
        final Repository repository = Repository.builder()
                .vcsOrganizationName(faker.rickAndMorty().character())
                .name(faker.ancient().god())
                .build();
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient, properties, new ObjectMapper());
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT("get_pull_request_details_for_pr_id",
                "get_pr_76.json", GithubPullRequestDTO.class);
        final GithubCommitsDTO[] githubCommitsStubs1 = getStubsFromClassT("get_commits_for_pr_number",
                "get_commits_for_pr_number_2_page_1_size_30.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsStubs2 = getStubsFromClassT("get_commits_for_pr_number",
                "get_commits_for_pr_number_2_page_2_size_28.json",
                GithubCommitsDTO[].class);

        // When
        when(githubHttpClient.getPullRequestsForRepositoryAndOrganizationOrderByDescDate(repository.getVcsOrganizationName(), repository.getName(), 1, properties.getSize()))
                .thenReturn(githubPullRequestStubs1);
        when(githubHttpClient.getPullRequestDetailsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), 80))
                .thenReturn(githubPullRequestDetails);
        when(githubHttpClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), 80, 1, properties.getSize()))
                .thenReturn(githubCommitsStubs1);

        final byte[] exactSizeRawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);
        final GithubPullRequestDTO[] exactSizeGithubPullRequestDTOSResult = githubAdapter.bytesToDto(exactSizeRawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommitsDTO[] exactSizeGithubCommitsDTOSResult = exactSizeGithubPullRequestDTOSResult[0].getGithubCommitsDTOS();

        when(githubHttpClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), 80, 2, properties.getSize()))
                .thenReturn(githubCommitsStubs2);
        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);
        final GithubPullRequestDTO[] githubPullRequestDTOSResult = githubAdapter.bytesToDto(rawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOSResult = githubPullRequestDTOSResult[0].getGithubCommitsDTOS();

        when(githubHttpClient.getCommitsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), 80, 1, properties.getSize()))
                .thenReturn(null);
        final byte[] nullRawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);
        final GithubPullRequestDTO[] nullGithubPullRequestDTOSResult = githubAdapter.bytesToDto(nullRawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommitsDTO[] nullGithubCommitsDTOSResult = nullGithubPullRequestDTOSResult[0].getGithubCommitsDTOS();


        // Then
        assertThat(exactSizeGithubCommitsDTOSResult.length).isEqualTo(githubCommitsStubs1.length);
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs1) {
            assertThat(exactSizeGithubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }

        assertThat(githubCommitsDTOSResult.length).isEqualTo(githubCommitsStubs1.length + githubCommitsStubs2.length);
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs1) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }
        for (GithubCommitsDTO githubCommitsDTO : githubCommitsStubs2) {
            assertThat(githubCommitsDTOSResult).anyMatch(githubCommitsDTO::equals);
        }

        assertThat(nullGithubCommitsDTOSResult.length).isEqualTo(0);
    }


}
