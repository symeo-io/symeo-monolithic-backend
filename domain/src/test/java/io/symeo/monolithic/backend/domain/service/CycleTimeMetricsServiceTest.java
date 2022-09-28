package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.AverageCycleTime;
import io.symeo.monolithic.backend.domain.model.insight.CycleTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.insights.CycleTimeMetricsMetricsService;
import io.symeo.monolithic.backend.domain.service.insights.CycleTimeService;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CycleTimeMetricsServiceTest {


    private static final Faker faker = new Faker();

    @Test
    void should_get_empty_cycle_time_metrics_for_wrong_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final CycleTimeMetricsMetricsService cycleTimeMetricsService = new CycleTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = DateHelper.stringToDate("2022-01-01");
        final Date endDate = DateHelper.stringToDate("2022-02-01");

        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(
                        OrganizationSettings.builder()
                                .deliverySettings(
                                        DeliverySettings.builder()
                                                .deployDetectionSettings(
                                                        DeployDetectionSettings.builder().build()
                                                ).build()
                                )
                                .build()
                );
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics =
                cycleTimeMetricsService.computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalCycleTimeMetrics).isEmpty();
    }

    @Test
    void should_get_cycle_time_metrics_given_merged_on_branch_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final CycleTimeMetricsMetricsService cycleTimeMetricsService = new CycleTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = DateHelper.stringToDate("2022-01-01");
        final Date endDate = DateHelper.stringToDate("2022-02-01");
        final Date previousStartDate =
                DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("main").build());
        final List<PullRequestView> previousPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("staging").build()
                );
        final List<Commit> allCommits = List.of(
                Commit.builder().sha(faker.pokemon().name()).build(),
                Commit.builder().sha(faker.pokemon().location()).build()
        );
        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("staging").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.animal().name()).build()
        );
        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("staging").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.pokemon().name()).build()
        );
        final AverageCycleTime averageCycleTime1 = AverageCycleTime.builder()
                .averageDeployTime(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageValue(5.0F)
                .build();
        final AverageCycleTime averageCycleTime2 = AverageCycleTime.builder()
                .averageDeployTime(2.0F)
                .averageCodingTime(3.0F)
                .averageReviewTime(4.0F)
                .averageValue(6.0F)
                .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                        "^staging$"));
        when(expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
                .thenReturn(
                        previousPullRequestViews
                );
        when(expositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommits);
        when(expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                startDate, endDate))
                .thenReturn(
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                );
        when(expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                previousStartDate, startDate))
                .thenReturn(
                        previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                );
        when(
                cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        currentPullRequestViews,
                        List.of(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                        allCommits
                )
        ).thenReturn(Optional.of(averageCycleTime1));
        when(cycleTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                previousPullRequestViews,
                List.of(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                allCommits
        )).thenReturn(Optional.of(averageCycleTime2));
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics =
                cycleTimeMetricsService.computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalCycleTimeMetrics).isNotNull();
        assertThat(optionalCycleTimeMetrics).isPresent();
        assertThat(optionalCycleTimeMetrics.get()).isEqualTo(
                CycleTimeMetrics.builder()
                        .averageDeployTime(1.0F)
                        .averageCodingTime(2.0F)
                        .averageReviewTime(3.0F)
                        .average(5.0F)
                        .averageCodingTimePercentageTendency(-33.3F)
                        .averageTendencyPercentage(-16.7F)
                        .averageReviewTimePercentageTendency(-25.0F)
                        .averageDeployTimePercentageTendency(-50F)
                        .previousEndDate(startDate)
                        .previousStartDate(previousStartDate)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .build()
        );
    }


    @Test
    void should_get_cycle_time_metrics_given_tag_to_deploy_regex_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final CycleTimeMetricsMetricsService cycleTimeMetricsService = new CycleTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = DateHelper.stringToDate("2022-01-01");
        final Date endDate = DateHelper.stringToDate("2022-02-01");
        final Date previousStartDate =
                DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("main").build());
        final List<PullRequestView> previousPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("staging").build()
                );
        final List<Commit> allCommits = List.of(
                Commit.builder().sha(faker.pokemon().name()).build(),
                Commit.builder().sha(faker.pokemon().location()).build()
        );
        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("staging").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.animal().name()).build()
        );
        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("staging").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.pokemon().name()).build()
        );
        final List<Tag> tags = List.of(
                Tag.builder().name("deploy").build(),
                Tag.builder().name(faker.funnyName().name()).build()
        );
        final AverageCycleTime averageCycleTime2 = AverageCycleTime.builder()
                .averageDeployTime(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageValue(5.0F)
                .build();
        final AverageCycleTime averageCycleTime1 = AverageCycleTime.builder()
                .averageDeployTime(2.0F)
                .averageCodingTime(3.0F)
                .averageReviewTime(4.0F)
                .averageValue(6.0F)
                .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.builder()
                        .deliverySettings(DeliverySettings.builder().deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .excludeBranchRegexes(List.of("^main$"))
                                                .tagRegex("^deploy$")
                                                .build())
                                .build()
                        ).build());
        when(expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(expositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
                .thenReturn(
                        previousPullRequestViews
                );
        when(expositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommits);
        when(expositionStorageAdapter.findTagsForTeamId(teamId)).thenReturn(
                tags
        );
        when(
                cycleTimeService.buildForTagRegexSettings(
                        List.of(currentPullRequestViews.get(0)),
                        List.of(tags.get(0)),
                        allCommits
                )
        )
                .thenReturn(Optional.of(averageCycleTime1));
        when(cycleTimeService.buildForTagRegexSettings(
                previousPullRequestViews,
                List.of(tags.get(0)),
                allCommits
        ))
                .thenReturn(Optional.of(averageCycleTime2));
        final Optional<CycleTimeMetrics> optionalCycleTimeMetrics =
                cycleTimeMetricsService.computeCycleTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalCycleTimeMetrics).isNotNull();
        assertThat(optionalCycleTimeMetrics).isPresent();
        assertThat(optionalCycleTimeMetrics.get()).isEqualTo(
                CycleTimeMetrics.builder()
                        .averageDeployTime(2.0F)
                        .averageCodingTime(3.0F)
                        .averageReviewTime(4.0F)
                        .average(6.0F)
                        .averageCodingTimePercentageTendency(50.0F)
                        .averageTendencyPercentage(20.0F)
                        .averageReviewTimePercentageTendency(33.3F)
                        .averageDeployTimePercentageTendency(100.0F)
                        .previousEndDate(startDate)
                        .previousStartDate(previousStartDate)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .build()
        );
    }


}
