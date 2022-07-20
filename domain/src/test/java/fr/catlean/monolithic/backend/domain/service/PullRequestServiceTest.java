package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.VcsConfiguration;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class PullRequestServiceTest {

    private final Faker faker = Faker.instance();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    void should_compute_collect_all_pull_requests_details_for_a_given_organization_account() throws CatleanException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final String vcsOrganizationName = faker.name().name();
        final String organizationName = faker.name().lastName();
        final String repo1Name = faker.pokemon().name() + "1";
        final String repo2Name = faker.pokemon().name() + "2";
        final String repo3Name = faker.pokemon().name() + "3";
        final String repo4Name = faker.pokemon().name() + "4";
        final Organization organizationAccount = Organization.builder().name(organizationName)
                .vcsConfiguration(
                        VcsConfiguration.builder()
                                .organizationName(vcsOrganizationName)
                                .build()
                )
                .build();
        final String team1Name = faker.pokemon().name() + "team1";
        final String team2Name = faker.pokemon().name() + "team2";
        organizationAccount.addTeam(team1Name, List.of(repo1Name, repo3Name), 1000, 5);
        organizationAccount.addTeam(team2Name, List.of(repo4Name), 500, 7);

        // When
        final PullRequest pr11 = PullRequest.builder().id("github-11").repository(repo1Name).build();
        final PullRequest pr12 = PullRequest.builder().id("github-12").repository(repo1Name).build();
        final PullRequest pr21 = PullRequest.builder().id("github-21").repository(repo2Name).build();
        final PullRequest pr22 = PullRequest.builder().id("github-22").repository(repo2Name).build();
        final List<PullRequest> pullRequestList = List.of(pr11, pr12, pr21, pr22);

        // When
        pullRequestService.savePullRequests(pullRequestList);

        // Then
        verify(expositionStorageAdapter, times(1)).savePullRequestDetails(pullRequestList);
    }

    @Test
    void should_compute_and_save_pull_request_size_histogram_given_simple_pull_request_cases() {
        // Given
        final Organization organizationAccount = Organization.builder()
                .name("fake-orga-account")
                .vcsConfiguration(
                        VcsConfiguration.builder().organizationName("fake-orga-name").build()
                ).build();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        organizationAccount.addTeam("team1", List.of(repositoryName1), 500, 5);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final List<PullRequest> pullRequests = getPullRequestsStubsWithSizeLimitToTestWeekRange(repositoryName1,
                organizationAccount, 100);
        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.SIZE_LIMIT)
                        .limit(500)
                        .organizationAccount(organizationAccount.getName())
                        .team(organizationAccount.getVcsConfiguration().getVcsTeams().get(0).getName())
                        .build();

        final List<java.util.Date> weekStartDates =
                DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4, organizationAccount.getTimeZone());
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
                2, 1, SDF.format(weekStartDates.get(8))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                6, 1, SDF.format(weekStartDates.get(9))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                5, 2, SDF.format(weekStartDates.get(10))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                6, 1, SDF.format(weekStartDates.get(11))
        );


        // When
        pullRequestService.computeAndSavePullRequestSizeHistogram(pullRequests, organizationAccount);

        // Then
        verify(expositionStorageAdapter, times(1)).savePullRequestHistograms(List.of(pullRequestHistogram));
    }

    @Test
    void should_compute_and_save_pull_request_time_histogram_given_simple_pull_request_cases() {
        // Given
        final Organization organizationAccount = Organization.builder()
                .name("fake-orga-account")
                .vcsConfiguration(
                        VcsConfiguration.builder().organizationName("fake-orga-name").build()
                ).build();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        organizationAccount.addTeam("team1", List.of(repositoryName1), 500, 5);
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final PullRequestService pullRequestService = new PullRequestService(expositionStorageAdapter);
        final List<PullRequest> pullRequests = getPullRequestsStubsWithSizeLimitToTestWeekRange(repositoryName1,
                organizationAccount, 100);
        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.TIME_LIMIT)
                        .limit(5)
                        .organizationAccount(organizationAccount.getName())
                        .team(organizationAccount.getVcsConfiguration().getVcsTeams().get(0).getName())
                        .build();

        final List<java.util.Date> weekStartDates =
                DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4, organizationAccount.getTimeZone());
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
                3, 0, SDF.format(weekStartDates.get(8))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                4, 3, SDF.format(weekStartDates.get(9))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                3, 4, SDF.format(weekStartDates.get(10))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                1, 6, SDF.format(weekStartDates.get(11))
        );


        // When
        pullRequestService.computeAndSavePullRequestTimeHistogram(pullRequests, organizationAccount);

        // Then
        verify(expositionStorageAdapter, times(1)).savePullRequestHistograms(List.of(pullRequestHistogram));
    }


    private static List<PullRequest> getPullRequestsStubsWithSizeLimitToTestWeekRange(final String repositoryName,
                                                                                      final Organization organizationAccount,
                                                                                      final Integer halfCodeSize) {
        final java.util.Date weekStartDate = DateHelper.getWeekStartDate(organizationAccount.getTimeZone());


        final PullRequest pr1Above = PullRequest.builder()
                .id("pr1-above")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize * 1000)
                .deletedLineNumber(halfCodeSize * 1000)
                .build();

        final PullRequest pr1 = PullRequest.builder()
                .id("pr1")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();
        final PullRequest pr1Merged = PullRequest.builder()
                .id("pr1-merged")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .mergeDate(Date.valueOf(weekStartDate.toInstant()
                        .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                        .minus(10, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();

        final PullRequest pr1Draft = PullRequest.builder()
                .id("pr1-draft")
                .repository(repositoryName)
                .isDraft(true)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();

        final PullRequest pr2 = PullRequest.builder()
                .id("pr2")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(15, ChronoUnit.DAYS)))
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
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(15, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr3WeekStart = PullRequest.builder()
                .id("pr3-week-start")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(14, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(13, ChronoUnit.DAYS))
                )
                .build();

        final PullRequest pr4 = PullRequest.builder()
                .id("pr4")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(6, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr4Draft = PullRequest.builder()
                .id("pr4-draft")
                .addedLineNumber(halfCodeSize)
                .isDraft(true)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(6, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr5 = PullRequest.builder()
                .id("pr5")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(12, ChronoUnit.DAYS)))
                .build();

        java.util.Date mergeDateFewHoursLater = Date.valueOf(weekStartDate.toInstant()
                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                .minus(9, ChronoUnit.DAYS));
        mergeDateFewHoursLater = Date.from(Instant.ofEpochMilli(mergeDateFewHoursLater.getTime() + 1000 * 60 * 60 * 6));

        final PullRequest pr5OneDay = PullRequest.builder()
                .id("pr5-one-day")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(9, ChronoUnit.DAYS)))
                .mergeDate(
                        mergeDateFewHoursLater)
                .build();
        final PullRequest pr6 = PullRequest.builder()
                .id("pr6")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(4, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr7 = PullRequest.builder()
                .id("pr7")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr7Above = PullRequest.builder()
                .id("pr7-above")
                .addedLineNumber(halfCodeSize * 100)
                .deletedLineNumber(halfCodeSize * 100)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organizationAccount.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .build();


        return new ArrayList<>(List.of(pr1, pr1Above, pr1Draft, pr1Merged, pr2, pr3, pr3WeekStart, pr4, pr4Draft, pr5,
                pr5OneDay, pr6, pr7, pr7Above));
    }
}
