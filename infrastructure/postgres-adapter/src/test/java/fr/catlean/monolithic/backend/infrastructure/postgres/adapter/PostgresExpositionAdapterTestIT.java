package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.DataCompareToLimit;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresExpositionAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestHistogramDataEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestHistogramRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
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
    @Autowired
    private PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;


    @AfterEach
    void tearDown() {
        pullRequestHistogramRepository.deleteAll();
        repositoryRepository.deleteAll();
    }

    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
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
        final UUID organizationId1 = UUID.randomUUID();
        final UUID organizationId2 = UUID.randomUUID();
        final UUID organizationId3 = UUID.randomUUID();
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogram(organizationId1),
                buildPullRequestHistogram(organizationId2),
                buildPullRequestHistogram(organizationId3)
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
        final UUID organizationId1 = UUID.randomUUID();
        final UUID organizationId2 = UUID.randomUUID();
        final String team1 = faker.name().firstName() + "-1";
        final String team2 = faker.name().firstName() + "-2";
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
        final List<PullRequestHistogram> pullRequestHistograms = List.of(
                buildPullRequestHistogramForOrgAndTeam(team1, organizationId1),
                buildPullRequestHistogramForOrgAndTeam(team2, organizationId2)
        );

        // When
        postgresExpositionAdapter.savePullRequestHistograms(pullRequestHistograms);
        final PullRequestHistogram pullRequestHistogram =
                postgresExpositionAdapter.readPullRequestHistogram(organizationId1.toString(),
                        team1, PullRequestHistogram.SIZE_LIMIT);

        // Then
        assertThat(pullRequestHistogram).isNotNull();
        assertThat(pullRequestHistogram.getTeam()).isEqualTo(team1);
        assertThat(pullRequestHistogram.getOrganizationId()).isEqualTo(organizationId1);
        assertThat(pullRequestHistogram.getDataByWeek()).hasSize(5);
    }

    @Test
    void should_save_repositories() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
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
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        final List<RepositoryEntity> repositoryEntities = List.of(
                RepositoryEntity.builder().id("1L").name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization.getId()).build(),
                RepositoryEntity.builder().id("2L").name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization.getId()).build(),
                RepositoryEntity.builder().id("3L").name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization.getId()).build());
        repositoryRepository.saveAll(repositoryEntities);

        // When
        final List<Repository> repositories = postgresExpositionAdapter.readRepositoriesForOrganization(organization);

        // Then
        assertThat(repositories).hasSize(3);
    }

    @Test
    void should_find_all_pull_requests_given_an_organization() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        pullRequestRepository.saveAll(
                List.of(
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(1, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(2, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(3, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(4, organization))
                )
        );

        // When
        final List<PullRequest> allPullRequestsForOrganization =
                postgresExpositionAdapter.findAllPullRequestsForOrganization(organization);

        // Then
        assertThat(allPullRequestsForOrganization).hasSize(4);
    }

    @Test
    void should_read_pr_time_to_merge_view() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                pullRequestHistogramRepository, repositoryRepository, pullRequestTimeToMergeRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        pullRequestRepository.saveAll(
                List.of(
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(1, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(2, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(3, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(4, organization))
                )
        );

        // When
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                        UUID.randomUUID());

        // Then
        assertThat(pullRequestTimeToMergeViews).hasSize(4);
    }

    private Repository buildRepository(Organization organization) {
        return Repository.builder()
                .organization(organization)
                .name(faker.pokemon().name())
                .id(faker.address().firstName() + faker.ancient().god())
                .vcsOrganizationName(organization.getVcsOrganization().getName())
                .build();
    }

    private PullRequestHistogram buildPullRequestHistogram(UUID organizationId) {
        final String team = faker.name().firstName();
        return buildPullRequestHistogramForOrgAndTeam(team, organizationId);
    }

    private PullRequestHistogram buildPullRequestHistogramForOrgAndTeam(String team, UUID organizationId) {
        final List<DataCompareToLimit> dataCompareToLimits = new ArrayList<>();
        DateHelper.getWeekStartDateForTheLastWeekNumber(5, TimeZone.getTimeZone(ZoneId.systemDefault()))
                .stream().map(date -> new SimpleDateFormat("dd/MM/yyyy").format(date))
                .forEach(dateAsString -> dataCompareToLimits.add(buildDataCompareToLimit(dateAsString)));


        return PullRequestHistogram.builder()
                .organizationId(organizationId)
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
        return buildPullRequestForOrganization(id, Organization.builder().id(UUID.randomUUID()).build());
    }

    private PullRequest buildPullRequestForOrganization(final int id, final Organization organization) {
        return PullRequest.builder()
                .id(faker.dragonBall().character() + id)
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
                .organizationId(organization.getId())
                .build();
    }
}
