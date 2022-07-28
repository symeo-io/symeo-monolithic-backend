package fr.catlean.monolithic.backend.infrastructure.postgres.adapter;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.infrastructure.postgres.PostgresTeamGoalAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.SetupConfiguration;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
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

    @Test
    void should_create_team_goal() throws CatleanException {
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

        // When
        postgresTeamGoalAdapter.saveTeamGoal(
                TeamGoal.fromTeamStandardAndTeamId(
                        TeamStandard.builder()
                                .code(standardCode)
                                .build(), teamEntity.getId()));

        // Then
        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();
        assertThat(teamGoalEntities).hasSize(1);
        assertThat(teamGoalEntities.get(0).getId()).isNotNull();
        assertThat(teamGoalEntities.get(0).getStandardCode()).isEqualTo(standardCode);
        assertThat(teamGoalEntities.get(0).getTeamEntity()).isEqualTo(teamEntity);
    }
}
