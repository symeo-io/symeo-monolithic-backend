package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.Team;
import io.symeo.monolithic.backend.domain.model.account.User;
import io.symeo.monolithic.backend.domain.port.out.AccountTeamStorage;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresAccountTeamAdapter implements AccountTeamStorage {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamGoalRepository teamGoalRepository;

    @Override
    public List<Team> createTeamsForUser(final List<Team> teams, final User user) throws SymeoException {
        try {
            final List<Team> teamsCreated = teamRepository.saveAll(
                            teams.stream()
                                    .map(TeamMapper::domainToEntity)
                                    .toList()
                    ).stream()
                    .map(TeamMapper::entityToDomain)
                    .toList();
            userRepository.save(UserMapper.domainToEntity(user));
            return teamsCreated;
        } catch (Exception e) {
            LOGGER.error("Failed to create teams {}", teams, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create teams " + String.join(",", teams.stream().map(Team::getName).toList()))
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> findByOrganization(Organization organization) throws SymeoException {
        try {
            return teamRepository.findAllByOrganizationId(organization.getId())
                    .stream()
                    .map(TeamMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            LOGGER.error("Failed to find teams for organization {}", organization, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find teams by organization " + organization.toString())
                    .build();
        }
    }

    @Override
    public void deleteById(UUID teamId) throws SymeoException {
        try {
            teamGoalRepository.deleteAllByTeamId(teamId);
            teamRepository.deleteById(teamId);
        } catch (Exception e) {
            final String message = String.format("Failed to delete team for id %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void update(Team team) throws SymeoException {
        try {
            teamRepository.save(TeamMapper.domainToEntity(team));
        } catch (Exception e) {
            final String message = String.format("Failed to update team %s", team);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
