package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Onboarding;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresAccountTeamAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.exposition.RepositoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresAccountTeamAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;
    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        teamRepository.deleteAll();
        userRepository.deleteAll();
        repositoryRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_team_given_existing_repositories_and_organization() throws CatleanException {
        // Given
        final PostgresAccountTeamAdapter postgresAccountTeamAdapter =
                new PostgresAccountTeamAdapter(teamRepository, userRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID().toString())
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
                .organizationId(UUID.fromString(organizationEntity.getId()))
                .build();
        final List<Repository> repositories2 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.dragonBall().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.gameOfThrones().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team2 = Team.builder()
                .repositories(repositories2)
                .name(faker.lordOfTheRings().character())
                .organizationId(UUID.fromString(organizationEntity.getId()))
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
    void should_find_all_teams_by_organization_id() throws CatleanException {
        // Given
        final PostgresAccountTeamAdapter postgresAccountTeamAdapter =
                new PostgresAccountTeamAdapter(teamRepository, userRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID().toString())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories1 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team1 = Team.builder()
                .repositories(repositories1)
                .name(faker.harryPotter().book())
                .organizationId(UUID.fromString(organizationEntity.getId()))
                .build();
        final List<Repository> repositories2 = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.dragonBall().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.gameOfThrones().character()).vcsOrganizationName(faker.dragonBall().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team2 = Team.builder()
                .repositories(repositories2)
                .name(faker.lordOfTheRings().character())
                .organizationId(UUID.fromString(organizationEntity.getId()))
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
        final PostgresAccountTeamAdapter postgresAccountTeamAdapter =
                new PostgresAccountTeamAdapter(teamRepository, userRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .name(faker.pokemon().name())
                .id(UUID.randomUUID().toString())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).id(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team = Team.builder()
                .repositories(repositories)
                .name(faker.harryPotter().book())
                .organizationId(UUID.fromString(organizationEntity.getId()))
                .build();
        final User user =
                User.builder().email(faker.dragonBall().character()).onboarding(Onboarding.builder().build()).build();

        // When
        CatleanException catleanException = null;
        try {
            postgresAccountTeamAdapter.createTeamsForUser(List.of(team), user);
            postgresAccountTeamAdapter.createTeamsForUser(List.of(team), user);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
        assertThat(catleanException.getCode()).isEqualTo(POSTGRES_EXCEPTION);
        assertThat(catleanException.getMessage()).isEqualTo("Failed to create teams " + team.getName());
    }
}

