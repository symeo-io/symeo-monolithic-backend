package fr.catlean.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import fr.catlean.monolithic.backend.domain.helper.DateHelper;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.account.TeamGoal;
import fr.catlean.monolithic.backend.domain.model.account.TeamStandard;
import fr.catlean.monolithic.backend.domain.model.insight.PullRequestHistogram;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.Repository;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.VcsOrganization;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class PullRequestHistogramServiceTest {

    private final Faker faker = Faker.instance();
    private final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    @Test
    void should_compute_and_save_pull_request_size_histogram_given_simple_pull_request_cases() {
        // Given
        final PullRequestHistogramService pullRequestHistogramService =
                new PullRequestHistogramService();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("team1")
                .repositories(List.of(Repository.builder().name(repositoryName1).build()))
                .build();
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("fake-orga-account")
                .vcsOrganization(
                        VcsOrganization.builder().name("fake-orga-name").build()
                )
                .teams(List.of(team))
                .build();
        final TeamGoal teamGoal = TeamGoal.fromTeamStandardAndTeamId(TeamStandard.buildPullRequestSize(),
                team.getId(), 500);
        final List<PullRequest> pullRequests = getPullRequestsStubsWithSizeLimitToTestWeekRange(repositoryName1,
                organization, 100);
        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.SIZE_LIMIT)
                        .limit(teamGoal.getValueAsInteger())
                        .organizationId(organization.getId())
                        .build();

        final List<java.util.Date> weekStartDates =
                DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4, organization.getTimeZone());
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
                3, 1, SDF.format(weekStartDates.get(8))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                7, 1, SDF.format(weekStartDates.get(9))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                6, 2, SDF.format(weekStartDates.get(10))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                8, 1, SDF.format(weekStartDates.get(11))
        );


        // When
//        PullRequestHistogram pullRequestHistogramResult =
//                pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.SIZE_LIMIT,
//                        pullRequests, organization, teamGoal);

        // Then
//        assertThat(pullRequestHistogramResult).isEqualTo(pullRequestHistogram);
    }

    @Test
    void should_compute_and_save_pull_request_time_histogram_given_simple_pull_request_cases() {
        // Given
        final PullRequestHistogramService pullRequestHistogramService =
                new PullRequestHistogramService();
        final String repositoryName1 = faker.pokemon().name() + "-1";
        final Team team = Team.builder()
                .id(UUID.randomUUID())
                .name("team1")
                .repositories(List.of(Repository.builder().name(repositoryName1).build()))
                .build();
        final TeamGoal teamGoal = TeamGoal.fromTeamStandardAndTeamId(
                TeamStandard.buildTimeToMerge(), team.getId(), 5
        );
        final Organization organization = Organization.builder()
                .id(UUID.randomUUID())
                .name("fake-orga-account")
                .vcsOrganization(
                        VcsOrganization.builder().name("fake-orga-name").build()
                )
                .teams(List.of(team))
                .build();
        final List<PullRequest> pullRequests = getPullRequestsStubsWithSizeLimitToTestWeekRange(repositoryName1,
                organization, 100);
        final PullRequestHistogram pullRequestHistogram =
                PullRequestHistogram.builder().
                        type(PullRequestHistogram.TIME_LIMIT)
                        .limit(teamGoal.getValueAsInteger())
                        .organizationId(organization.getId())
                        .build();

        final List<java.util.Date> weekStartDates =
                DateHelper.getWeekStartDateForTheLastWeekNumber(3 * 4, organization.getTimeZone());
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
                4, 0, SDF.format(weekStartDates.get(8))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                4, 4, SDF.format(weekStartDates.get(9))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                3, 5, SDF.format(weekStartDates.get(10))
        );
        pullRequestHistogram.addDataBelowAndAboveLimitForWeek(
                1, 8, SDF.format(weekStartDates.get(11))
        );


        // When
//        PullRequestHistogram pullRequestHistogramResult =
//                pullRequestHistogramService.getPullRequestHistogram(PullRequestHistogram.TIME_LIMIT,
//                        pullRequests, organization, teamGoal);

        // Then
//        assertThat(pullRequestHistogramResult).isEqualTo(pullRequestHistogram);
    }


    public static List<PullRequest> getPullRequestsStubsWithSizeLimitToTestWeekRange(final String repositoryName,
                                                                                     final Organization organization,
                                                                                     final Integer halfCodeSize) {
        final java.util.Date weekStartDate = DateHelper.getWeekStartDate(organization.getTimeZone());


        final PullRequest pr1Above = PullRequest.builder()
                .id("pr1-above")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize * 1000)
                .deletedLineNumber(halfCodeSize * 1000)
                .build();

        final PullRequest pr1 = PullRequest.builder()
                .id("pr1")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();
        final PullRequest pr1Merged = PullRequest.builder()
                .id("pr1-merged")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .mergeDate(Date.valueOf(weekStartDate.toInstant()
                        .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
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
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(25, ChronoUnit.DAYS)))
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .build();

        final PullRequest pr2 = PullRequest.builder()
                .id("pr2")
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
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
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(15, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr3WeekStart = PullRequest.builder()
                .id("pr3-week-start")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(14, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
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
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
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
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(6, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr5 = PullRequest.builder()
                .id("pr5")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(12, ChronoUnit.DAYS)))
                .build();

        java.util.Date mergeDateFewHoursLater = Date.valueOf(weekStartDate.toInstant()
                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                .minus(9, ChronoUnit.DAYS));
        mergeDateFewHoursLater = Date.from(Instant.ofEpochMilli(mergeDateFewHoursLater.getTime() + 1000 * 60 * 60 * 6));

        final PullRequest pr5OneDay = PullRequest.builder()
                .id("pr5-one-day")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
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
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(4, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr7 = PullRequest.builder()
                .id("pr7")
                .addedLineNumber(halfCodeSize)
                .deletedLineNumber(halfCodeSize)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .build();
        final PullRequest pr7Above = PullRequest.builder()
                .id("pr7-above")
                .addedLineNumber(halfCodeSize * 100)
                .deletedLineNumber(halfCodeSize * 100)
                .repository(repositoryName)
                .creationDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .mergeDate(
                        Date.valueOf(weekStartDate.toInstant()
                                .atZone(organization.getTimeZone().toZoneId()).toLocalDate()
                                .minus(3, ChronoUnit.DAYS)))
                .build();


        return new ArrayList<>(List.of(pr1, pr1Above, pr1Draft, pr1Merged, pr2, pr3, pr3WeekStart, pr4, pr4Draft, pr5,
                pr5OneDay, pr6, pr7, pr7Above));
    }
}
