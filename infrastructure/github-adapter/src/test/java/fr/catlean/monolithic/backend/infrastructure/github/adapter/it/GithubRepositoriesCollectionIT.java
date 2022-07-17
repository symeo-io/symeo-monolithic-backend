package fr.catlean.monolithic.backend.infrastructure.github.adapter.it;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationAccessTokenDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.installation.GithubInstallationDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.dto.repo.GithubRepositoryDTO;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.jwt.GithubJwtTokenProvider;
import fr.catlean.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

public class GithubRepositoriesCollectionIT extends AbstractGithubAdapterIT {

    @Autowired
    public GithubJwtTokenProvider githubJwtTokenProvider;
    @Autowired
    public GithubAdapter githubAdapter;
    @Autowired
    public GithubProperties githubProperties;

    @Test
    void should_get_repositories_from_organization() throws CatleanException, IOException {
        // Given
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("get_github_app_installations",
                "get_github_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[1];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();

        wireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.generateSignedJwtToken()))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO = getStubsFromClassT(
                "get_github_app_installations",
                "post_github_app_installation.json",
                GithubInstallationAccessTokenDTO.class);
        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO.getId() + "/access_token"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.generateSignedJwtToken()))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO,
                                        200)));
        final GithubRepositoryDTO[] getRepositoriesForOrgs1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_3.json", GithubRepositoryDTO[].class);
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=1"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrgs1,
                                        200)));
        final GithubRepositoryDTO[] getRepositoriesForOrgs2 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_2_size_3.json", GithubRepositoryDTO[].class);
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=2"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrgs2,
                                        200)));
        final GithubRepositoryDTO[] getRepositoriesForOrgs3 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_3_size_3.json", GithubRepositoryDTO[].class);
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=3"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrgs3,
                                        200)));


        // When
        final byte[] rawRepositories = githubAdapter.getRawRepositories(organizationName);


        // Then
        assertThat(rawRepositories).isNotNull();
        final GithubRepositoryDTO[] githubRepositoryDTOS = objectMapper.readValue(rawRepositories,
                GithubRepositoryDTO[].class);
        assertThat(githubRepositoryDTOS).hasSize(getRepositoriesForOrgs1.length + getRepositoriesForOrgs2.length + getRepositoriesForOrgs3.length);
        final List<String> allRepoNames = new ArrayList<>();
        allRepoNames.addAll(Arrays.stream(getRepositoriesForOrgs1).map(GithubRepositoryDTO::getName).toList());
        allRepoNames.addAll(Arrays.stream(getRepositoriesForOrgs2).map(GithubRepositoryDTO::getName).toList());
        allRepoNames.addAll(Arrays.stream(getRepositoriesForOrgs3).map(GithubRepositoryDTO::getName).toList());
        for (GithubRepositoryDTO githubRepositoryDTO : githubRepositoryDTOS) {
            assertThat(allRepoNames.contains(githubRepositoryDTO.getName())).isTrue();
        }
    }
}
