package io.symeo.monolithic.backend.domain.storage;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.TeamStandard;
import org.junit.jupiter.api.Test;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.INVALID_TEAM_STANDARD_CODE;
import static io.symeo.monolithic.backend.domain.model.account.TeamStandard.TIME_TO_MERGE;
import static org.assertj.core.api.Assertions.assertThat;

public class TeamStandardInMemoryStorageTest {

    private final Faker faker = new Faker();

    @Test
    void should_find_team_standard_by_code() throws SymeoException {
        // Given
        final TeamStandardInMemoryStorage teamStandardInMemoryStorage = new TeamStandardInMemoryStorage();

        // When
        final TeamStandard teamStandard = teamStandardInMemoryStorage.getByCode(TIME_TO_MERGE);

        // Then
        assertThat(teamStandard.getCode()).isEqualTo(TIME_TO_MERGE);
    }

    @Test
    void should_raise_exception_for_invalid_code() {
        // Given
        final TeamStandardInMemoryStorage teamStandardInMemoryStorage = new TeamStandardInMemoryStorage();
        final String standardCode = faker.dragonBall().character();

        // When
        SymeoException symeoException = null;
        try {
            teamStandardInMemoryStorage.getByCode(standardCode);
        } catch (SymeoException e) {
            symeoException = e;
        }

        // Then
        assertThat(symeoException).isNotNull();
        assertThat(symeoException.getMessage()).isEqualTo(String.format("Invalid team standard code %s",
                standardCode));
        assertThat(symeoException.getCode()).isEqualTo(INVALID_TEAM_STANDARD_CODE);
    }
}
