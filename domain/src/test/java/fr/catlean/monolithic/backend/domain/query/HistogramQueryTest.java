package fr.catlean.monolithic.backend.domain.query;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HistogramQueryTest {

    private final Faker faker = new Faker();

    @Test
    void should_read_histogram_for_organisation_and_team_and_type() throws CatleanException {
        // Given
        final AccountOrganizationStorageAdapter accountOrganizationStorageAdapter =
                mock(AccountOrganizationStorageAdapter.class);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final HistogramQuery histogramQuery = new HistogramQuery(expositionStorageAdapter);
        final String histogramType = "time-limit";
        final String organizationName = faker.university().name();
        final String teamName = faker.name().name();
        final UUID organizationId = UUID.randomUUID();
        final PullRequestHistogram pullRequestStub =
                PullRequestHistogram.builder().organizationId(organizationId).build();

        // When
        when(accountOrganizationStorageAdapter.findVcsOrganizationForName(organizationName)).thenReturn(
                Organization.builder().name(organizationName).vcsOrganization(VcsOrganization.builder().build()).build()
        );
        when(expositionStorageAdapter.readPullRequestHistogram(organizationId.toString(), teamName, histogramType)).thenReturn(pullRequestStub);
        final PullRequestHistogram pullRequestHistogram =
                histogramQuery.readPullRequestHistogram(Organization.builder().id(organizationId).vcsOrganization(VcsOrganization.builder().name(organizationName).build()).build(),
                        teamName, histogramType);

        // Then
        assertThat(pullRequestHistogram).isEqualTo(pullRequestStub);
    }

}
