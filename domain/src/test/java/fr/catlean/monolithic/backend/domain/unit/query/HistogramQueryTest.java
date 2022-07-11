package fr.catlean.monolithic.backend.domain.unit.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.query.HistogramQuery;
import org.junit.jupiter.api.Test;

public class HistogramQueryTest {

    private final Faker faker = new Faker();

    @Test
    void should_read_histogram_for_organisation_and_team_and_type() {
        // Given
        final HistogramQuery histogramQuery = new HistogramQuery();
        final String histogramType = "time-limit";
        final String organizationName = faker.university().name();
        final String teamName = faker.name().name();

        // When
        final PullRequestHistogram pullRequestHistogram = histogramQuery.readPullRequestHistogram(organizationName,
                teamName, histogramType);

        // Then
    }
}
