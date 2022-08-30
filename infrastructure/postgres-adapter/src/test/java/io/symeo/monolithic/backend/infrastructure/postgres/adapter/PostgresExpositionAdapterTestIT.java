package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.*;
import io.symeo.monolithic.backend.infrastructure.postgres.PostgresExpositionAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommentEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.CommitEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.PullRequestEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.PullRequestMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.time.ZonedDateTime;
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
    @Autowired
    private PullRequestFullViewRepository pullRequestFullViewRepository;
    @Autowired
    private CustomPullRequestViewRepository customPullRequestViewRepository;
    @Autowired
    private CommitRepository commitRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PullRequestWithCommitsAndCommentsRepository pullRequestWithCommitsAndCommentsRepository;
    private PostgresExpositionAdapter postgresExpositionAdapter;

    @AfterEach
    void tearDown() {
        commitRepository.deleteAll();
        commentRepository.deleteAll();
        teamRepository.deleteAll();
        pullRequestRepository.deleteAll();
        repositoryRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository,
                pullRequestFullViewRepository, customPullRequestViewRepository,
                pullRequestWithCommitsAndCommentsRepository);
    }

    @Test
    void should_save_pull_requests_to_postgres() {
        // Given
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1),
                buildPullRequest(2),
                buildPullRequest(3)
        );

        // When
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(pullRequestsToSave);

        // Then
        final List<PullRequestEntity> all = pullRequestRepository.findAll();
        assertThat(all).hasSize(pullRequestsToSave.size());
    }

    @Test
    void should_save_pull_requests_with_commits() {
        // Given
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1).toBuilder()
                        .commits(List.of(
                                Commit.builder()
                                        .sha(faker.dragonBall().character() + "-11")
                                        .author(faker.ancient().god())
                                        .date(new Date())
                                        .build(),
                                Commit.builder()
                                        .sha(faker.dragonBall().character() + "-12")
                                        .author(faker.ancient().god())
                                        .date(new Date())
                                        .build()

                        )).build(),
                buildPullRequest(2).toBuilder()
                        .commits(List.of(
                                Commit.builder()
                                        .sha(faker.dragonBall().character() + "-11")
                                        .author(faker.ancient().god())
                                        .date(new Date())
                                        .build()

                        )).build()
        );

        // When
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(pullRequestsToSave);

        // Then
        final List<CommitEntity> commitEntities = commitRepository.findAll();
        assertThat(commitEntities).hasSize(3);
    }

    @Test
    void should_save_pull_requests_with_comments() {
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1).toBuilder()
                        .comments(List.of(
                                Comment.builder()
                                        .creationDate(new Date())
                                        .id(faker.rickAndMorty().character())
                                        .build(),
                                Comment.builder()
                                        .creationDate(new Date())
                                        .id(faker.rickAndMorty().character())
                                        .build()

                        )).build(),
                buildPullRequest(2).toBuilder()
                        .comments(List.of(
                                Comment.builder()
                                        .creationDate(new Date())
                                        .id(faker.rickAndMorty().character())
                                        .build()
                        )).build()
        );

        // When
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedCommitsAndComments(pullRequestsToSave);

        // Then
        final List<CommentEntity> commentEntities = commentRepository.findAll();
        assertThat(commentEntities).hasSize(3);
    }

    @Test
    void should_save_repositories() {
        // Given
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
    void should_find_all_pull_requests_given_an_organization_given_a_null_team_id() throws SymeoException {
        // Given
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
    void should_find_all_pull_requests_given_an_organization_given_a_team_id() throws SymeoException {
        // Given
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
    void should_read_pr_time_and_pr_size_to_merge_view_given_a_null_team_id() throws SymeoException {
        // Given
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
        final List<PullRequestView> pullRequestTimeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());
        final List<PullRequestView> pullRequestSizeViews =
                postgresExpositionAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());

        // Then
        assertThat(pullRequestTimeViews).hasSize(2);
        assertThat(pullRequestSizeViews).hasSize(2);
    }

    @Test
    void should_read_pr_time_and_pr_size_to_merge_view_given_a_team_id() throws SymeoException {
        // Given
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
        final List<PullRequestView> pullRequestTimeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());
        final List<PullRequestView> pullRequestSizeViews =
                postgresExpositionAdapter.readPullRequestsSizeViewForOrganizationAndTeam(organization,
                        teamEntity.getId());

        // Then
        assertThat(pullRequestTimeViews).hasSize(2);
        assertThat(pullRequestSizeViews).hasSize(2);
    }


    @Test
    void should_find_and_count_all_pr_full_view_details_given_a_team_id_and_date_range_and_pagination() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final String repositoryId = faker.rickAndMorty().character();
        repositoryRepository.save(RepositoryEntity.builder().id(repositoryId).name(faker.ancient().god()).build());
        final UUID teamId = UUID.randomUUID();
        organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.name().firstName())
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder().id(teamId)
                        .name(faker.ancient().hero())
                        .organizationId(organizationId)
                        .repositoryIds(List.of(repositoryId)).build()
        );
        final ZonedDateTime now = ZonedDateTime.now();
        pullRequestRepository.saveAll(List.of(
                PullRequestEntity.builder()
                        .id(faker.rickAndMorty().character() + "-1")
                        .code(faker.harryPotter().book())
                        .organizationId(organizationId)
                        .creationDate(now.minusDays(50))
                        .mergeDate(now.minusDays(49))
                        .vcsRepositoryId(repositoryId)
                        .authorLogin(faker.dragonBall().character())
                        .title(faker.gameOfThrones().character())
                        .lastUpdateDate(now)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.rickAndMorty().character() + "-2")
                        .code(faker.harryPotter().book())
                        .organizationId(organizationId)
                        .creationDate(now.minusDays(30))
                        .vcsRepositoryId(repositoryId)
                        .authorLogin(faker.dragonBall().character())
                        .title(faker.gameOfThrones().character())
                        .lastUpdateDate(now)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.rickAndMorty().character() + "-3")
                        .code(faker.harryPotter().book())
                        .organizationId(organizationId)
                        .creationDate(now.minusDays(20))
                        .vcsRepositoryId(repositoryId)
                        .authorLogin(faker.dragonBall().character())
                        .title(faker.gameOfThrones().character())
                        .lastUpdateDate(now)
                        .build(),
                PullRequestEntity.builder()
                        .id(faker.rickAndMorty().character() + "-4")
                        .organizationId(organizationId)
                        .code(faker.harryPotter().book())
                        .creationDate(now.minusDays(20))
                        .mergeDate(now.minusDays(19))
                        .vcsRepositoryId(repositoryId)
                        .authorLogin(faker.dragonBall().character())
                        .title(faker.gameOfThrones().character())
                        .lastUpdateDate(now)
                        .build(),
                PullRequestEntity.builder()
                        .code(faker.harryPotter().book())
                        .id(faker.rickAndMorty().character() + "-5")
                        .organizationId(organizationId)
                        .creationDate(now.minusDays(20))
                        .mergeDate(now.minusDays(1))
                        .vcsRepositoryId(repositoryId)
                        .authorLogin(faker.dragonBall().character())
                        .title(faker.gameOfThrones().character())
                        .lastUpdateDate(now)
                        .build()
        ));


        // When
        final Date from = Date.from(now.minusDays(40).toInstant());
        final Date to = new Date();
        final int pageSize = 3;
        final int count = postgresExpositionAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(
                teamId, from, to
        );
        final List<PullRequestView> pullRequestViewsPage11 =
                postgresExpositionAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(
                        teamId, from, to, 0, pageSize,
                        "creation_date", "asc"
                );
        final List<PullRequestView> pullRequestViewsPage12 =
                postgresExpositionAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(
                        teamId, from, to, 0, pageSize,
                        "size", "asc"
                );
        final List<PullRequestView> pullRequestViewsPage21 =
                postgresExpositionAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(
                        teamId, from, to, 1, pageSize,
                        "creation_date", "asc"
                );
        final List<PullRequestView> pullRequestViewsPage22 =
                postgresExpositionAdapter.readPullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted(
                        teamId, from, to, 1, pageSize,
                        "creation_date", "desc"
                );


        // Then
        assertThat(count).isEqualTo(4);
        assertThat(pullRequestViewsPage11.size()).isEqualTo(pageSize);
        assertThat(pullRequestViewsPage12.size()).isEqualTo(pageSize);
        assertThat(pullRequestViewsPage21.size()).isEqualTo(1);
        assertThat(pullRequestViewsPage21.size()).isEqualTo(1);
        assertThat(pullRequestViewsPage22.get(0)).isNotEqualTo(pullRequestViewsPage21.get(0));
        assertThat(pullRequestViewsPage12.get(0)).isNotEqualTo(pullRequestViewsPage11.get(0));
    }

    @Test
    void should_return_pull_requests_with_commits_and_comments() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final String repositoryId = faker.rickAndMorty().character();
        repositoryRepository.save(RepositoryEntity.builder().id(repositoryId).name(faker.ancient().god()).build());
        final UUID teamId = UUID.randomUUID();
        organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.name().firstName())
                        .build()
        );
        teamRepository.save(
                TeamEntity.builder().id(teamId)
                        .name(faker.ancient().hero())
                        .organizationId(organizationId)
                        .repositoryIds(List.of(repositoryId)).build()
        );
        final ZonedDateTime now = ZonedDateTime.now();
        final PullRequestEntity pr1 = PullRequestEntity.builder()
                .id("1")
                .code(faker.harryPotter().book())
                .organizationId(organizationId)
                .creationDate(now.minusDays(25))
                .mergeDate(now.minusDays(20))
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(now)
                .state(PullRequest.MERGE)
                .build();
        final PullRequestEntity pr11 = PullRequestEntity.builder()
                .id("11")
                .code(faker.harryPotter().book())
                .organizationId(organizationId)
                .creationDate(now.minusDays(50))
                .mergeDate(now.minusDays(49))
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(now)
                .state(PullRequest.MERGE)
                .build();

        final PullRequestEntity pr2 = PullRequestEntity.builder()
                .id("2")
                .code(faker.harryPotter().book())
                .organizationId(organizationId)
                .creationDate(now.minusDays(30))
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .state(PullRequest.MERGE)
                .lastUpdateDate(now)
                .build();
        final PullRequestEntity pr3 = PullRequestEntity.builder()
                .id("3")
                .code(faker.harryPotter().book())
                .organizationId(organizationId)
                .creationDate(now.minusDays(30))
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .state(PullRequest.OPEN)
                .lastUpdateDate(now)
                .build();
        pullRequestRepository.saveAll(List.of(
                pr1,
                pr2,
                pr3,
                pr11
        ));
        commentRepository.saveAll(
                List.of(
                        CommentEntity.builder()
                                .id("1")
                                .pullRequest(pr1)
                                .creationDate(ZonedDateTime.now())
                                .build(),
                        CommentEntity.builder()
                                .id("2")
                                .pullRequest(pr1)
                                .creationDate(ZonedDateTime.now())
                                .build(),
                        CommentEntity.builder()
                                .id("3")
                                .pullRequest(pr2)
                                .creationDate(ZonedDateTime.now())
                                .build()
                )
        );
        commitRepository.saveAll(
                List.of(
                        CommitEntity.builder()
                                .sha(faker.dragonBall().character())
                                .pullRequest(pr1)
                                .date(ZonedDateTime.now())
                                .message(faker.gameOfThrones().character())
                                .authorLogin(faker.gameOfThrones().dragon())
                                .build(),
                        CommitEntity.builder()
                                .sha(faker.rickAndMorty().character())
                                .pullRequest(pr1)
                                .date(ZonedDateTime.now())
                                .message(faker.gameOfThrones().character())
                                .authorLogin(faker.gameOfThrones().dragon())
                                .build(),
                        CommitEntity.builder()
                                .sha(faker.rickAndMorty().location())
                                .pullRequest(pr2)
                                .date(ZonedDateTime.now())
                                .message(faker.gameOfThrones().character())
                                .authorLogin(faker.gameOfThrones().dragon())
                                .build(),
                        CommitEntity.builder()
                                .sha(faker.demographic().race())
                                .pullRequest(pr2)
                                .date(ZonedDateTime.now())
                                .message(faker.gameOfThrones().character())
                                .authorLogin(faker.gameOfThrones().dragon())
                                .build()
                )
        );
        final Date startDate = Date.from(now.minusDays(40).toInstant());
        final Date endDate = new Date();


        // When
        final List<PullRequestView> pullRequestViews =
                postgresExpositionAdapter.readMergedPullRequestsWithCommitsForTeamIdFromStartDateToEndDate(teamId,
                        startDate,
                        endDate);

        // Then
        assertThat(pullRequestViews).hasSize(2);
        assertThat(pullRequestViews.get(0).getCommits()).hasSize(2);
        assertThat(pullRequestViews.get(1).getCommits()).hasSize(2);
        assertThat(pullRequestViews.get(0).getComments()).hasSize(2);
        assertThat(pullRequestViews.get(1).getComments()).hasSize(1);
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
