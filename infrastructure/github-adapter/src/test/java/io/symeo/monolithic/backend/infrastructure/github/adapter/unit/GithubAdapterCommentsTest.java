package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import io.symeo.monolithic.backend.domain.model.platform.vcs.Comment;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.pr.GithubCommentsDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

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
}
