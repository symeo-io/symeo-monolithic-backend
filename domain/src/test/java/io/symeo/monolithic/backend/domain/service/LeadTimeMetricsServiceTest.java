package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.AverageLeadTime;
import io.symeo.monolithic.backend.domain.model.insight.LeadTimeMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeMetricsMetricsService;
import io.symeo.monolithic.backend.domain.service.insights.LeadTimeService;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LeadTimeMetricsServiceTest {


    private static final Faker faker = new Faker();

    @Test
    void should_get_empty_lead_time_metrics_for_wrong_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final LeadTimeService leadTimeService = mock(LeadTimeService.class);
        final LeadTimeMetricsMetricsService leadTimeMetricsService = new LeadTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                leadTimeService
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
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics =
                leadTimeMetricsService.computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalLeadTimeMetrics).isEmpty();
    }

    @Test
    void should_get_lead_time_metrics_given_merged_on_branch_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final LeadTimeService leadTimeService = mock(LeadTimeService.class);
        final LeadTimeMetricsMetricsService leadTimeMetricsService = new LeadTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                leadTimeService
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
        final AverageLeadTime averageLeadTime1 = AverageLeadTime.builder()
                .averageDeployTime(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageReviewLag(4.0F)
                .averageValue(5.0F)
                .build();
        final AverageLeadTime averageLeadTime2 = AverageLeadTime.builder()
                .averageDeployTime(2.0F)
                .averageCodingTime(3.0F)
                .averageReviewTime(4.0F)
                .averageReviewLag(5.0F)
                .averageValue(6.0F)
                .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                        "^staging$"));
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
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
                leadTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                        currentPullRequestViews,
                        List.of(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                        allCommits
                )
        ).thenReturn(Optional.of(averageLeadTime1));
        when(leadTimeService.buildForPullRequestMergedOnBranchRegexSettings(
                previousPullRequestViews,
                List.of(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                allCommits
        )).thenReturn(Optional.of(averageLeadTime2));
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics =
                leadTimeMetricsService.computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalLeadTimeMetrics).isNotNull();
        assertThat(optionalLeadTimeMetrics).isPresent();
        assertThat(optionalLeadTimeMetrics.get()).isEqualTo(
                LeadTimeMetrics.builder()
                        .averageDeployTime(1.0F)
                        .averageCodingTime(2.0F)
                        .averageReviewTime(3.0F)
                        .averageReviewLag(4.0F)
                        .average(5.0F)
                        .averageCodingTimePercentageTendency(-33.3F)
                        .averageReviewLagPercentageTendency(-20.0F)
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
    void should_get_lead_time_metrics_given_tag_to_deploy_regex_delivery_settings() throws SymeoException {
        // Given
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final LeadTimeService leadTimeService = mock(LeadTimeService.class);
        final LeadTimeMetricsMetricsService leadTimeMetricsService = new LeadTimeMetricsMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                leadTimeService
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
        final AverageLeadTime averageLeadTime2 = AverageLeadTime.builder()
                .averageDeployTime(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageReviewLag(4.0F)
                .averageValue(5.0F)
                .build();
        final AverageLeadTime averageLeadTime1 = AverageLeadTime.builder()
                .averageDeployTime(2.0F)
                .averageCodingTime(3.0F)
                .averageReviewTime(4.0F)
                .averageReviewLag(5.0F)
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
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(expositionStorageAdapter.readMergedPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
                .thenReturn(
                        previousPullRequestViews
                );
        when(expositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommits);
        when(expositionStorageAdapter.findTagsForTeamId(teamId)).thenReturn(
                tags
        );
        when(
                leadTimeService.buildForTagRegexSettings(
                        List.of(currentPullRequestViews.get(0)),
                        List.of(tags.get(0)),
                        allCommits
                )
        )
                .thenReturn(Optional.of(averageLeadTime1));
        when(leadTimeService.buildForTagRegexSettings(
                previousPullRequestViews,
                List.of(tags.get(0)),
                allCommits
        ))
                .thenReturn(Optional.of(averageLeadTime2));
        final Optional<LeadTimeMetrics> optionalLeadTimeMetrics =
                leadTimeMetricsService.computeLeadTimeMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalLeadTimeMetrics).isNotNull();
        assertThat(optionalLeadTimeMetrics).isPresent();
        assertThat(optionalLeadTimeMetrics.get()).isEqualTo(
                LeadTimeMetrics.builder()
                        .averageDeployTime(2.0F)
                        .averageCodingTime(3.0F)
                        .averageReviewTime(4.0F)
                        .averageReviewLag(5.0F)
                        .average(6.0F)
                        .averageCodingTimePercentageTendency(50.0F)
                        .averageReviewLagPercentageTendency(25.0F)
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
