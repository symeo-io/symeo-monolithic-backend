package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubHttpApiClient;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import io.symeo.monolithic.backend.job.domain.model.vcs.Comment;
import io.symeo.monolithic.backend.job.domain.model.vcs.Repository;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubAdapterCommentsTest extends AbstractGithubAdapterTest {


    @Test
    void should_map_github_comments_dto_to_domain() throws IOException, ParseException {
        // Given
        final GithubCommentsDTO[] githubCommentsDTO = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_1.json",
                GithubCommentsDTO[].class);
        final GithubCommentsDTO githubCommentsDTO1 = githubCommentsDTO[0];
        final String githubPlatformName = faker.rickAndMorty().character();

        // When
        final Comment comment = GithubMapper.mapCommentToDomain(githubCommentsDTO1, githubPlatformName);

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
        final GithubHttpApiClient githubHttpApiClient = mock(GithubHttpApiClient.class);
        final GithubProperties properties = new GithubProperties();
        properties.setSize(3);
        final GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
        final Integer currentPullRequestNumber = faker.number().randomDigit();
        githubPullRequestDTO.setNumber(currentPullRequestNumber);
        final Repository repository = Repository.builder()
                .vcsOrganizationName(faker.rickAndMorty().character())
                .name(faker.ancient().god())
                .build();
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpApiClient, properties, new ObjectMapper());
        final GithubPullRequestDTO[] githubPullRequestStubs1 = getStubsFromClassT("get_pull_requests_for_repo",
                "get_pr_for_repo_page_1_size_1.json", GithubPullRequestDTO[].class);
        final GithubPullRequestDTO githubPullRequestDetails = getStubsFromClassT("get_pull_request_details_for_pr_id",
                "get_pr_76.json", GithubPullRequestDTO.class);
        final GithubCommentsDTO[] githubCommentsStubs1 = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_2_page_1_size_3.json",
                GithubCommentsDTO[].class);
        final GithubCommentsDTO[] githubCommentsStubs2 = getStubsFromClassT("get_comments_for_pr_number",
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

        final byte[] exactSizeRawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository
                , null);
        final GithubPullRequestDTO[] exactSizeGithubPullRequestDTOSResult =
                githubAdapter.bytesToDto(exactSizeRawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommentsDTO[] exactSizeGithubCommentsDTOSResult =
                exactSizeGithubPullRequestDTOSResult[0].getGithubCommentsDTOS();

        when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80, 2, properties.getSize()))
                .thenReturn(githubCommentsStubs2);
        final byte[] rawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);
        final GithubPullRequestDTO[] githubPullRequestDTOSResult =
                githubAdapter.bytesToDto(rawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommentsDTO[] githubCommentsDTOSResult = githubPullRequestDTOSResult[0].getGithubCommentsDTOS();

        when(githubHttpApiClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(),
                repository.getName(), 80, 1, properties.getSize()))
                .thenReturn(null);
        final byte[] nullRawPullRequestsForRepository = githubAdapter.getRawPullRequestsForRepository(repository, null);
        final GithubPullRequestDTO[] nullGithubPullRequestDTOSResult =
                githubAdapter.bytesToDto(nullRawPullRequestsForRepository, GithubPullRequestDTO[].class);
        final GithubCommentsDTO[] nullGithubCommentsDTOSResult =
                nullGithubPullRequestDTOSResult[0].getGithubCommentsDTOS();


        // Then
        assertThat(exactSizeGithubCommentsDTOSResult.length).isEqualTo(githubCommentsStubs1.length);
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
            assertThat(exactSizeGithubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }

        assertThat(githubCommentsDTOSResult.length).isEqualTo(githubCommentsStubs1.length + githubCommentsStubs2.length);
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
            assertThat(githubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs2) {
            assertThat(githubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }

        assertThat(nullGithubCommentsDTOSResult.length).isEqualTo(0);

    }
}
