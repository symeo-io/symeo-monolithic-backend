package fr.catlean.delivery.processor.domain.unit.service;

import com.github.javafaker.Faker;
import fr.catlean.delivery.processor.domain.helper.DateHelper;
import fr.catlean.delivery.processor.domain.model.PullRequest;
import fr.catlean.delivery.processor.domain.model.account.OrganisationAccount;
import fr.catlean.delivery.processor.domain.model.account.VcsConfiguration;
import fr.catlean.delivery.processor.domain.model.insight.PullRequestHistogram;
import fr.catlean.delivery.processor.domain.port.out.ExpositionStorage;
import fr.catlean.delivery.processor.domain.service.PullRequestSizeService;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class PullRequestSizeServiceTest {

    private final Faker faker = Faker.instance();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");


    @Test
    void should_compute_and_save_pull_request_size_histogram_given_simple_pull_request_case() {
        // Given
        final OrganisationAccount organisationAccount = OrganisationAccount.builder()
                .name("fake-orga-account")
                .vcsConfiguration(
                        VcsConfiguration.builder().organisationName("fake-orga-name").build()
                ).build();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        organisationAccount.addTeam("team1", List.of(repositoryName1), 500, 5);
        final ExpositionStorage expositionStorage = mock(ExpositionStorage.class);
        final PullRequestSizeService pullRequestSizeService = new PullRequestSizeService(expositionStorage);
        final List<PullRequest> pullRequests = getPullRequestsStubsWithSizeLimitToTestWeekRange(repositoryName1,
                organisationAccount, 100);
        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.SIZE_LIMIT)
                        .limit(500)
                        .organisationAccount(organisationAccount.getName())
                        .build();

        final List<java.util.Date> weekStartDates =
                DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4, organisationAccount.getTimeZone());
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(0))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(1))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(2))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(3))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(4))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(5))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(6))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                0, 0, SDF.format(weekStartDates.get(7))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                1, 0, SDF.format(weekStartDates.get(8))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                3, 0, SDF.format(weekStartDates.get(9))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                4, 0, SDF.format(weekStartDates.get(10))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                6, 0, SDF.format(weekStartDates.get(11))
        );




        // When
        pullRequestSizeService.computeAndSavePullRequestSizeHistogram(pullRequests, organisationAccount);

        // Then
        verify(expositionStorage, times(1)).savePullRequestHistograms(List.of(pullRequestHistogram));
    }

    private static List<PullRequest> getPullRequestsStubsWithSizeLimitToTestWeekRange(final String repositoryName,
                                                                                      final OrganisationAccount organisationAccount,
                                                                                      final Integer halfCodeSize) {
        final java.util.Date weekStartDate = DateHelper.getWeekStartDate(organisationAccount.getTimeZone());

        final PullRequest pr1 = PullRequest.builder()
                .id("pr1")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();
        final PullRequest pr2 = PullRequest.builder()
                .id("pr2")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(15, ChronoUnit.DAYS)))
//                .mergeDate(Date.valueOf(weekStartDate.toInstant()
//                        .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
//                        .minus(10, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();
        final PullRequest pr3 = PullRequest.builder()
                .id("pr3")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(15, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr4 = PullRequest.builder()
                .id("pr4")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(6, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr5 = PullRequest.builder()
                .id("pr5")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(12, ChronoUnit.DAYS)))
//                .mergeDate(
//                        Date.valueOf(weekStartDate.toInstant()
//                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
//                                .minus(10, ChronoUnit.DAYS)))
                .build();

        final PullRequest pr6 = PullRequest.builder()
                .id("pr6")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(4, ChronoUnit.DAYS)))
//                .mergeDate(
//                        Date.valueOf(weekStartDate.toInstant()
//                                .atZone(organisationAccount.getTimeZone().toZoneId()).toLocalDate()
//                                .minus(4, ChronoUnit.DAYS)))
                .build();
        return new ArrayList<>(List.of(pr1, pr2, pr3, pr4, pr5, pr6));
    }
}
