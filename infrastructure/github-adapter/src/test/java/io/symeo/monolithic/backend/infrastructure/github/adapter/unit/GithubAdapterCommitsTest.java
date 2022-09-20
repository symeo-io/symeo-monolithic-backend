package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
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
    void should_collect_commits_given_a_repository() throws SymeoException, IOException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        final int pageSize = 2;
        properties.setSize(pageSize);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient,
                properties, new ObjectMapper());
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final String repositoryName = faker.ancient().god();
        final byte[] alreadyCollectedCommits = faker.animal().name().getBytes();

        // When
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_repository",
                "get_commits_for_page_1_size_2.json",
                GithubCommitsDTO[].class);
        when(githubHttpClient.getCommitsForRepositoryAndOrganization(vcsOrganizationName, repositoryName, 1, pageSize))
                .thenReturn(githubCommitsDTOS1);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_repository",
                "get_commits_for_page_2_size_2.json",
                GithubCommitsDTO[].class);
        when(githubHttpClient.getCommitsForRepositoryAndOrganization(vcsOrganizationName, repositoryName, 2, pageSize))
                .thenReturn(githubCommitsDTOS2);
        final GithubCommitsDTO[] githubCommitsDTOS3 = getStubsFromClassT("get_commits_for_repository",
                "get_commits_for_page_3_size_2.json",
                GithubCommitsDTO[].class);
        when(githubHttpClient.getCommitsForRepositoryAndOrganization(vcsOrganizationName, repositoryName, 3, pageSize))
                .thenReturn(githubCommitsDTOS3);
        final byte[] rawCommitsForRepository = githubAdapter.getRawCommitsForRepository(
                vcsOrganizationName,
                repositoryName,
                alreadyCollectedCommits
        );

        // Then
        final List<Commit> commits = githubAdapter.commitsBytesToDomain(rawCommitsForRepository);
        Arrays.stream(githubCommitsDTOS1).map(GithubMapper::mapCommitToDomain)
                .forEach(commit -> assertThat(commits.contains(commit)).isTrue());
        Arrays.stream(githubCommitsDTOS2).map(GithubMapper::mapCommitToDomain)
                .forEach(commit -> assertThat(commits.contains(commit)).isTrue());
        Arrays.stream(githubCommitsDTOS3).map(GithubMapper::mapCommitToDomain)
                .forEach(commit -> assertThat(commits.contains(commit)).isTrue());
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
        final String branchName = faker.ancient().hero();
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_1.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_2.json",
                GithubCommitsDTO[].class);

        // When
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranch(vcsOrganizationName, repositoryName,
                branchName, 1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranch(vcsOrganizationName, repositoryName,
                branchName, 2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForBranchFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, branchName, null, new byte[0]);

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
        final String branchName = faker.ancient().hero();
        final GithubCommitsDTO[] githubCommitsDTOS1 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_1.json",
                GithubCommitsDTO[].class);
        final GithubCommitsDTO[] githubCommitsDTOS2 = getStubsFromClassT("get_commits_for_branch",
                "get_commits_for_branch_size_2_page_2.json",
                GithubCommitsDTO[].class);
        final Date lastCollectionDate = stringToDate("2020-01-01");

        // When
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(vcsOrganizationName, repositoryName,
                branchName, lastCollectionDate, 1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(vcsOrganizationName, repositoryName,
                branchName, lastCollectionDate, 2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForBranchFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, branchName, lastCollectionDate, new byte[0]);

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
        final String branchName = faker.ancient().hero();
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
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(vcsOrganizationName, repositoryName,
                branchName, lastCollectionDate, 1, size))
                .thenReturn(githubCommitsDTOS1);
        when(githubHttpClient.getCommitsForOrganizationAndRepositoryAndBranchFromLastCollectionDate(vcsOrganizationName, repositoryName,
                branchName, lastCollectionDate, 2, size))
                .thenReturn(githubCommitsDTOS2);
        final byte[] rawCommitsForBranchFromLastCollectionDate =
                githubAdapter.getRawCommitsForBranchFromLastCollectionDate(vcsOrganizationName,
                        repositoryName, branchName, lastCollectionDate,
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


}
