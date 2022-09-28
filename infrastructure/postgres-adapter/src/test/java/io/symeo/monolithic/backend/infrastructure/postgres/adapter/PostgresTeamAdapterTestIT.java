package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Onboarding;
import io.symeo.monolithic.backend.domain.model.account.Team;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Repository;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

public class PostgresTeamAdapterTestIT extends AbstractPostgresIT {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamGoalRepository teamGoalRepository;

    @AfterEach
    void tearDown() {
        teamGoalRepository.deleteAll();
        teamRepository.deleteAll();
        userRepository.deleteAll();
        repositoryRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_team_given_existing_repositories_and_organization() throws SymeoException {
        // Given
        final PostgresTeamAdapter postgresAccountTeamAdapter =
                new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID())
                .build();
        User user =
                User.builder().email(faker.dragonBall().character()).onboarding(Onboarding.builder().build()).build();
        user = UserMapper.entityToDomain(userRepository.save(UserMapper.domainToEntity(user)));
        user.hasConfiguredTeam();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories1 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team1 = Team.builder()
                .repositories(repositories1)
                .name(faker.harryPotter().book())
                .organizationId(organizationEntity.getId())
                .build();
        final List<Repository> repositories2 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.dragonBall().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.gameOfThrones().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team2 = Team.builder()
                .repositories(repositories2)
                .name(faker.lordOfTheRings().character())
                .organizationId(organizationEntity.getId())
                .build();


        // When
        final List<Team> createdTeams = postgresAccountTeamAdapter.createTeamsForUser(List.of(team1, team2), user);

        // Then
        assertThat(createdTeams.get(0).getId()).isNotNull();
        assertThat(createdTeams.get(0).getName()).isEqualTo(team1.getName());
        assertThat(createdTeams.get(0).getOrganizationId()).isEqualTo(team1.getOrganizationId());
        assertThat(createdTeams.get(0).getRepositories().size()).isEqualTo(team1.getRepositories().size());
        assertThat(createdTeams.get(1).getId()).isNotNull();
        assertThat(createdTeams.get(1).getName()).isEqualTo(team2.getName());
        assertThat(createdTeams.get(1).getOrganizationId()).isEqualTo(team2.getOrganizationId());
        assertThat(createdTeams.get(1).getRepositories().size()).isEqualTo(team2.getRepositories().size());
        assertThat(teamRepository.findAll()).hasSize(2);
        assertThat(userRepository.findByEmail(user.getEmail()).get().getOnboardingEntity().getHasConfiguredTeam()).isTrue();
    }

    @Test
    void should_find_all_teams_by_organization_id() throws SymeoException {
        // Given
        final PostgresTeamAdapter postgresAccountTeamAdapter =
                new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories1 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team1 = Team.builder()
                .repositories(repositories1)
                .name(faker.harryPotter().book())
                .organizationId(organizationEntity.getId())
                .build();
        final List<Repository> repositories2 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.dragonBall().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.gameOfThrones().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team2 = Team.builder()
                .repositories(repositories2)
                .name(faker.lordOfTheRings().character())
                .organizationId(organizationEntity.getId())
                .build();
        teamRepository.saveAll(List.of(TeamMapper.domainToEntity(team1), TeamMapper.domainToEntity(team2)));


        // When
        final List<Team> teams =
                postgresAccountTeamAdapter.findByOrganization(OrganizationMapper.entityToDomain(organizationEntity));


        // Then
        assertThat(teams.get(0).getId()).isNotNull();
        assertThat(teams.get(0).getName()).isEqualTo(team1.getName());
        assertThat(teams.get(0).getOrganizationId()).isEqualTo(team1.getOrganizationId());
        assertThat(teams.get(0).getRepositories().size()).isEqualTo(team1.getRepositories().size());
        assertThat(teams.get(1).getId()).isNotNull();
        assertThat(teams.get(1).getName()).isEqualTo(team2.getName());
        assertThat(teams.get(1).getOrganizationId()).isEqualTo(team2.getOrganizationId());
        assertThat(teams.get(1).getRepositories().size()).isEqualTo(team2.getRepositories().size());
        assertThat(teamRepository.findAll()).hasSize(2);
    }

    @Test
    void should_raise_an_exception_for_duplicated_team_name_and_organization() {
        // Given
        final PostgresTeamAdapter postgresAccountTeamAdapter =
                new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team = Team.builder()
                .repositories(repositories)
                .name(faker.harryPotter().book())
                .organizationId(organizationEntity.getId())
                .build();
        final User user =
                User.builder().email(faker.dragonBall().character()).onboarding(Onboarding.builder().build()).build();

        // When
        SymeoException symeoException = null;
        try {
            postgresAccountTeamAdapter.createTeamsForUser(List.of(team), user);
            postgresAccountTeamAdapter.createTeamsForUser(List.of(team), user);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(POSTGRES_EXCEPTION);
        assertThat(symeoException.getMessage()).isEqualTo("Failed to create teams " + team.getName());
    }

    @Test
    void should_delete_team_and_linked_team_goals_given_a_team_id() throws SymeoException {
        // Given
        final PostgresTeamAdapter postgresAccountTeamAdapter =
                new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories1 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .repositories(repositories1)
                .name(faker.harryPotter().book())
                .organizationId(organizationEntity.getId())
                .build();
        teamRepository.save(TeamMapper.domainToEntity(team));
        teamGoalRepository.save(TeamGoalEntity.builder()
                .value(faker.pokemon().name())
                .teamId(team.getId())
                .standardCode(TeamStandard.TIME_TO_MERGE)
                .id(UUID.randomUUID())
                .build());

        // When
        postgresAccountTeamAdapter.deleteById(team.getId());

        // Then
        assertThat(teamRepository.findAll()).hasSize(0);
        assertThat(teamGoalRepository.findAll()).hasSize(0);
    }

    @Test
    void should_update_team_given_update_team() throws SymeoException {
        // Given
        final PostgresTeamAdapter postgresAccountTeamAdapter =
                new PostgresTeamAdapter(teamRepository, userRepository, teamGoalRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID())
                .build();
        User user =
                User.builder().email(faker.dragonBall().character()).onboarding(Onboarding.builder().build()).build();
        user = UserMapper.entityToDomain(userRepository.save(UserMapper.domainToEntity(user)));
        user.hasConfiguredTeam();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories1 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team1 = Team.builder()
                .id(UUID.randomUUID())
                .repositories(repositories1)
                .name(faker.harryPotter().book())
                .organizationId(organizationEntity.getId())
                .build();
        teamRepository.save(TeamMapper.domainToEntity(team1));
        final List<Repository> repositories2 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.dragonBall().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.gameOfThrones().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team teamUpdate =
                team1.toBuilder().name(faker.dragonBall().character()).repositories(repositories2).build();

        // When
        postgresAccountTeamAdapter.update(teamUpdate);

        // Then
        final TeamEntity teamEntity = teamRepository.findById(teamUpdate.getId()).get();
        assertThat(teamEntity.getId()).isEqualTo(teamUpdate.getId());
        assertThat(teamEntity.getName()).isEqualTo(teamUpdate.getName());
        assertThat(teamEntity.getOrganizationId()).isEqualTo(teamUpdate.getOrganizationId());
        assertThat(teamEntity.getRepositoryIds()).hasSize(teamUpdate.getRepositories().size());
        teamEntity.getRepositoryIds().forEach(repositoryId -> assertThat(teamUpdate.getRepositories().stream().map(Repository::getId).toList().contains(repositoryId)).isTrue());
    }
}

