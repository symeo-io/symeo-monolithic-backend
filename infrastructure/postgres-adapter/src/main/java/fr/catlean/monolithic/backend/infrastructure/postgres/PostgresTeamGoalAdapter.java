package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.port.out.TeamGoalStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamGoalMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamGoalMapper.domainToEntity;


@AllArgsConstructor
@Slf4j
public class PostgresTeamGoalAdapter implements TeamGoalStorage {

    private final TeamRepository teamRepository;
    private final TeamGoalRepository teamGoalRepository;

    @Override
    @Transactional
    public void saveTeamGoal(TeamGoal teamGoal) throws CatleanException {
        try {
            teamGoalRepository.save(domainToEntity(teamGoal));
        } catch (Exception e) {
            final String message = String.format("Failed to save team goal %s", teamGoal);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamGoal> readForTeamId(UUID teamId) throws CatleanException {
        try {
            return teamGoalRepository.findAllByTeamId(teamId)
                    .stream()
                    .map(TeamGoalMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read team goals for teamId %s", teamId);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void deleteForId(UUID teamGoalId) throws CatleanException {
        try {
            teamGoalRepository.deleteById(teamGoalId);
        } catch (Exception e) {
            final String message = String.format("Failed to delete team goal for id %s", teamGoalId);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void updateForIdAndValue(UUID id, Integer value) throws CatleanException {
        try {
            teamGoalRepository.updateValueForId(id, value.toString());
        } catch (Exception e) {
            final String message = String.format("Failed to update team goal for id %s and value %s", id, value);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
