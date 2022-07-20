package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.domainToEntity;

@AllArgsConstructor
@Slf4j
public class PostgresAccountTeamAdapter implements AccountTeamStorage {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public List<Team> createTeamsForUser(final List<Team> teams, final User user) throws CatleanException {
        try {
            final List<Team> teamsCreated = teamRepository.saveAll(
                            teams.stream()
                                    .map(TeamMapper::domainToEntity)
                                    .toList()
                    ).stream()
                    .map(TeamMapper::entityToDomain)
                    .toList();
            userRepository.save(domainToEntity(user));
            return teamsCreated;
        } catch (Exception e) {
            LOGGER.error("Failed to create teams {}", teams, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create teams " + String.join(",", teams.stream().map(Team::getName).toList()))
                    .build();
        }
    }
}
