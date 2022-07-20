package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresExpositionAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresExpositionAdapterTestIT {

    private final Faker faker = new Faker();

    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Autowired
    private PullRequestHistogramRepository pullRequestHistogramRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;


    @AfterEach
    void tearDown() {
        pullRequestHistogramRepository.deleteAll();
        repositoryRepository.deleteAll();
    }

    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository);
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1),
                buildPullRequest(2),
                buildPullRequest(3)
        );

        // When
        postgresExpositionAdapter.savePullRequestDetails(pullRequestsToSave);

        // Then
        final List<PullRequestEntity> all = pullRequestRepository.findAll();
        assertThat(all).hasSize(pullRequestsToSave.size());
    }


    @Test
    void should_save_pull_request_histograms_to_postgres() {
        // Given
        final String organization1 = faker.name().firstName() + "-1";
        final String organization2 = faker.name().firstName() + "-2";
        final String organization3 = faker.name().firstName() + "-3";
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogram(organization1),
                buildPullRequestHistogram(organization2),
                buildPullRequestHistogram(organization3)
        );

        // When
        postgresExpositionAdapter.savePullRequestHistograms(pullRequestHistograms);

        // Then
        final List<PullRequestHistogramDataEntity> all = pullRequestHistogramRepository.findAll();
        assertThat(all).hasSize(3 * 5);

    }

    @Test
    void should_read_pull_request_histograms_given_an_organization_a_team_a_type() {
        // Given
        final String organization1 = faker.name().firstName() + "-1";
        final String organization2 = faker.name().firstName() + "-2";
        final String team1 = faker.name().firstName() + "-1";
        final String team2 = faker.name().firstName() + "-2";
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogramForOrgAndTeam(team1, organization1),
                buildPullRequestHistogramForOrgAndTeam(team2, organization2)
        );

        // When
        postgresExpositionAdapter.savePullRequestHistograms(pullRequestHistograms);
        final PullRequestHistogram pullRequestHistogram =
                postgresExpositionAdapter.readPullRequestHistogram(organization1,
                        team1, PullRequestHistogram.SIZE_LIMIT);

        // Then
        assertThat(pullRequestHistogram).isNotNull();
        assertThat(pullRequestHistogram.getTeam()).isEqualTo(team1);
        assertThat(pullRequestHistogram.getOrganization()).isEqualTo(organization1);
        assertThat(pullRequestHistogram.getDataByWeek()).hasSize(5);
    }

    @Test
    void should_save_repositories() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();

        // When
        postgresExpositionAdapter.saveRepositories(
                List.of(
                        buildRepository(organization),
                        buildRepository(organization),
                        buildRepository(organization)
                )
        );

        // Then
        assertThat(repositoryRepository.findAll()).hasSize(3);
    }

    @Test
    void should_read_repositories_for_an_organization() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        final List<RepositoryEntity> repositoryEntities = List.of(
                new RepositoryEntity(1L, faker.gameOfThrones().character(),
                        faker.name().firstName(), faker.friends().character(), organization.getId().toString()),
                new RepositoryEntity(2L, faker.gameOfThrones().character(),
                        faker.name().firstName(), faker.friends().character(), organization.getId().toString()),
                new RepositoryEntity(3L, faker.gameOfThrones().character(),
                        faker.name().firstName(), faker.friends().character(), organization.getId().toString()));
        repositoryRepository.saveAll(repositoryEntities);

        // When
        final List<Repository> repositories = postgresExpositionAdapter.readRepositoriesForOrganization(organization);

        // Then
        assertThat(repositories).hasSize(3);
    }

    private Repository buildRepository(Organization organization) {
        return Repository.builder()
                .organization(organization)
                .name(faker.pokemon().name())
                .vcsId(faker.address().firstName() + faker.ancient().god())
                .vcsOrganizationName(organization.getVcsOrganization().getName())
                .build();
    }

    private PullRequestHistogram buildPullRequestHistogram(String organizationName) {
        final String team = faker.name().firstName();
        return buildPullRequestHistogramForOrgAndTeam(team, organizationName);
    }

    private PullRequestHistogram buildPullRequestHistogramForOrgAndTeam(String team, String organizationName) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        DateHelper.getWeekStartDateForTheLastWeekNumber(5, TimeZone.getTimeZone(ZoneId.systemDefault()))
                .stream().map(date -> new SimpleDateFormat("dd/MM/yyyy").format(date))
                .forEach(dateAsString -> dataCompareToLimits.add(buildDataCompareToLimit(dateAsString)));


        return PullRequestHistogram.builder()
                .organization(organizationName)
                .team(team)
                .limit(faker.number().randomDigit())
                .type(PullRequestHistogram.SIZE_LIMIT)
                .dataByWeek(dataCompareToLimits)
                .build();
    }


    private DataCompareToLimit buildDataCompareToLimit(final String dateAsString) {
        return DataCompareToLimit.builder()
                .dateAsString(dateAsString)
                .numberAboveLimit(faker.number().randomDigit())
                .numberBelowLimit(faker.number().randomDigit())
                .build();
    }

    private PullRequest buildPullRequest(int id) {
        return PullRequest.builder()
                .id("fake-platform-name-" + id)
                .number(id)
                .title(faker.name().title())
                .lastUpdateDate(faker.date().past(1, TimeUnit.DAYS))
                .creationDate(faker.date().past(7, TimeUnit.DAYS))
                .mergeDate(new Date())
                .vcsUrl(faker.pokemon().name())
                .deletedLineNumber(faker.number().numberBetween(0, 20000))
                .authorLogin(faker.name().firstName())
                .commitNumber(faker.number().randomDigit())
                .addedLineNumber(faker.number().numberBetween(0, 20000))
                .isDraft(true)
                .isMerged(false)
                .build();
    }
}
