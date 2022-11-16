package io.symeo.monolithic.backend.job.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.github.GithubAdapter;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.mockito.Mockito.*;

public class VcsDataProcessingServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_collect_github_data_given_a_repository_and_a_date_range() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final GithubAdapter githubAdapter = mock(GithubAdapter.class);
        final CycleTimeDataService cycleTimeDataService = mock(CycleTimeDataService.class);
        final VcsDataProcessingService vcsDataProcessingService =
                new VcsDataProcessingService(githubAdapter, dataProcessingExpositionStorageAdapter,
                        cycleTimeDataService);
        final Repository repository = Repository.builder()
                .name(faker.name().firstName())
                .vcsOrganizationName(faker.rickAndMorty().location())
                .organizationId(UUID.randomUUID())
                .vcsOrganizationId(faker.rickAndMorty().character())
                .id(faker.idNumber().invalid())
                .build();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-01-03");
        final List<PullRequest> pullRequests = List.of(
                PullRequest.builder()
                        .number(faker.number().numberBetween(0, 100))
                        .lastUpdateDate(stringToDate("2022-01-02"))
                        .build(),
                PullRequest.builder()
                        .number(faker.number().numberBetween(0, 100) + 1)
                        .lastUpdateDate(stringToDate("2021-01-02"))
                        .build()

        );
        final String deployDetectionType = faker.rickAndMorty().character();
        final String pullRequestMergedOnBranchRegex = faker.name().name();
        final String tagRegex = faker.gameOfThrones().character();
        final List<String> excludedBranchRegex = List.of("main", "staging");

        final List<CycleTime> cycleTimes = List.of(
                CycleTime.builder()
                        .id(faker.cat().name())
                        .build()
        );

        // When
        when(githubAdapter.getPullRequestsWithLinkedCommentsForRepositoryAndDateRange(repository, startDate, endDate))
                .thenReturn(pullRequests);
        when(dataProcessingExpositionStorageAdapter.savePullRequestDetailsWithLinkedComments(pullRequests.subList(0, 1)))
                .thenReturn(pullRequests.subList(0, 1));
        when(cycleTimeDataService.computeCycleTimesForRepository(repository, pullRequests.subList(0, 1), deployDetectionType,
                pullRequestMergedOnBranchRegex, tagRegex, excludedBranchRegex, endDate))
                .thenReturn(cycleTimes);
        vcsDataProcessingService.collectVcsDataForRepositoryAndDateRange(repository, startDate, endDate,
                deployDetectionType, pullRequestMergedOnBranchRegex, tagRegex, excludedBranchRegex);

        // Then
        verify(dataProcessingExpositionStorageAdapter, times(1)).saveCycleTimes(cycleTimes);
    }

    @Test
    void should_collect_github_non_partial_data() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final GithubAdapter githubAdapter = mock(GithubAdapter.class);
        final CycleTimeDataService cycleTimeDataService = mock(CycleTimeDataService.class);
        final VcsDataProcessingService vcsDataProcessingService =
                new VcsDataProcessingService(githubAdapter, dataProcessingExpositionStorageAdapter,
                        cycleTimeDataService);
        final Repository repository = Repository.builder()
                .name(faker.name().firstName())
                .vcsOrganizationName(faker.rickAndMorty().location())
                .organizationId(UUID.randomUUID())
                .vcsOrganizationId(faker.rickAndMorty().character())
                .id(faker.idNumber().invalid())
                .build();
        final List<Tag> tags = List.of(Tag.builder().repositoryId(faker.idNumber().valid()).build());
        final Branch branch1 = Branch.builder().name(faker.ancient().god()).build();
        final Branch branch2 = Branch.builder().name(faker.ancient().hero()).build();
        final List<Branch> branches = List.of(
                branch1,
                branch2
        );
        final List<Commit> commits = List.of(
                Commit.builder().sha(faker.pokemon().name()).build(),
                Commit.builder().sha(faker.harryPotter().book()).build()
        );

        // When
        when(githubAdapter.getTags(repository)).thenReturn(tags);
        when(githubAdapter.getBranches(repository)).thenReturn(branches);
        when(githubAdapter.getCommitsForBranches(repository,
                branches.stream().map(Branch::getName).toList()))
                .thenReturn(commits);
        vcsDataProcessingService.collectNonPartialData(repository);

        // Then
        verify(dataProcessingExpositionStorageAdapter, times(1)).saveCommits(commits);
        verify(dataProcessingExpositionStorageAdapter, times(1)).saveTags(tags);
    }


    @Test
    void should_collect_repositories_given_a_vcs_organization() throws SymeoException {
        // Given
        final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter =
                mock(DataProcessingExpositionStorageAdapter.class);
        final GithubAdapter githubAdapter = mock(GithubAdapter.class);
        final CycleTimeDataService cycleTimeDataService = mock(CycleTimeDataService.class);
        final VcsDataProcessingService vcsDataProcessingService =
                new VcsDataProcessingService(githubAdapter, dataProcessingExpositionStorageAdapter,
                        cycleTimeDataService);
        final VcsOrganization vcsOrganization = VcsOrganization.builder()
                .externalId(faker.pokemon().name())
                .organizationId(UUID.randomUUID())
                .vcsId(faker.idNumber().invalid())
                .id(faker.number().randomNumber())
                .name(faker.robin().quote())
                .build();
        final List<Repository> repositories = List.of(
                Repository.builder()
                        .name(faker.name().firstName())
                        .vcsOrganizationName(faker.rickAndMorty().location())
                        .organizationId(UUID.randomUUID())
                        .vcsOrganizationId(faker.rickAndMorty().character())
                        .id(faker.idNumber().invalid())
                        .build()
        );

        // When
        when(githubAdapter.getRepositoriesForVcsOrganization(vcsOrganization))
                .thenReturn(repositories);
        vcsDataProcessingService.collectRepositoriesForVcsOrganization(vcsOrganization);

        // Then
        verify(dataProcessingExpositionStorageAdapter, times(1)).saveRepositories(repositories);
    }

}
