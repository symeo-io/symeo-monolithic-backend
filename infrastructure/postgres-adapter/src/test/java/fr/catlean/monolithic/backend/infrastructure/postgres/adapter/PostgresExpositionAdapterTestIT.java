package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestSizeView;
import fr.catlean.monolithic.backend.domain.model.insight.view.PullRequestTimeToMergeView;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresExpositionAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestSizeRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.PullRequestTimeToMergeRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresExpositionAdapterTestIT {

    private final Faker faker = new Faker();

    @Autowired
    private PullRequestRepository pullRequestRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;
    @Autowired
    private PullRequestTimeToMergeRepository pullRequestTimeToMergeRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private PullRequestSizeRepository pullRequestSizeRepository;


    @AfterEach
    void tearDown() {
        teamRepository.deleteAll();
        pullRequestRepository.deleteAll();
        repositoryRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
    void should_save_repositories() {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
    void should_find_all_pull_requests_given_an_organization_given_a_null_team_id() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
                postgresExpositionAdapter.findAllPullRequestsForOrganizationAndTeamId(organization, null);

        // Then
        assertThat(allPullRequestsForOrganization).hasSize(4);
    }

    @Test
    void should_find_all_pull_requests_given_an_organization_given_a_team_id() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
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
                postgresExpositionAdapter.findAllPullRequestsForOrganizationAndTeamId(organization, null);

        // Then
        assertThat(allPullRequestsForOrganization).hasSize(4);
    }


    @Test
    void should_read_pr_time_and_pr_size_to_merge_view_given_a_null_team_id() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final List<PullRequestEntity> pullRequestEntities = pullRequestRepository.saveAll(
                List.of(
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(1, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(2, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(3, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(4, organization))
                )
        );
        repositoryRepository.saveAll(
                List.of(
                        RepositoryEntity.builder().id(pullRequestEntities.get(0).getVcsRepositoryId()).name(faker.dragonBall().character()).organizationId(organization.getId()).build(),
                        RepositoryEntity.builder().id(pullRequestEntities.get(1).getVcsRepositoryId()).name(faker.dragonBall().character()).organizationId(organization.getId()).build()
                )
        );
        final TeamEntity teamEntity =
                teamRepository.save(TeamEntity.builder().name(faker.rickAndMorty().character()).organizationId(organization.getId()).id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(pullRequestEntities.get(0).getVcsRepositoryId(),
                                        pullRequestEntities.get(1).getVcsRepositoryId()
                                )).build());


        // When
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());
        final List<PullRequestSizeView> pullRequestSizeViews =
                postgresExpositionAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());

        // Then
        assertThat(pullRequestTimeToMergeViews).hasSize(2);
        assertThat(pullRequestSizeViews).hasSize(2);
    }

    @Test
    void should_read_pr_time_and_pr_size_to_merge_view_given_a_team_id() throws CatleanException {
        // Given
        final PostgresExpositionAdapter postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository);
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .vcsOrganization(VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final List<PullRequestEntity> pullRequestEntities = pullRequestRepository.saveAll(
                List.of(
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(1, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(2, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(3, organization)),
                        PullRequestMapper.domainToEntity(buildPullRequestForOrganization(4, organization))
                )
        );
        repositoryRepository.saveAll(
                List.of(
                        RepositoryEntity.builder().id(pullRequestEntities.get(0).getVcsRepositoryId()).name(faker.dragonBall().character()).organizationId(organization.getId()).build(),
                        RepositoryEntity.builder().id(pullRequestEntities.get(1).getVcsRepositoryId()).name(faker.dragonBall().character()).organizationId(organization.getId()).build()
                )
        );
        final TeamEntity teamEntity =
                teamRepository.save(TeamEntity.builder().name(faker.rickAndMorty().character()).organizationId(organization.getId()).id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(pullRequestEntities.get(0).getVcsRepositoryId(),
                                        pullRequestEntities.get(1).getVcsRepositoryId()
                                )).build());

        // When
        final List<PullRequestTimeToMergeView> pullRequestTimeToMergeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());
        final List<PullRequestSizeView> pullRequestSizeViews =
                postgresExpositionAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());

        // Then
        assertThat(pullRequestTimeToMergeViews).hasSize(2);
        assertThat(pullRequestSizeViews).hasSize(2);
    }


    private Repository buildRepository(Organization organization) {
        return Repository.builder()
                .name(faker.pokemon().name())
                .id(faker.address().firstName() + faker.ancient().god())
                .vcsOrganizationName(organization.getVcsOrganization().getName())
                .vcsOrganizationId(organization.getVcsOrganization().getVcsId())
                .organizationId(organization.getId())
                .build();
    }

    private PullRequest buildPullRequest(int id) {
        return buildPullRequestForOrganization(id,
                Organization.builder().id(UUID.randomUUID()).vcsOrganization(VcsOrganization.builder().vcsId(faker.rickAndMorty().character()).name(faker.ancient().god()).build()).build());
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
                .repositoryId(faker.gameOfThrones().character())
                .isDraft(true)
                .isMerged(false)
                .organizationId(organization.getId())
                .vcsOrganizationId(organization.getVcsOrganization().getVcsId())
                .build();
    }
}
