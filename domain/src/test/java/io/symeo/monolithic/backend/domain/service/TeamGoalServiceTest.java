package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.port.out.TeamGoalStorage;
import io.symeo.monolithic.backend.domain.port.out.TeamStandardStorage;
import io.symeo.monolithic.backend.domain.service.account.TeamGoalService;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TeamGoalServiceTest {

    private final Faker faker = new Faker();

    @Test
    void should_create_team_goal() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();
        final String standardCode = faker.dragonBall().character();
        final int value = faker.number().randomDigit();

        // When
        when(teamStandardStorage.getByCode(standardCode)).
                thenReturn(TeamStandard.builder().code(standardCode).build());
        teamGoalService.createTeamGoalForTeam(teamId, standardCode,
                value);

        // Then
        final ArgumentCaptor<TeamGoal> teamGoalArgumentCaptor = ArgumentCaptor.forClass(TeamGoal.class);
        verify(teamGoalStorage, times(1)).saveTeamGoal(teamGoalArgumentCaptor.capture());
        final TeamGoal captorValue = teamGoalArgumentCaptor.getValue();
        assertThat(captorValue.getTeamId()).isEqualTo(teamId);
        assertThat(captorValue.getStandardCode()).isEqualTo(standardCode);
        assertThat(captorValue.getValue()).isEqualTo(Integer.toString(value));
    }


    @Test
    void should_read_team_goals_for_team() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();

        // When
        final List<TeamGoal> expectedTeamGoals = List.of();
        when(teamGoalStorage.readForTeamId(teamId)).thenReturn(expectedTeamGoals);
        final List<TeamGoal> teamGoals = teamGoalService.readForTeamId(teamId);

        // Then
        assertThat(teamGoals).isEqualTo(expectedTeamGoals);
    }

    @Test
    void should_delete_team_goal_given_an_id() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();

        // When
        teamGoalService.deleteTeamGoalForId(teamId);

        // Then
        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(teamGoalStorage, times(1)).deleteForId(uuidArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isEqualTo(teamId);
    }

    @Test
    void should_update_team_goal_given_an_id_and_a_value() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID id = UUID.randomUUID();
        final int value = faker.number().randomDigit();


        // When
        teamGoalService.updateTeamGoalForTeam(id, value);

        // Then
        final ArgumentCaptor<UUID> uuidArgumentCaptor = ArgumentCaptor.forClass(UUID.class);
        final ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(teamGoalStorage, times(1)).updateForIdAndValue(uuidArgumentCaptor.capture(),
                integerArgumentCaptor.capture());
        assertThat(uuidArgumentCaptor.getValue()).isEqualTo(id);
        assertThat(integerArgumentCaptor.getValue()).isEqualTo(value);
    }

    @Test
    void should_return_team_goal_given_a_team_id_and_a_team_standard_code() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();
        final TeamGoal expectedTeamGoal =
                TeamGoal.builder().teamId(teamId).standardCode(TeamStandard.TIME_TO_MERGE).build();

        // When
        when(teamGoalStorage.readForTeamId(teamId))
                .thenReturn(
                        List.of(
                                expectedTeamGoal,
                                TeamGoal.builder().teamId(teamId).standardCode(faker.dragonBall().character()).build()
                        ));
        final TeamGoal teamGoal = teamGoalService.getTeamGoalForTeamIdAndTeamStandard(teamId,
                TeamStandard.buildTimeToMerge());

        // Then
        assertThat(teamGoal).isEqualTo(expectedTeamGoal);
    }

    @Test
    void should_raise_an_exception_for_team_standard_not_found() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();
        final TeamStandard teamStandard = TeamStandard.buildTimeToMerge();

        // When
        when(teamGoalStorage.readForTeamId(teamId))
                .thenReturn(
                        List.of(
                                TeamGoal.builder().teamId(teamId).standardCode(faker.dragonBall().character()).build(),
                                TeamGoal.builder().teamId(teamId).standardCode(faker.dragonBall().character()).build(),
                                TeamGoal.builder().teamId(teamId).standardCode(faker.dragonBall().character()).build()
                        ));
        SymeoException symeoException = null;
        try {

            teamGoalService.getTeamGoalForTeamIdAndTeamStandard(teamId, teamStandard);
        } catch (SymeoException exception) {
            symeoException = exception;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.TEAM_STANDARD_NOT_FOUND);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Team standard not found for code %s",
                teamStandard.getCode()));
    }

    @Test
    void should_raise_an_exception_for_team_not_found() throws SymeoException {
        // Given
        final TeamStandardStorage teamStandardStorage = mock(TeamStandardStorage.class);
        final TeamGoalStorage teamGoalStorage = mock(TeamGoalStorage.class);
        final TeamGoalService teamGoalService = new TeamGoalService(teamStandardStorage, teamGoalStorage);
        final UUID teamId = UUID.randomUUID();
        final TeamStandard teamStandard = TeamStandard.buildTimeToMerge();

        // When
        when(teamGoalStorage.readForTeamId(teamId))
                .thenReturn(List.of());
        SymeoException symeoException = null;
        try {

            teamGoalService.getTeamGoalForTeamIdAndTeamStandard(teamId, teamStandard);
        } catch (SymeoException exception) {
            symeoException = exception;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getCode()).isEqualTo(SymeoExceptionCode.TEAM_NOT_FOUND);
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Team not found for id %s",
                teamId));
    }
}
