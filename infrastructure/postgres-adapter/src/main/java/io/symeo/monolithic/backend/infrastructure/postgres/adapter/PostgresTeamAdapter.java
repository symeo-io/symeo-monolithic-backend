package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.Team;
import io.symeo.monolithic.backend.domain.bff.model.account.User;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;

@AllArgsConstructor
@Slf4j
public class PostgresTeamAdapter implements TeamStorage {

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
    @Transactional(readOnly = true)
    public Optional<Team> findById(UUID teamId) throws SymeoException {
        try {
            return teamRepository.findOneById(teamId)
                    .map(TeamMapper::entityToDomain);
        } catch (Exception e) {
            LOGGER.error("Failed to find team for id {}", teamId.toString(), e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find team by id " + teamId.toString())
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
    public Team update(Team team) throws SymeoException {
        try {
            final TeamEntity teamEntity =
                    teamRepository.findById(team.getId())
                            .orElseThrow(
                                    () -> {
                                        final String message = String.format("Failed to update team %s not found",
                                                team);
                                        LOGGER.error(message);
                                        return SymeoException.builder()
                                                .code(SymeoExceptionCode.TEAM_NOT_FOUND)
                                                .message(message)
                                                .build();
                                    }
                            );
            teamEntity.setName(team.getName());
            teamEntity.setRepositoryIds(team.getRepositories().stream().map(RepositoryView::getId).toList());
            return TeamMapper.entityToDomain(teamRepository.save(teamEntity));
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
