package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.User;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper.entityToDomain;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.UserMapper.domainToEntity;

@AllArgsConstructor
@Slf4j
public class PostgresTeamAdapter implements AccountTeamStorage {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;

    @Override
    public Team createTeamForUser(final Team team, final User user) throws CatleanException {
        try {
            final TeamEntity teamEntity = domainToEntity(team);
            final Team teamCreated = entityToDomain(teamRepository.save(teamEntity));
            userRepository.save(domainToEntity(user));
            return teamCreated;
        } catch (Exception e) {
            LOGGER.error("Failed to create team {}", team, e);
            throw CatleanException.builder()
                    .code("T.POSTGRES_EXCEPTION")
                    .message("Failed to create team " + team.getName())
                    .build();
        }
    }
}
