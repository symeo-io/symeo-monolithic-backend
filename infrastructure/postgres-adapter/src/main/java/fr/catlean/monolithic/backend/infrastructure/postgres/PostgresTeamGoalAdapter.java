package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.port.out.TeamGoalStorage;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamGoalEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamGoalMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamGoalRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;


@AllArgsConstructor
@Slf4j
public class PostgresTeamGoalAdapter implements TeamGoalStorage {

    private final TeamRepository teamRepository;
    private final TeamGoalRepository teamGoalRepository;

    @Override
    @Transactional
    public void saveTeamGoal(TeamGoal teamGoal) throws CatleanException {
        try {
            final TeamEntity teamEntity = teamRepository.getById(teamGoal.getTeamId());
            final TeamGoalEntity teamGoalEntity = TeamGoalMapper.domainToEntity(teamGoal);
            teamGoalEntity.setTeamEntity(teamEntity);
            teamGoalRepository.save(teamGoalEntity);
        } catch (Exception e) {
            final String message = String.format("Failed to save team goal %s", teamGoal);
            LOGGER.error(message, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message(message)
                    .build();
        }
    }
}
