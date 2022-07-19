package fr.catlean.monolithic.backend.infrastructure.postgres.it.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.Repository;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.PostgresTeamAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.exposition.RepositoryEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.it.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.exposition.RepositoryMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = SetupConfiguration.class)
public class PostgresTeamAdapterTestIT {

    private final Faker faker = new Faker();
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private RepositoryRepository repositoryRepository;

    @AfterEach
    void tearDown() {
        teamRepository.deleteAll();
    }

    @Test
    void should_create_team_given_existing_repositories_and_organization() throws CatleanException {
        // Given
        final PostgresTeamAdapter postgresTeamAdapter =
                new PostgresTeamAdapter(teamRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .externalId(faker.dragonBall().character())
                .name(faker.pokemon().name())
                .id(UUID.randomUUID().toString())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).vcsId(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).vcsId(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team = Team.builder()
                .repositories(repositories)
                .name(faker.harryPotter().book())
                .organizationId(UUID.fromString(organizationEntity.getId()))
                .build();

        // When
        final Team createdTeam = postgresTeamAdapter.createTeam(team);

        // Then
        assertThat(createdTeam.getId()).isNotNull();
        assertThat(createdTeam.getName()).isEqualTo(team.getName());
        assertThat(createdTeam.getOrganizationId()).isEqualTo(team.getOrganizationId());
        assertThat(createdTeam.getRepositories().size()).isEqualTo(team.getRepositories().size());
        assertThat(teamRepository.findAll()).hasSize(1);
    }

    @Test
    void should_raise_an_exception_for_duplicated_team_name_and_organization() {
        // Given
        final PostgresTeamAdapter postgresTeamAdapter =
                new PostgresTeamAdapter(teamRepository);
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .externalId(faker.dragonBall().character())
                .name(faker.pokemon().name())
                .id(UUID.randomUUID().toString())
                .build();
        organizationRepository.save(organizationEntity);
        final List<Repository> repositories = repositoryRepository.saveAll(List.of(
                RepositoryEntity.builder().name(faker.name().firstName()).vcsId(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build(),
                RepositoryEntity.builder().name(faker.name().lastName()).vcsId(faker.rickAndMorty().character()).vcsOrganizationName(faker.gameOfThrones().character()).organizationId(organizationEntity.getId()).build()
        )).stream().map(RepositoryMapper::entityToDomain).toList();
        final Team team = Team.builder()
                .repositories(repositories)
                .name(faker.harryPotter().book())
                .organizationId(UUID.fromString(organizationEntity.getId()))
                .build();

        // When
        CatleanException catleanException = null;
        try {
            postgresTeamAdapter.createTeam(team);
            postgresTeamAdapter.createTeam(team);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
        assertThat(catleanException.getCode()).isEqualTo("T.POSTGRES_EXCEPTION");
        assertThat(catleanException.getMessage()).isEqualTo("Failed to create team " + team.getName());
    }
}

