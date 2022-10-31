package io.symeo.monolithic.backend.bootstrap.it.bff;

import io.symeo.monolithic.backend.domain.bff.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.bff.contract.api.model.PatchTeamGoalsRequest;
import io.symeo.monolithic.backend.bff.contract.api.model.PostCreateTeamGoalsRequest;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.*;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SymeoTeamGoalsApiIT extends AbstractSymeoBackForFrontendApiIT {

    @Autowired
    public OrganizationRepository organizationRepository;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public TeamRepository teamRepository;
    @Autowired
    public TeamGoalRepository teamGoalRepository;
    private static final UUID organizationId = UUID.randomUUID();
    private static final UUID activeUserId = UUID.randomUUID();
    private static final UUID teamId = UUID.randomUUID();

    @Test
    @Order(1)
    void should_create_time_to_merge_team_goal_given_a_team() {
        // Given
        final OrganizationEntity organizationEntity = organizationRepository.save(
                OrganizationEntity.builder()
                        .id(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        );
        final String email = faker.gameOfThrones().character();
        UserMapper.entityToDomain(userRepository.save(
                UserEntity.builder()
                        .id(activeUserId)
                        .onboardingEntity(OnboardingEntity.builder().id(UUID.randomUUID()).hasConfiguredTeam(true).hasConnectedToVcs(true).build())
                        .organizationEntities(List.of(organizationEntity))
                        .status(User.ACTIVE)
                        .email(email)
                        .build()
        ));
        authenticationContextProvider.authorizeUserForMail(email);
        TeamMapper.entityToDomain(teamRepository.save(
                TeamEntity.builder()
                        .id(teamId)
                        .organizationId(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        ));
        final int value = faker.number().randomDigit();
        final PostCreateTeamGoalsRequest postCreateTeamGoalsRequest = new PostCreateTeamGoalsRequest();
        postCreateTeamGoalsRequest.setTeamId(teamId);
        postCreateTeamGoalsRequest.setValue(value);
        postCreateTeamGoalsRequest.setStandardCode(TeamStandard.TIME_TO_MERGE);

        // When
        client.post()
                .uri(getApiURI(TEAMS_GOALS_REST_API))
                .body(BodyInserters.fromValue(postCreateTeamGoalsRequest))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();
        assertThat(teamGoalEntities).hasSize(1);
        assertThat(teamGoalEntities.get(0).getTeamId()).isEqualTo(postCreateTeamGoalsRequest.getTeamId());
        assertThat(teamGoalEntities.get(0).getStandardCode()).isEqualTo(postCreateTeamGoalsRequest.getStandardCode());
        assertThat(teamGoalEntities.get(0).getValue()).isEqualTo(postCreateTeamGoalsRequest.getValue().toString());
    }

    @Order(2)
    @Test
    void should_return_an_error_for_invalid_standard_code() {
        final int value = faker.number().randomDigit();
        final PostCreateTeamGoalsRequest postCreateTeamGoalsRequest = new PostCreateTeamGoalsRequest();
        postCreateTeamGoalsRequest.setTeamId(teamId);
        postCreateTeamGoalsRequest.setValue(value);
        postCreateTeamGoalsRequest.setStandardCode(faker.dragonBall().character());

        // When
        client.post()
                .uri(getApiURI(TEAMS_GOALS_REST_API))
                .body(BodyInserters.fromValue(postCreateTeamGoalsRequest))
                .exchange()
                // Then
                .expectStatus()
                .is4xxClientError()
                .expectBody()
                .jsonPath("$.errors[0].code").isEqualTo(SymeoExceptionCode.INVALID_TEAM_STANDARD_CODE)
                .jsonPath("$.errors[0].message").isNotEmpty();
    }

    @Order(3)
    @Test
    void should_read_all_team_goals_given_a_team_id() {
        // Given
        teamGoalRepository.save(
                TeamGoalEntity.builder()
                        .teamId(teamId).id(UUID.randomUUID())
                        .standardCode(faker.dragonBall().character())
                        .value(Integer.toString(faker.number().randomDigit())).build());
        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();

        // When
        client.get()
                .uri(getApiURI(TEAMS_GOALS_REST_API, "team_id", teamId.toString()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.errors").isEmpty()
                .jsonPath("$.team_goals[0].id").isEqualTo(teamGoalEntities.get(0).getId().toString())
                .jsonPath("$.team_goals[0].standard_code").isEqualTo(teamGoalEntities.get(0).getStandardCode())
                .jsonPath("$.team_goals[0].value").isEqualTo(Integer.valueOf(teamGoalEntities.get(0).getValue()))
                .jsonPath("$.team_goals[0].current_value").isEqualTo(2.12)
                .jsonPath("$.team_goals[1].id").isEqualTo(teamGoalEntities.get(1).getId().toString())
                .jsonPath("$.team_goals[1].standard_code").isEqualTo(teamGoalEntities.get(1).getStandardCode())
                .jsonPath("$.team_goals[1].value").isEqualTo(Integer.valueOf(teamGoalEntities.get(1).getValue()))
                .jsonPath("$.team_goals[1].current_value").isEqualTo(2.12);
    }

    @Order(4)
    @Test
    void should_update_team_goal_value_given_an_id() {
        // Given
        final TeamGoalEntity teamGoalEntityToUpdate = teamGoalRepository.findAll().get(0);
        final PatchTeamGoalsRequest patchTeamGoalsRequest = new PatchTeamGoalsRequest();
        patchTeamGoalsRequest.setId(teamGoalEntityToUpdate.getId());
        final int newValue = faker.number().randomDigit();
        patchTeamGoalsRequest.setValue(newValue);

        // When
        client.patch()
                .uri(getApiURI(TEAMS_GOALS_REST_API))
                .body(BodyInserters.fromValue(patchTeamGoalsRequest))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        final TeamGoalEntity updatedTeamGoalEntity = teamGoalRepository.findById(teamGoalEntityToUpdate.getId()).get();
        assertThat(updatedTeamGoalEntity.getId()).isEqualTo(teamGoalEntityToUpdate.getId());
        assertThat(updatedTeamGoalEntity.getValue()).isEqualTo(Integer.toString(newValue));
        assertThat(updatedTeamGoalEntity.getStandardCode()).isEqualTo(teamGoalEntityToUpdate.getStandardCode());
        assertThat(updatedTeamGoalEntity.getTeamId()).isEqualTo(teamGoalEntityToUpdate.getTeamId());
    }

    @Order(5)
    @Test
    void should_delete_a_team_goal_given_an_id() {
        // Given
        final TeamGoalEntity teamGoalEntityToDelete = teamGoalRepository.findAll().get(0);

        // When
        client.delete()
                .uri(getApiURI(TEAMS_GOALS_REST_API, "team_goal_id", teamGoalEntityToDelete.getId().toString()))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();
        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();
        assertThat(teamGoalEntities).hasSize(1);
        assertThat(teamGoalEntities.get(0).getId()).isNotEqualTo(teamGoalEntityToDelete.getId());
    }

    @Test
    @Order(6)
    void should_create_pull_request_team_goal_given_a_team() {
        // Given
        TeamMapper.entityToDomain(teamRepository.save(
                TeamEntity.builder()
                        .id(teamId)
                        .organizationId(organizationId)
                        .name(faker.rickAndMorty().character())
                        .build()
        ));
        final int value = faker.number().randomDigit();
        final PostCreateTeamGoalsRequest postCreateTeamGoalsRequest = new PostCreateTeamGoalsRequest();
        postCreateTeamGoalsRequest.setTeamId(teamId);
        postCreateTeamGoalsRequest.setValue(value);
        postCreateTeamGoalsRequest.setStandardCode(TeamStandard.PULL_REQUEST_SIZE);

        // When
        client.post()
                .uri(getApiURI(TEAMS_GOALS_REST_API))
                .body(BodyInserters.fromValue(postCreateTeamGoalsRequest))
                .exchange()
                // Then
                .expectStatus()
                .is2xxSuccessful();

        final List<TeamGoalEntity> teamGoalEntities = teamGoalRepository.findAll();
        assertThat(teamGoalEntities).hasSize(2);
        assertThat(teamGoalEntities.get(1).getTeamId()).isEqualTo(postCreateTeamGoalsRequest.getTeamId());
        assertThat(teamGoalEntities.get(1).getStandardCode()).isEqualTo(postCreateTeamGoalsRequest.getStandardCode());
        assertThat(teamGoalEntities.get(1).getValue()).isEqualTo(postCreateTeamGoalsRequest.getValue().toString());
    }
}
