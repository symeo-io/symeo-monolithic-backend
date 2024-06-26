package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.*;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.*;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.service.CycleTimeDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;


public class PostgresExpositionAdapterTestIT extends AbstractPostgresIT {

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
    private CustomPullRequestWithCommitsAndCommentsRepository pullRequestWithCommitsAndCommentsRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private CustomCommitRepository customCommitRepository;
    @Autowired
    private VcsOrganizationRepository vcsOrganizationRepository;
    @Autowired
    private CustomCycleTimeRepository customCycleTimeRepository;
    @Autowired
    private CycleTimeRepository cycleTimeRepository;
    private PostgresExpositionAdapter postgresExpositionAdapter;


    @AfterEach
    void tearDown() {
        commitRepository.deleteAll();
        commentRepository.deleteAll();
        teamRepository.deleteAll();
        pullRequestRepository.deleteAll();
        tagRepository.deleteAll();
        repositoryRepository.deleteAll();
        organizationRepository.deleteAll();
        cycleTimeRepository.deleteAll();
    }

    @BeforeEach
    public void setUp() {
        postgresExpositionAdapter = new PostgresExpositionAdapter(pullRequestRepository,
                repositoryRepository, pullRequestTimeToMergeRepository, pullRequestSizeRepository,
                pullRequestFullViewRepository, customPullRequestViewRepository,
                pullRequestWithCommitsAndCommentsRepository, commitRepository, tagRepository, customCommitRepository,
                vcsOrganizationRepository, customCycleTimeRepository, cycleTimeRepository);
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
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedComments(pullRequestsToSave);

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
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedComments(pullRequestsToSave);

        // Then
        final List<PullRequestEntity> pullRequestRepositoryAll = pullRequestRepository.findAll();
        assertThat(pullRequestRepositoryAll.get(0).getCommitShaList()).hasSize(2);
        assertThat(pullRequestRepositoryAll.get(1).getCommitShaList()).hasSize(1);
    }

    @Test
    void should_save_pull_requests_with_comments() {
        final List<PullRequest> pullRequestsToSave = List.of(
                buildPullRequest(1).toBuilder()
                        .comments(List.of(
                                Comment.builder()
                                        .creationDate(new Date())
                                        .id(faker.rickAndMorty().location())
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
                                        .id(faker.harryPotter().character())
                                        .build()
                        )).build()
        );

        // When
        postgresExpositionAdapter.savePullRequestDetailsWithLinkedComments(pullRequestsToSave);

        // Then
        final List<CommentEntity> commentEntities = commentRepository.findAll();
        assertThat(commentEntities).hasSize(3);
    }

    @Test
    void should_save_repositories() {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder().vcsId(faker.pokemon().location())
                        .name(faker.name().name()).build())
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
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
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
        final List<RepositoryView> repositories =
                postgresExpositionAdapter.readRepositoriesForOrganization(organization);

        // Then
        assertThat(repositories).hasSize(3);
    }


    @Test
    void should_read_pr_time_and_pr_size_to_merge_view_given_a_team_id() throws SymeoException {
        // Given
        final Date startDate = stringToDate("2022-08-15");
        final Date endDate = stringToDate("2022-08-25");
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final List<RepositoryEntity> repositoryEntities = repositoryRepository.saveAll(
                List.of(
                        RepositoryEntity.builder().id("1").name(faker.dragonBall().character()).organizationId(organization.getId()).build(),
                        RepositoryEntity.builder().id("2").name(faker.dragonBall().character()).organizationId(organization.getId()).build()
                )
        );
        final String repositoryId = repositoryEntities.get(0).getId();
        pullRequestRepository.saveAll(
                buildPullRequestStubs(organization.getId(), repositoryId, startDate, endDate)
        );
        final TeamEntity teamEntity =
                teamRepository.save(TeamEntity.builder().name(faker.rickAndMorty().character()).organizationId(organization.getId()).id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(repositoryId
                                )).build());

        // When
        final List<PullRequestView> pullRequestTimeViews =
                postgresExpositionAdapter.readPullRequestsTimeToMergeViewForOrganizationAndTeamBetweenStartDateAndEndDate(organization,
                        teamEntity.getId(), startDate, endDate);
        final List<PullRequestView> pullRequestSizeViews =
                postgresExpositionAdapter.readPullRequestsSizeViewForOrganizationAndTeamBetweenStartDateToEndDate(organization,
                        teamEntity.getId(), startDate, endDate);

        // Then
        assertThat(pullRequestTimeViews).hasSize(6);
        assertThat(pullRequestSizeViews).hasSize(6);
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
                        "creation_date", "desc"
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
                .commitShaList(List.of(faker.name().firstName()))
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
                .commitShaList(List.of(faker.ancient().god(), faker.ancient().hero()))
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
        final Date endDate = new Date();


        // When
        final List<PullRequestView> pullRequestViews =
                postgresExpositionAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, endDate);

        // Then
        assertThat(pullRequestViews).hasSize(4);
        for (PullRequestView pullRequestView : pullRequestViews) {
            if (pullRequestView.getId().equals("1")) {
                assertThat(pullRequestView.getComments()).hasSize(2);
                assertThat(pullRequestView.getCommitShaList()).hasSize(1);
            }
            if (pullRequestView.getId().equals("11")) {
                assertThat(pullRequestView.getComments()).hasSize(0);
                assertThat(pullRequestView.getCommitShaList()).hasSize(0);
            }
            if (pullRequestView.getId().equals("2")) {
                assertThat(pullRequestView.getComments()).hasSize(1);
                assertThat(pullRequestView.getCommitShaList()).hasSize(2);
            }
            if (pullRequestView.getId().equals("3")) {
                assertThat(pullRequestView.getComments()).hasSize(0);
                assertThat(pullRequestView.getCommitShaList()).hasSize(0);
            }
        }
    }


    @Test
    void should_find_default_most_used_branch_given_an_organization_id() throws SymeoException {
        // Given
        final Organization organization1 = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        final Organization organization2 = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        final String defaultBranch1 = faker.rickAndMorty().character();
        final String defaultBranch2 = faker.rickAndMorty().location();
        final List<RepositoryEntity> repositoryEntities = List.of(
                RepositoryEntity.builder().id("1L").defaultBranch(defaultBranch1).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization1.getId()).build(),
                RepositoryEntity.builder().id("2L").defaultBranch(defaultBranch1).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization1.getId()).build(),
                RepositoryEntity.builder().id("3L").defaultBranch(defaultBranch2).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization1.getId()).build(),

                RepositoryEntity.builder().id("4L").defaultBranch(defaultBranch1).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization2.getId()).build(),
                RepositoryEntity.builder().id("5L").defaultBranch(defaultBranch2).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization2.getId()).build(),
                RepositoryEntity.builder().id("6L").defaultBranch(defaultBranch2).name(faker.gameOfThrones().character())
                        .vcsOrganizationName(faker.name().firstName()).organizationId(organization2.getId()).build()
        );
        repositoryRepository.saveAll(repositoryEntities);

        // When
        final String branch1 =
                postgresExpositionAdapter.findDefaultMostUsedBranchForOrganizationId(organization1.getId());
        final String branch2 =
                postgresExpositionAdapter.findDefaultMostUsedBranchForOrganizationId(organization2.getId());

        // Then
        assertThat(branch1).isEqualTo(defaultBranch1);
        assertThat(branch2).isEqualTo(defaultBranch2);
    }

    @Test
    void should_find_repositories_given_a_team_id_and_an_organization_id() throws SymeoException {
        // Given
        final String vcsOrganizationName = faker.name().name();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.animal().name())
                .vcsOrganization(Organization.VcsOrganization.builder().name(vcsOrganizationName).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final List<RepositoryEntity> repositoryEntities = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().id(faker.dragonBall().character())
                        .organizationId(organization.getId())
                        .name(faker.ancient().god() + "-1")
                        .defaultBranch(faker.ancient().hero())
                        .vcsOrganizationName(vcsOrganizationName)
                        .build(),
                RepositoryEntity.builder().id(faker.rickAndMorty().character())
                        .name(faker.ancient().god() + "-2")
                        .defaultBranch(faker.ancient().hero())
                        .organizationId(organization.getId())
                        .vcsOrganizationName(vcsOrganizationName)
                        .build()
        ));
        final TeamEntity teamEntity = teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organization.getId())
                        .name(faker.name().firstName())
                        .id(UUID.randomUUID())
                        .repositoryIds(
                                repositoryEntities.stream().map(RepositoryEntity::getId).toList()
                        )
                        .build());

        // When
        final List<RepositoryView> allRepositoriesForOrganizationIdAndTeamId =
                postgresExpositionAdapter.findAllRepositoriesForOrganizationIdAndTeamId(organization.getId(),
                        teamEntity.getId());

        // Then
        final List<RepositoryView> expectedRepositories =
                repositoryEntities.stream().map(RepositoryMapper::entityToDomain).toList();
        allRepositoriesForOrganizationIdAndTeamId.forEach(repository ->
                assertThat(expectedRepositories.stream()
                        .anyMatch(expectedRepository -> expectedRepository.equals(repository)))
                        .isTrue()
        );
    }

    @Test
    void should_find_repositories_given_an_organization_id() throws SymeoException {
        // Given
        final String vcsOrganizationName = faker.name().name();
        final String vcsOrganizationId = faker.pokemon().location();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.animal().name())
                .vcsOrganization(Organization.VcsOrganization.builder()
                        .name(vcsOrganizationName)
                        .vcsId(vcsOrganizationId).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final List<RepositoryEntity> repositoryEntities = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().id(faker.dragonBall().character())
                        .organizationId(organization.getId())
                        .name(faker.ancient().god() + "-1")
                        .defaultBranch(faker.ancient().hero())
                        .vcsOrganizationName(vcsOrganizationName)
                        .vcsOrganizationId(vcsOrganizationId)
                        .build(),
                RepositoryEntity.builder().id(faker.rickAndMorty().character())
                        .name(faker.ancient().god() + "-2")
                        .defaultBranch(faker.ancient().hero())
                        .organizationId(organization.getId())
                        .vcsOrganizationName(vcsOrganizationName)
                        .vcsOrganizationId(vcsOrganizationId)
                        .build()
        ));
        teamRepository.save(
                TeamEntity.builder()
                        .organizationId(organization.getId())
                        .name(faker.name().firstName())
                        .id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(repositoryEntities.get(0).getId())
                        )
                        .build());

        // When
        final List<Repository> allRepositoriesForOrganizationIdAndTeamId =
                postgresExpositionAdapter.findAllRepositoriesLinkedToTeamsForOrganizationId(organization.getId());

        // Then
        assertThat(allRepositoriesForOrganizationIdAndTeamId).hasSize(1);
        assertThat(allRepositoriesForOrganizationIdAndTeamId.get(0).getId()).isEqualTo(repositoryEntities.get(0).getId());
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
                Organization.builder().id(UUID.randomUUID()).vcsOrganization(Organization.VcsOrganization.builder().vcsId(faker.rickAndMorty().character()).name(faker.ancient().god()).build()).build());
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


    private List<PullRequestEntity> buildPullRequestStubs(final UUID organizationId, final String repositoryId,
                                                          final Date startDate,
                                                          final Date endDate) {
        final PullRequestEntity pr1 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-1")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr2 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-2")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr3 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-3")
                .organizationId(organizationId)
                .creationDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr4 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-4")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(3))
                .mergeDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(1))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr5 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-5")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .mergeDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr6 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-6")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .mergeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(3))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr7 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-7")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr8 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-8")
                .organizationId(organizationId)
                .creationDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(2))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr9 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-9")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(1))
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr10 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-10")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(3))
                .closeDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(1))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr11 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-11")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .closeDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr12 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-12")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .closeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(3))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr13 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-13")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .closeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr14 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-14")
                .organizationId(organizationId)
                .creationDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .closeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(2))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr15 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-15")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .closeDate(endDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(1))
                .mergeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr16 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-16")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .isDraft(true)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .build();
        final PullRequestEntity pr17 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-17")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(null)
                .closeDate(null)
                .isDraft(true)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .build();
        final PullRequestEntity pr18 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-18")
                .organizationId(organizationId)
                .creationDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .isDraft(true)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .build();
        final PullRequestEntity pr19 = PullRequestEntity.builder()
                .code(faker.harryPotter().book())
                .id(faker.rickAndMorty().character() + "-19")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(3))
                .mergeDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(1))
                .closeDate(null)
                .isDraft(true)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .build();
        return List.of(pr1, pr2, pr3, pr4, pr5, pr6, pr7, pr8, pr9, pr10, pr11, pr12, pr13, pr14, pr15, pr16, pr17,
                pr18, pr19);
    }


    @Test
    void should_save_tags() throws SymeoException {
        // Given
        final String repositoryId = faker.rickAndMorty().character();
        final String repositoryName = faker.dragonBall().character();
        repositoryRepository.saveAll(
                List.of(RepositoryEntity.builder().id(repositoryId).name(repositoryName).build())
        );
        final List<Tag> tags = List.of(
                Tag.builder().commitSha(faker.name().firstName())
                        .repositoryId(repositoryId)
                        .name(faker.name().firstName())
                        .build(),
                Tag.builder().commitSha(faker.name().lastName())
                        .repositoryId(repositoryId)
                        .name(faker.name().firstName())
                        .build());

        // When
        postgresExpositionAdapter.saveTags(tags);

        // Then
        final List<TagEntity> tagList = tagRepository.findAll();
        assertThat(tagList).hasSize(tags.size());
    }

    @Test
    void should_find_tags_given_a_team_id() throws SymeoException {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final String fakeRepositoryName = faker.dragonBall().character();
        final List<RepositoryEntity> repositoryEntities = repositoryRepository.saveAll(
                List.of(
                        RepositoryEntity.builder().id("1").name(fakeRepositoryName + "-1").organizationId(organization.getId()).build(),
                        RepositoryEntity.builder().id("2").name(fakeRepositoryName + "-2").organizationId(organization.getId()).build()
                )
        );
        final String repositoryId = repositoryEntities.get(0).getId();
        final TeamEntity teamEntity =
                teamRepository.save(TeamEntity.builder().name(faker.rickAndMorty().character()).organizationId(organization.getId()).id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(repositoryId
                                )).build());
        teamRepository.save(teamEntity);
        tagRepository.saveAll(
                List.of(
                        TagEntity.builder()
                                .sha(faker.rickAndMorty().location() + "-1")
                                .repositoryId(repositoryId)
                                .name(faker.funnyName().name())
                                .build(),
                        TagEntity.builder()
                                .sha(faker.rickAndMorty().character() + "-2")
                                .repositoryId(repositoryId)
                                .name(faker.funnyName().name())
                                .build(),
                        TagEntity.builder()
                                .sha(faker.rickAndMorty().character() + "-3")
                                .repositoryId("2")
                                .name(faker.funnyName().name())
                                .build()
                )
        );

        // When
        final List<TagView> tagsForTeamId = postgresExpositionAdapter.findTagsForTeamId(teamEntity.getId());

        // Then
        assertThat(tagsForTeamId).hasSize(2);
        assertThat(tagsForTeamId.get(0).getRepository().getName()).isEqualTo(fakeRepositoryName + "-1");
        assertThat(tagsForTeamId.get(1).getRepository().getName()).isEqualTo(fakeRepositoryName + "-1");
    }


    @Test
    void should_read_all_commits_given_a_team_id() throws SymeoException {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name(faker.name().firstName())
                .vcsOrganization(Organization.VcsOrganization.builder().name(faker.name().name()).build())
                .build();
        organizationRepository.save(OrganizationMapper.domainToEntity(organization));
        final RepositoryEntity repositoryEntity = repositoryRepository.save(
                RepositoryEntity.builder().id("1").name(faker.dragonBall().character()).organizationId(organization.getId()).build()
        );
        final TeamEntity teamEntity =
                teamRepository.save(TeamEntity.builder().name(faker.rickAndMorty().character()).organizationId(organization.getId()).id(UUID.randomUUID())
                        .repositoryIds(
                                List.of(repositoryEntity.getId()
                                )).build());
        teamRepository.save(teamEntity);
        final List<CommitEntity> commitEntities = List.of(
                CommitEntity.builder()
                        .message(faker.business().creditCardExpiry())
                        .sha(UUID.randomUUID() + faker.ancient().god())
                        .date(ZonedDateTime.now())
                        .authorLogin(faker.dragonBall().character())
                        .parentShaList(List.of(
                                faker.name().firstName(),
                                faker.name().lastName(),
                                faker.name().name()
                        ))
                        .repositoryId(repositoryEntity.getId())
                        .build(),
                CommitEntity.builder()
                        .message(faker.business().creditCardExpiry())
                        .sha(UUID.randomUUID() + faker.ancient().god())
                        .date(ZonedDateTime.now())
                        .authorLogin(faker.dragonBall().character())
                        .parentShaList(List.of(
                                faker.name().firstName(),
                                faker.name().lastName(),
                                faker.name().name()
                        ))
                        .repositoryId(repositoryEntity.getId())
                        .build(),
                CommitEntity.builder()
                        .message(faker.business().creditCardExpiry())
                        .sha(UUID.randomUUID() + faker.ancient().god())
                        .date(ZonedDateTime.now())
                        .authorLogin(faker.dragonBall().character())
                        .repositoryId(repositoryEntity.getId())
                        .build()
        );
        commitRepository.saveAll(
                commitEntities
        );

        // When
        final List<CommitView> commits = postgresExpositionAdapter.readAllCommitsForTeamId(teamEntity.getId());

        // Then
        assertThat(commits).hasSize(3);

        for (CommitEntity commitEntity : commitEntities) {
            final Optional<CommitView> optionalCommit =
                    commits.stream().filter(commit -> commit.getSha().equals(commitEntity.getSha())).findFirst();
            optionalCommit
                    .ifPresentOrElse(commit -> {
                                assertThat(commit.getParentShaList()).hasSize(commitEntity.getParentShaList().size());
                                assertThat(commit.getSha()).isEqualTo(commitEntity.getSha());
                                assertThat(commit.getMessage()).isEqualTo(commitEntity.getMessage());
                                assertThat(commit.getRepositoryId()).isEqualTo(commitEntity.getRepositoryId());
                                assertThat(commit.getAuthor()).isEqualTo(commitEntity.getAuthorLogin());
                            },
                            () -> assertThat(true).isFalse());
        }
    }

    @Test
    void should_find_vcs_organization_given_an_organization_id_and_a_vcs_organization_id() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.lordOfTheRings().character())
                .id(organizationId)
                .build();
        final VcsOrganizationEntity vcsOrganizationEntity = VcsOrganizationEntity.builder()
                .vcsId(faker.pokemon().location())
                .organizationEntity(organizationEntity)
                .name(faker.rickAndMorty().character())
                .externalId(faker.ancient().god())
                .build();

        // When
        vcsOrganizationRepository.save(vcsOrganizationEntity);
        final Optional<VcsOrganization> emptyVcsOrganizationByIdAndOrganizationId =
                postgresExpositionAdapter.findVcsOrganizationByIdAndOrganizationId(
                        faker.number().randomNumber() + 100L, organizationId
                );
        final Optional<VcsOrganization> vcsOrganizationByIdAndOrganizationId =
                postgresExpositionAdapter.findVcsOrganizationByIdAndOrganizationId(
                        vcsOrganizationEntity.getId(), organizationId
                );

        // Then
        assertThat(emptyVcsOrganizationByIdAndOrganizationId).isEmpty();
        assertThat(vcsOrganizationByIdAndOrganizationId).isPresent();
        final VcsOrganization vcsOrganization = vcsOrganizationByIdAndOrganizationId.get();
        assertThat(vcsOrganization.getId()).isEqualTo(vcsOrganizationEntity.getId());
        assertThat(vcsOrganization.getOrganizationId()).isEqualTo(vcsOrganizationEntity.getOrganizationEntity().getId());
        assertThat(vcsOrganization.getVcsId()).isEqualTo(vcsOrganizationEntity.getVcsId());
        assertThat(vcsOrganization.getName()).isEqualTo(vcsOrganizationEntity.getName());
    }


    @Test
    void should_find_all_repositories_in_repository_ids() throws SymeoException {
        // Given
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .vcsOrganization(Organization.VcsOrganization.builder()
                        .vcsId(faker.pokemon().location())
                        .name(faker.name().name()).build())
                .build();
        final Repository repo1 = buildRepository(organization);
        final Repository repo2 = buildRepository(organization);
        final Repository repo3 = buildRepository(organization);
        postgresExpositionAdapter.saveRepositories(
                List.of(
                        repo1,
                        repo2,
                        repo3
                )
        );
        final List<String> repositoryIds = List.of(repo1.getId(), repo3.getId());

        // When
        final List<Repository> allRepositoriesByIds =
                postgresExpositionAdapter.findAllRepositoriesByIds(repositoryIds);

        // Then
        assertThat(allRepositoriesByIds).hasSize(repositoryIds.size());
        assertThat(allRepositoriesByIds).contains(repo1, repo3);
    }

    @Test
    void should_find_all_pull_requests_given_an_repository_id() throws SymeoException {
        // Given
        final String repositoryId = faker.rickAndMorty().character();
        final UUID organizationId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-01-15");

        final PullRequestEntity pr1 = PullRequestEntity.builder()
                .code(String.valueOf(faker.number().numberBetween(1, 100)))
                .id(faker.rickAndMorty().character() + "-1")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).minusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr2 = PullRequestEntity.builder()
                .code(String.valueOf(faker.number().numberBetween(1, 100)))
                .id(faker.rickAndMorty().character() + "-2")
                .organizationId(organizationId)
                .creationDate(startDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(1))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(faker.dragonBall().character())
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();
        final PullRequestEntity pr3 = PullRequestEntity.builder()
                .code(String.valueOf(faker.number().numberBetween(1, 100)))
                .id(faker.rickAndMorty().character() + "-3")
                .organizationId(organizationId)
                .creationDate(endDate.toInstant().atZone(ZoneId.systemDefault()).plusDays(2))
                .mergeDate(null)
                .closeDate(null)
                .vcsRepositoryId(repositoryId)
                .authorLogin(faker.dragonBall().character())
                .title(faker.gameOfThrones().character())
                .lastUpdateDate(ZonedDateTime.now())
                .isDraft(false)
                .build();

        pullRequestRepository.saveAll(List.of(pr1, pr2, pr3));

        // When
        final List<PullRequest> pullRequests = postgresExpositionAdapter.findAllPullRequestsForRepositoryId(repositoryId);

        // Then
        assertThat(pullRequests).hasSize(2);
        assertThat(pullRequests.get(0).getId()).isEqualTo(pr1.getId());
        assertThat(pullRequests.get(1).getId()).isEqualTo(pr3.getId());
    }

    @Test
    void should_replace_cycle_times_for_repository_id() throws SymeoException, InterruptedException {
        // Given
        final String repositoryId = faker.rickAndMorty().character();
        final String repositoryName = faker.gameOfThrones().character();
        final UUID vcsOrganizationId = UUID.randomUUID();
        final CycleTimeEntity cycleTime1 = CycleTimeEntity.builder()
                .id(faker.dog().name() + "-1")
                .value(100L)
                .codingTime(100L)
                .reviewTime(100L)
                .timeToDeploy(100L)
                .deployDate(stringToDate("2022-01-10"))
                .pullRequestId(faker.dog().name() + "-1")
                .pullRequestState("merge")
                .pullRequestAuthorLogin(faker.harryPotter().character() + "-1")
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(repositoryName)
                .pullRequestVcsUrl(faker.pokemon().location())
                .pullRequestTitle(faker.animal().name())
                .pullRequestCreationDate(stringToDate("2022-01-01"))
                .pullRequestMergeDate(stringToDate("2022-01-10"))
                .pullRequestHead(faker.ancient().god())
                .pullRequestUpdateDate(stringToDate("2022-01-10"))
                .build();
        final CycleTimeEntity cycleTime2 = CycleTimeEntity.builder()
                .id(faker.dog().name() + "-2")
                .value(200L)
                .codingTime(200L)
                .reviewTime(200L)
                .timeToDeploy(200L)
                .deployDate(stringToDate("2022-01-15"))
                .pullRequestId(faker.dog().name() + "-2")
                .pullRequestState("merge")
                .pullRequestAuthorLogin(faker.harryPotter().character() + "-2")
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(repositoryName)
                .pullRequestVcsUrl(faker.pokemon().location())
                .pullRequestTitle(faker.animal().name())
                .pullRequestCreationDate(stringToDate("2022-01-05"))
                .pullRequestMergeDate(stringToDate("2022-01-15"))
                .pullRequestHead(faker.ancient().god())
                .pullRequestUpdateDate(stringToDate("2022-01-15"))
                .build();
        final CycleTimeEntity cycleTime3 = CycleTimeEntity.builder()
                .id(faker.dog().name() + "-3")
                .value(300L)
                .codingTime(300L)
                .reviewTime(300L)
                .timeToDeploy(300L)
                .deployDate(stringToDate("2022-01-20"))
                .pullRequestId(faker.dog().name() + "-3")
                .pullRequestState("merge")
                .pullRequestAuthorLogin(faker.harryPotter().character() + "-3")
                .pullRequestVcsRepositoryId(repositoryId)
                .pullRequestVcsRepository(repositoryName)
                .pullRequestVcsUrl(faker.pokemon().location())
                .pullRequestTitle(faker.animal().name())
                .pullRequestCreationDate(stringToDate("2022-01-10"))
                .pullRequestMergeDate(stringToDate("2022-01-20"))
                .pullRequestHead(faker.ancient().god())
                .pullRequestUpdateDate(stringToDate("2022-01-20"))
                .build();
        cycleTimeRepository.saveAll(List.of(cycleTime1, cycleTime2, cycleTime3));

        final String updatedCycleTimeId1 = faker.dog().name() + "-4";
        final String updatedCycleTimeId2 = faker.dog().name() + "-5";
        final String updatedCycleTimeId3 = faker.dog().name() + "-6";
        final CycleTime updatedCycleTime1 = CycleTime.builder()
                .id(updatedCycleTimeId1)
                .value(400L)
                .codingTime(400L)
                .reviewTime(400L)
                .timeToDeploy(400L)
                .deployDate(stringToDate("2022-01-10 10:00:00"))
                .pullRequest(
                        PullRequest.builder()
                                .id(updatedCycleTimeId1)
                                .isDraft(false)
                                .commitNumber(faker.number().numberBetween(1, 10))
                                .addedLineNumber(faker.number().numberBetween(1, 10))
                                .deletedLineNumber(faker.number().numberBetween(1, 10))
                                .vcsOrganizationId(vcsOrganizationId.toString())
                                .organizationId(vcsOrganizationId)
                                .base(faker.ancient().god())
                                .authorLogin(faker.harryPotter().character() + "-4")
                                .number(faker.number().numberBetween(1, 100))
                                .mergeCommitSha(faker.ancient().primordial())
                                .commitShaList(List.of())
                                .comments(List.of())
                                .repositoryId(repositoryId)
                                .repository(repositoryName)
                                .vcsUrl(faker.pokemon().location())
                                .title(faker.animal().name())
                                .creationDate(stringToDate("2022-01-01"))
                                .mergeDate(stringToDate("2022-01-10"))
                                .head(faker.ancient().god())
                                .lastUpdateDate(stringToDate("2022-01-10"))
                                .build()
                )
                .build();
        final CycleTime updatedCycleTime2 = CycleTime.builder()
                .id(updatedCycleTimeId2)
                .value(500L)
                .codingTime(500L)
                .reviewTime(500L)
                .timeToDeploy(500L)
                .deployDate(stringToDate("2022-01-15 10:00:00"))
                .pullRequest(
                        PullRequest.builder()
                                .id(updatedCycleTimeId2)
                                .isDraft(false)
                                .commitNumber(faker.number().numberBetween(1, 10))
                                .addedLineNumber(faker.number().numberBetween(1, 10))
                                .deletedLineNumber(faker.number().numberBetween(1, 10))
                                .vcsOrganizationId(vcsOrganizationId.toString())
                                .organizationId(vcsOrganizationId)
                                .base(faker.ancient().god())
                                .authorLogin(faker.harryPotter().character() + "-5")
                                .number(faker.number().numberBetween(1, 100))
                                .mergeCommitSha(faker.ancient().primordial())
                                .commitShaList(List.of())
                                .comments(List.of())
                                .repositoryId(repositoryId)
                                .repository(repositoryName)
                                .vcsUrl(faker.pokemon().location())
                                .title(faker.animal().name())
                                .creationDate(stringToDate("2022-01-05"))
                                .mergeDate(stringToDate("2022-01-15"))
                                .head(faker.ancient().god())
                                .lastUpdateDate(stringToDate("2022-01-15"))
                                .build()
                )
                .build();
        final CycleTime updatedCycleTime3 = CycleTime.builder()
                .id(updatedCycleTimeId3)
                .value(600L)
                .codingTime(600L)
                .reviewTime(600L)
                .timeToDeploy(600L)
                .deployDate(stringToDate("2022-01-20 10:00:00"))
                .pullRequest(
                        PullRequest.builder()
                                .id(updatedCycleTimeId3)
                                .isDraft(false)
                                .commitNumber(faker.number().numberBetween(1, 10))
                                .addedLineNumber(faker.number().numberBetween(1, 10))
                                .deletedLineNumber(faker.number().numberBetween(1, 10))
                                .vcsOrganizationId(vcsOrganizationId.toString())
                                .organizationId(vcsOrganizationId)
                                .base(faker.ancient().god())
                                .authorLogin(faker.harryPotter().character() + "-6")
                                .number(faker.number().numberBetween(1, 100))
                                .mergeCommitSha(faker.ancient().primordial())
                                .commitShaList(List.of())
                                .comments(List.of())
                                .repositoryId(repositoryId)
                                .repository(repositoryName)
                                .vcsUrl(faker.pokemon().location())
                                .title(faker.animal().name())
                                .creationDate(stringToDate("2022-01-10"))
                                .mergeDate(stringToDate("2022-01-20"))
                                .head(faker.ancient().god())
                                .lastUpdateDate(stringToDate("2022-01-20"))
                                .build()
                )
                .build();

        // When
        postgresExpositionAdapter.replaceCycleTimesForRepositoryId(
                repositoryId,
                List.of(updatedCycleTime1, updatedCycleTime2, updatedCycleTime3)
        );
        List<CycleTimeEntity> updatedCycleTimeEntities = cycleTimeRepository.findAll();
        // Then
        assertThat(updatedCycleTimeEntities).hasSize(3);
        assertThat(updatedCycleTimeEntities.stream().map(CycleTimeEntity::getId))
                .contains(
                        updatedCycleTime1.getPullRequest().getId(),
                        updatedCycleTime2.getPullRequest().getId(),
                        updatedCycleTime3.getPullRequest().getId()
                );
    }
}
