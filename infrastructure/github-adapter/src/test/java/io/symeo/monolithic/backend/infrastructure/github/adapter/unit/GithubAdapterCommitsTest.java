package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommitsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(commit.getDate()).isEqualTo(simpleDateFormat.parse("2022-08-24 " +
                "11:05:15"));
    }
}
