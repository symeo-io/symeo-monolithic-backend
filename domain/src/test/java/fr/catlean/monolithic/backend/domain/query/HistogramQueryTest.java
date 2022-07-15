package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HistogramQueryTest {

    private final Faker faker = new Faker();

    @Test
    void should_read_histogram_for_organisation_and_team_and_type() throws CatleanException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter, organizationStorageAdapter);
        final String histogramType = "time-limit";
        final String organizationName = faker.university().name();
        final String teamName = faker.name().name();
        final PullRequestHistogram pullRequestStub = generatePullRequestStub(organizationName);

        // When
        when(organizationStorageAdapter.findOrganizationForName(organizationName)).thenReturn(
                Organization.builder().name(organizationName).vcsConfiguration(VcsConfiguration.builder().build()).build()
        );
        when(expositionStorageAdapter.readPullRequestHistogram(organizationName, teamName, histogramType)).thenReturn(pullRequestStub);
        final PullRequestHistogram pullRequestHistogram = histogramQuery.readPullRequestHistogram(organizationName,
                teamName, histogramType);

        // Then
        assertThat(pullRequestHistogram).isEqualTo(pullRequestStub);
    }

    @Test
    void should_throw_not_found_organisation_exception_when_reading_pull_request_histogram() throws CatleanException {
        // Given
        final OrganizationStorageAdapter organizationStorageAdapter = mock(OrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter, organizationStorageAdapter);
        final String histogramType = "time-limit";
        final String organizationName = faker.university().name();
        final String teamName = faker.name().name();
        final CatleanException organisationNotFoundException =
                CatleanException.builder().code("F.ORGANISATION_NOT_FOUND").message(
                        "Organisation not found").build();

        // When
        when(organizationStorageAdapter.findOrganizationForName(organizationName))
                .thenThrow(organisationNotFoundException);

        // Then
        CatleanException catleanException = null;
        try {
            histogramQuery.readPullRequestHistogram(organizationName,
                    teamName, histogramType);
        } catch (CatleanException e) {
            catleanException = e;
        }
        assertThat(catleanException).isNotNull();
        assertThat(catleanException).isEqualTo(organisationNotFoundException);

    }

    private static PullRequestHistogram generatePullRequestStub(String organizationName) {
        return PullRequestHistogram.builder()
                .organizationAccount(organizationName)
                .build();
    }
}
