package io.symeo.monolithic.backend.infrastructure.github.adapter.it;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.installation.GithubInstallationAccessTokenDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.installation.GithubInstallationDTO;
import io.symeo.monolithic.backend.job.domain.model.vcs.github.repo.GithubRepositoryDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.AfterEach;
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
    public GithubAdapterITConfiguration.GithubJwtTokenProviderMock githubJwtTokenProvider;
    @Autowired
    public GithubAdapter githubAdapter;
    @Autowired
    public GithubProperties githubProperties;

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
        githubJwtTokenProvider.setCount(0);
    }

    @Test
    void should_get_repositories_from_organization() throws SymeoException, IOException {
        // Given
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("get_github_app_installations",
                "get_simple_github_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO = githubInstallationDTOS[0];
        final String organizationName = githubInstallationDTO.getAccount().getLogin();
        githubJwtTokenProvider.setSignedJwtToken(FAKER.animal().name());

        wireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.getSignedJwtToken()))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO = getStubsFromClassT(
                "get_github_app_installations",
                "post_github_app_installation_1.json",
                GithubInstallationAccessTokenDTO.class);
        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.getSignedJwtToken()))
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


    @Test
    void should_get_repositories_for_multiple_organization_and_refresh_token() throws IOException, SymeoException {
        // Given
        final GithubInstallationDTO[] githubInstallationDTOS = getStubsFromClassT("get_github_app_installations",
                "get_github_app_installations.json", GithubInstallationDTO[].class);
        final GithubInstallationDTO githubInstallationDTO1 = githubInstallationDTOS[0];
        final GithubInstallationDTO githubInstallationDTO2 = githubInstallationDTOS[1];
        final GithubInstallationDTO githubInstallationDTO3 = githubInstallationDTOS[2];
        final String organizationName1 = githubInstallationDTO1.getAccount().getLogin();
        final String organizationName2 = githubInstallationDTO2.getAccount().getLogin();
        final String organizationName3 = githubInstallationDTO3.getAccount().getLogin();
        final String signedJwtToken1 = FAKER.gameOfThrones().character();
        final String signedJwtToken2 = FAKER.dragonBall().character();
        githubJwtTokenProvider.setSignedJwtToken(signedJwtToken1);
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + signedJwtToken1))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/app/installations"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + signedJwtToken2))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationDTOS,
                                        200)));


        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO1 = getStubsFromClassT(
                "get_github_app_installations",
                "post_github_app_installation_1.json",
                GithubInstallationAccessTokenDTO.class);
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO2 = getStubsFromClassT(
                "get_github_app_installations",
                "post_github_app_installation_2.json",
                GithubInstallationAccessTokenDTO.class);
        final GithubInstallationAccessTokenDTO githubInstallationAccessTokenDTO3 = getStubsFromClassT(
                "get_github_app_installations",
                "post_github_app_installation_3.json",
                GithubInstallationAccessTokenDTO.class);

        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO1.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.getSignedJwtToken()))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO1,
                                        200)));
        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO2.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubJwtTokenProvider.getSignedJwtToken()))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO2,
                                        200)));
        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO3.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + signedJwtToken1))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO2,
                                        200)));
        wireMockServer.stubFor(
                post(
                        urlEqualTo("/app/installations/" + githubInstallationDTO3.getId() + "/access_tokens"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + signedJwtToken2))
                        .willReturn(
                                jsonResponse(
                                        githubInstallationAccessTokenDTO3,
                                        200)));


        final GithubRepositoryDTO[] getRepositoriesForOrg1 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_2_org_1.json", GithubRepositoryDTO[].class);
        final GithubRepositoryDTO[] getRepositoriesForOrg2 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_2_org_2.json", GithubRepositoryDTO[].class);
        final GithubRepositoryDTO[] getRepositoriesForOrg3 = getStubsFromClassT("get_repositories_for_org",
                "get_repo_for_org_page_1_size_2_org_3.json", GithubRepositoryDTO[].class);


        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName1 + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=1"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO1.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrg1,
                                        200)));
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName2 + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=1"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO2.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrg2,
                                        200)));
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName3 + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=1"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO2.getToken()))
                        .willReturn(
                                unauthorized()));
        wireMockServer.stubFor(
                get(
                        urlEqualTo("/orgs/" + organizationName3 + "/repos?sort=name&per_page=" + githubProperties.getSize() + "&page=1"))
                        .withHeader("Authorization",
                                equalTo("Bearer " + githubInstallationAccessTokenDTO3.getToken()))
                        .willReturn(
                                jsonResponse(
                                        getRepositoriesForOrg3,
                                        200)));

        // When
        final byte[] rawRepositories1 = githubAdapter.getRawRepositories(organizationName1);
        final byte[] rawRepositories2 = githubAdapter.getRawRepositories(organizationName2);
        githubJwtTokenProvider.setSignedJwtToken(signedJwtToken2);
        final byte[] rawRepositories3 = githubAdapter.getRawRepositories(organizationName3);


        // Then
        assertThat(githubJwtTokenProvider.getCount()).isEqualTo(3);
        final GithubRepositoryDTO[] githubRepositoryDTOS1 = objectMapper.readValue(rawRepositories1,
                GithubRepositoryDTO[].class);
        assertThat(githubRepositoryDTOS1).hasSize(getRepositoriesForOrg1.length);
        final GithubRepositoryDTO[] githubRepositoryDTOS2 = objectMapper.readValue(rawRepositories2,
                GithubRepositoryDTO[].class);
        assertThat(githubRepositoryDTOS2).hasSize(getRepositoriesForOrg2.length);
        final GithubRepositoryDTO[] githubRepositoryDTOS3 = objectMapper.readValue(rawRepositories3,
                GithubRepositoryDTO[].class);
        assertThat(githubRepositoryDTOS3).hasSize(getRepositoriesForOrg3.length);
    }
}
