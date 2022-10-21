package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.GithubTagDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.mapper.GithubMapper;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubAdapterTagTest extends AbstractGithubAdapterTest {

    final private static Faker faker = new Faker();

    @Test
    void should_collect_tag_given_an_organizationName_and_repository() throws IOException, SymeoException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final String urlHost = faker.gameOfThrones().character();
        final GithubProperties properties = new GithubProperties();
        properties.setSize(3);
        properties.setUrlHost(urlHost);
        final Repository repository = Repository.builder()
                .vcsOrganizationName(faker.rickAndMorty().character())
                .name(faker.ancient().god())
                .build();
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient, properties, new ObjectMapper());
        final GithubTagDTO[] githubTagStub = getStubsFromClassT("get_tags_for_organization_and_repository",
                "get_repo_1_organization_1_tags.json", GithubTagDTO[].class);

        // When
        when(githubHttpClient.getTagsForOrganizationAndRepository(repository.getVcsOrganizationName(), repository.getName()))
                .thenReturn(githubTagStub);
        final GithubTagDTO[] resultGithubTagDTOS = githubAdapter.bytesToDto(
                githubAdapter.getRawTags(repository.getVcsOrganizationName(), repository.getName()),
                GithubTagDTO[].class);
        final GithubTagDTO firstGithubTagDTO = resultGithubTagDTOS[0];

        // Then
        assertThat(resultGithubTagDTOS.length).isEqualTo(githubTagStub.length);
        for (GithubTagDTO githubTagDTO : githubTagStub) {
            assertThat(resultGithubTagDTOS).anyMatch(githubTagDTO::equals);
        }
        assertThat(firstGithubTagDTO.getVcsApiUrl()).isEqualTo(
                urlHost
                        + repository.getVcsOrganizationName()
                        + "/" + repository.getName()
                        + "/tree/" + firstGithubTagDTO.getRef().replace("refs/tags/", ""));

    }

    @Test
    void should_map_github_tags_dto_to_domain() throws IOException {
        final String vcsUrl = faker.gameOfThrones().character();
        final GithubTagDTO[] githubTagsDTO = getStubsFromClassT("get_tags_for_organization_and_repository",
                "get_repo_1_organization_1_tags.json",
                GithubTagDTO[].class);
        final GithubTagDTO githubTagDTO1 = githubTagsDTO[0];
        githubTagDTO1.setVcsApiUrl(vcsUrl);

        // When
        final Tag tag = GithubMapper.mapTagToDomain(githubTagDTO1);

        // Then
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo("infrastructure-08-08-2022-3");
        assertThat(tag.getCommitSha()).isEqualTo("817245d0b26d7a252327c60da4eff4469ab0d9ab");
        assertThat(tag.getVcsUrl()).isEqualTo(vcsUrl);
    }

}
