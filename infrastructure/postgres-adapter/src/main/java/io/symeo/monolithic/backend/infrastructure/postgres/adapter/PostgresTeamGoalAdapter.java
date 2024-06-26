package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.TeamGoal;
import io.symeo.monolithic.backend.domain.bff.port.out.TeamGoalStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.TeamGoalMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;


@AllArgsConstructor
@Slf4j
public class PostgresTeamGoalAdapter implements TeamGoalStorage {

    private final TeamRepository teamRepository;
    private final TeamGoalRepository teamGoalRepository;

    @Override
    @Transactional
    public void saveTeamGoal(TeamGoal teamGoal) throws SymeoException {
        try {
            teamGoalRepository.save(TeamGoalMapper.domainToEntity(teamGoal));
        } catch (Exception e) {
            final String message = String.format("Failed to save team goal %s", teamGoal);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TeamGoal> readForTeamId(UUID teamId) throws SymeoException {
        try {
            return teamGoalRepository.findAllByTeamIdOrderByTechnicalCreationDate(teamId)
                    .stream()
                    .map(TeamGoalMapper::entityToDomain)
                    .toList();
        } catch (Exception e) {
            final String message = String.format("Failed to read team goals for teamId %s", teamId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void deleteForId(UUID teamGoalId) throws SymeoException {
        try {
            teamGoalRepository.deleteById(teamGoalId);
        } catch (Exception e) {
            final String message = String.format("Failed to delete team goal for id %s", teamGoalId);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }

    @Override
    public void updateForIdAndValue(UUID id, Integer value) throws SymeoException {
        try {
            teamGoalRepository.updateValueForId(id, value.toString());
        } catch (Exception e) {
            final String message = String.format("Failed to update team goal for id %s and value %s", id, value);
            LOGGER.error(message, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
