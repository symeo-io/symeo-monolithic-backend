package fr.catlean.monolithic.backend.domain.storage;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.port.out.TeamStandardStorage;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.INVALID_TEAM_STANDARD_CODE;

@Slf4j
public class TeamStandardInMemoryStorage implements TeamStandardStorage {

    private final static Map<String, TeamStandard> TEAM_STANDARD_MAP =
            Map.of(
                    TeamStandard.TIME_TO_MERGE, TeamStandard.builder().code(TeamStandard.TIME_TO_MERGE).build()
            );

    @Override
    public TeamStandard getByCode(String standardCode) throws CatleanException {
        if (TEAM_STANDARD_MAP.containsKey(standardCode)) {
            return TEAM_STANDARD_MAP.get(standardCode);
        }
        final String message = String.format("Invalid team standard code %s", standardCode);
        LOGGER.error(message);
        throw CatleanException.builder()
                .code(INVALID_TEAM_STANDARD_CODE)
                .message(message)
                .build();
    }
}
