package fr.catlean.monolithic.backend.domain.storage;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import org.junit.jupiter.api.Test;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.INVALID_TEAM_STANDARD_CODE;
import static fr.catlean.monolithic.backend.domain.model.account.TeamStandard.TIME_TO_MERGE;
import static org.assertj.core.api.Assertions.assertThat;

public class TeamStandardInMemoryStorageTest {

    private final Faker faker = new Faker();

    @Test
    void should_find_team_standard_by_code() throws CatleanException {
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
        CatleanException catleanException = null;
        try {
            teamStandardInMemoryStorage.getByCode(standardCode);
        } catch (CatleanException e) {
            catleanException = e;
        }

        // Then
        assertThat(catleanException).isNotNull();
        assertThat(catleanException.getMessage()).isEqualTo(String.format("Invalid team standard code %s",
                standardCode));
        assertThat(catleanException.getCode()).isEqualTo(INVALID_TEAM_STANDARD_CODE);
    }
}
