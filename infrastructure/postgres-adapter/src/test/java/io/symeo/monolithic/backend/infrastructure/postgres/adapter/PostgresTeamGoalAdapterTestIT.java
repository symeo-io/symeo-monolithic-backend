package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.infrastructure.postgres.PostgresTeamGoalAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
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
public class PostgresTeamGoalAdapterTestIT {

    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public TeamGoalRepository teamGoalRepository;
    private final Faker faker = new Faker();

    @AfterEach
    public void setUp() {
        teamGoalRepository.deleteAll();
        teamRepository.deleteAll();
        organizationRepository.deleteAll();
    }

    @Test
    void should_create_team_goal() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.pokemon().name())
                .build();
        organizationRepository.save(organizationEntity);
        final TeamEntity teamEntity = TeamEntity.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .build();
        teamRepository.save(teamEntity);
        final PostgresTeamGoalAdapter postgresTeamGoalAdapter = new PostgresTeamGoalAdapter(teamRepository,
                teamGoalRepository);
        final String standardCode = faker.rickAndMorty().character();
        final int value = faker.number().randomDigit();

        // When
        postgresTeamGoalAdapter.saveTeamGoal(
                TeamGoal.fromTeamStandardAndTeamId(
                        TeamStandard.builder()
                                .code(standardCode)
                                .build(), teamEntity.getId(), value));

        // Then
        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();
        assertThat(teamGoalEntities).hasSize(1);
        assertThat(teamGoalEntities.get(0).getId()).isNotNull();
        assertThat(teamGoalEntities.get(0).getStandardCode()).isEqualTo(standardCode);
        assertThat(teamGoalEntities.get(0).getTeamId()).isEqualTo(teamEntity.getId());
        assertThat(teamGoalEntities.get(0).getValue()).isEqualTo(Integer.toString(value));
    }

    @Test
    void should_read_all_team_goals_given_a_team_id() throws SymeoException {
        final UUID organizationId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.pokemon().name())
                .build();
        organizationRepository.save(organizationEntity);
        final TeamEntity teamEntity1 = TeamEntity.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .build();
        final TeamEntity teamEntity2 = TeamEntity.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .build();
        teamRepository.save(teamEntity1);
        teamRepository.save(teamEntity2);
        teamGoalRepository.saveAll(
                List.of(
                        TeamGoalEntity.builder().teamId(teamEntity1.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build(),
                        TeamGoalEntity.builder().teamId(teamEntity1.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build(),
                        TeamGoalEntity.builder().teamId(teamEntity2.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build()
                )
        );
        final PostgresTeamGoalAdapter postgresTeamGoalAdapter = new PostgresTeamGoalAdapter(teamRepository,
                teamGoalRepository);

        // When
        final List<TeamGoal> teamGoals1 = postgresTeamGoalAdapter.readForTeamId(teamEntity1.getId());
        final List<TeamGoal> teamGoals2 = postgresTeamGoalAdapter.readForTeamId(teamEntity2.getId());

        // Then
        assertThat(teamGoals1).hasSize(2);
        teamGoals1.forEach(teamGoal -> assertThat(teamGoal.getTeamId()).isEqualTo(teamEntity1.getId()));
        assertThat(teamGoals2).hasSize(1);
        teamGoals2.forEach(teamGoal -> assertThat(teamGoal.getTeamId()).isEqualTo(teamEntity2.getId()));
    }

    @Test
    void should_delete_team_goal_given_an_id() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.pokemon().name())
                .build();
        organizationRepository.save(organizationEntity);
        final TeamEntity teamEntity1 = TeamEntity.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .build();
        teamRepository.save(teamEntity1);
        final TeamGoalEntity teamGoalEntityToDelete =
                TeamGoalEntity.builder().teamId(teamEntity1.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build();
        final TeamGoalEntity teamGoalToKeep =
                TeamGoalEntity.builder().teamId(teamEntity1.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build();
        teamGoalRepository.saveAll(
                List.of(
                        teamGoalEntityToDelete,
                        teamGoalToKeep
                )
        );
        final PostgresTeamGoalAdapter postgresTeamGoalAdapter = new PostgresTeamGoalAdapter(teamRepository,
                teamGoalRepository);

        // When
        postgresTeamGoalAdapter.deleteForId(teamGoalEntityToDelete.getId());

        // Then
        final List<TeamGoalEntity> all = teamGoalRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0)).isEqualTo(teamGoalToKeep);
    }

    @Test
    void should_update_team_goal_value_given_an_id() throws SymeoException {
        // Given
        final UUID organizationId = UUID.randomUUID();
        final OrganizationEntity organizationEntity = OrganizationEntity.builder()
                .id(organizationId)
                .name(faker.pokemon().name())
                .build();
        organizationRepository.save(organizationEntity);
        final TeamEntity teamEntity = TeamEntity.builder()
                .name(faker.name().firstName())
                .id(UUID.randomUUID())
                .organizationId(organizationId)
                .build();
        teamRepository.save(teamEntity);
        final TeamGoalEntity teamGoalEntity =
                TeamGoalEntity.builder().teamId(teamEntity.getId()).value(faker.pokemon().name()).standardCode(faker.dragonBall().character()).id(UUID.randomUUID()).build();
        teamGoalRepository.save(teamGoalEntity);
        final PostgresTeamGoalAdapter postgresTeamGoalAdapter = new PostgresTeamGoalAdapter(teamRepository,
                teamGoalRepository);
        final int newValue = faker.number().randomDigit();


        // When
        postgresTeamGoalAdapter.updateForIdAndValue(teamGoalEntity.getId(), newValue);

        // Then
        final TeamGoalEntity teamGoalEntityUpdated = teamGoalRepository.findAll().get(0);
        assertThat(teamGoalEntityUpdated.getId()).isEqualTo(teamGoalEntity.getId());
        assertThat(teamGoalEntityUpdated.getTeamId()).isEqualTo(teamGoalEntity.getTeamId());
        assertThat(teamGoalEntityUpdated.getValue()).isEqualTo(Integer.toString(newValue));
        assertThat(teamGoalEntityUpdated.getStandardCode()).isEqualTo(teamGoalEntity.getStandardCode());
    }
}
