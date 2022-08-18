package io.symeo.monolithic.backend.domain.storage;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import io.symeo.monolithic.backend.domain.port.out.TeamStandardStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class TeamStandardInMemoryStorage implements TeamStandardStorage {

    private final static Map<String, TeamStandard> TEAM_STANDARD_MAP =
            Map.of(
                    TeamStandard.TIME_TO_MERGE, TeamStandard.builder().code(TeamStandard.TIME_TO_MERGE).build(),
                    TeamStandard.PULL_REQUEST_SIZE, TeamStandard.builder().code(TeamStandard.PULL_REQUEST_SIZE).build()
            );

    @Override
    public TeamStandard getByCode(String standardCode) throws SymeoException {
        if (TEAM_STANDARD_MAP.containsKey(standardCode)) {
            return TEAM_STANDARD_MAP.get(standardCode);
        }
        final String message = String.format("Invalid team standard code %s", standardCode);
        LOGGER.error(message);
        throw SymeoException.builder()
                .code(SymeoExceptionCode.INVALID_TEAM_STANDARD_CODE)
                .message(message)
                .build();
    }
}
