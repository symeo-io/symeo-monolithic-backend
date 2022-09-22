package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubPullRequestDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
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
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubProperties properties = new GithubProperties();
        final int size = 5;
        properties.setSize(5);
        final GithubPullRequestDTO githubPullRequestDTO = new GithubPullRequestDTO();
        final Integer currentPullRequestNumber = faker.number().randomDigit();
        githubPullRequestDTO.setNumber(currentPullRequestNumber);
        final Repository repository = Repository.builder()
                .vcsOrganizationName(faker.rickAndMorty().character())
                .name(faker.ancient().god())
                .build();
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient, properties, new ObjectMapper());
        final GithubCommentsDTO[] githubCommentsStubs1 = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_1.json",
                GithubCommentsDTO[].class);
        final GithubCommentsDTO[] githubCommentsStubs2 = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_2.json",
                GithubCommentsDTO[].class);
        final GithubCommentsDTO[] githubCommentsStubs3 = getStubsFromClassT("get_comments_for_pr_number",
                "get_comments_for_pr_number_3.json",
                GithubCommentsDTO[].class);

        // When
        when(githubHttpClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber(), 1, size))
                .thenReturn(githubCommentsStubs1);
        when(githubHttpClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber(), 2, size))
                .thenReturn(githubCommentsStubs2);
        final GithubCommentsDTO[] nbrOfCommitMultipleOfPageSizeGithubCommentsDTOSResult = githubAdapter.getCommentsForPullRequestNumber(repository,githubPullRequestDTO);

        when(githubHttpClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber(), 3, size))
                .thenReturn(githubCommentsStubs3);
        final GithubCommentsDTO[] githubCommentsDTOSResult = githubAdapter.getCommentsForPullRequestNumber(repository,githubPullRequestDTO);

        when(githubHttpClient.getCommentsForPullRequestNumber(repository.getVcsOrganizationName(), repository.getName(), githubPullRequestDTO.getNumber(), 1, size))
                .thenReturn(null);
        final GithubCommentsDTO[] nullGithubCommentsDTOSResult = githubAdapter.getCommentsForPullRequestNumber(repository,githubPullRequestDTO);


        // Then
        assertThat(githubCommentsDTOSResult.length).isEqualTo(githubCommentsStubs1.length + githubCommentsStubs2.length + githubCommentsStubs3.length);
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
            assertThat(githubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs2) {
            assertThat(githubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs3) {
            assertThat(githubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }

        assertThat(nullGithubCommentsDTOSResult.length).isEqualTo(0);

        assertThat(nbrOfCommitMultipleOfPageSizeGithubCommentsDTOSResult.length).isEqualTo(githubCommentsStubs1.length + githubCommentsStubs2.length);
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs1) {
            assertThat(nbrOfCommitMultipleOfPageSizeGithubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }
        for (GithubCommentsDTO githubCommentsDTO : githubCommentsStubs2) {
            assertThat(nbrOfCommitMultipleOfPageSizeGithubCommentsDTOSResult).anyMatch(githubCommentsDTO::equals);
        }
    }
}
