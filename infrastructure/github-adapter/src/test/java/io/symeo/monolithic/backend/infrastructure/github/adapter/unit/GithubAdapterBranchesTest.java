package io.symeo.monolithic.backend.infrastructure.github.adapter.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Branch;
import io.symeo.monolithic.backend.infrastructure.github.adapter.GithubAdapter;
import io.symeo.monolithic.backend.infrastructure.github.adapter.client.GithubHttpClient;
import io.symeo.monolithic.backend.infrastructure.github.adapter.dto.GithubBranchDTO;
import io.symeo.monolithic.backend.infrastructure.github.adapter.properties.GithubProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GithubAdapterBranchesTest extends AbstractGithubAdapterTest {

    @Test
    void should_return_branches() throws IOException {
        // Given
        final GithubAdapter githubAdapter = new GithubAdapter(null, null, new ObjectMapper());
        final GithubBranchDTO[] getBranchesForRepos = getStubsFromClassT("get_branches_for_repo",
                "get_branches_for_repo_1.json", GithubBranchDTO[].class);

        // When
        final List<Branch> branches = githubAdapter.branchesBytesToDomain(dtoStubsToBytes(getBranchesForRepos));

        // Then
        assertThat(branches).hasSize(getBranchesForRepos.length);
        branches.forEach(branch -> assertThat(branch.getName()).isNotNull());
    }

    @Test
    void should_return_raw_branches_given_a_vcs_organization_name_and_a_repository_name() throws IOException,
            SymeoException {
        // Given
        final GithubHttpClient githubHttpClient = mock(GithubHttpClient.class);
        final GithubAdapter githubAdapter = new GithubAdapter(githubHttpClient, new GithubProperties(),
                new ObjectMapper());
        final String repositoryName = faker.ancient().god();
        final String vcsOrganizationName = faker.rickAndMorty().character();
        final GithubBranchDTO[] getBranchesForRepos = getStubsFromClassT("get_branches_for_repo",
                "get_branches_for_repo_1.json", GithubBranchDTO[].class);


        // When
        when(githubHttpClient.getBranchesForOrganizationAndRepository(vcsOrganizationName, repositoryName))
                .thenReturn(getBranchesForRepos);
        final byte[] rawBranches = githubAdapter.getRawBranches(vcsOrganizationName, repositoryName);

        // Then
        assertThat(rawBranches).isEqualTo(dtoStubsToBytes(getBranchesForRepos));
    }
}
