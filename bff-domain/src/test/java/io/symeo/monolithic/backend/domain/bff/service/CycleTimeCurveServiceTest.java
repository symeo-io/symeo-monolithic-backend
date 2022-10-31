package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimeFactory;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurve;
import io.symeo.monolithic.backend.domain.bff.model.metric.curve.CycleTimePieceCurveWithAverage;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeCurveService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CycleTimeCurveServiceTest {
    private static final Faker faker = new Faker();

    @Test
    void should_get_empty_cycle_time_curve_data_for_wrong_delivery_settings() throws SymeoException {
        // Given
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final CycleTimeFactory cycleTimeFactory = mock(CycleTimeFactory.class);
        final CycleTimeCurveService cycleTimeCurveService = new CycleTimeCurveService(organizationSettingsFacade,
                bffExpositionStorageAdapter, cycleTimeFactory);

        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

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

        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage =
                cycleTimeCurveService.computeCycleTimePieceCurveWithAverage(organization, teamId, startDate, endDate);

        // Then
        assertThat(cycleTimePieceCurveWithAverage.getAverageCurve().getData()).isEmpty();
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()).isEmpty();
    }

    @Test
    void should_get_cycle_time_curve_data_for_pull_request_merged_on_branch_delivery_settings() throws SymeoException {
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeFactory cycleTimeFactory = mock(CycleTimeFactory.class);
        final CycleTimeCurveService cycleTimeCurveService = new CycleTimeCurveService(
                organizationSettingsFacade,
                bffExpositionStorageAdapter,
                cycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-01-07");

        final String pullRequestViewId1 = faker.harryPotter().character() + "-1";
        final String pullRequestViewId2 = faker.harryPotter().character() + "-2";

        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("head-1").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("head-2").build());
        final List<CommitView> allCommitsUntilEndDate = List.of(
                CommitView.builder().sha(faker.pokemon().name()).build(),
                CommitView.builder().sha(faker.pokemon().location()).build()
        );

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(pullRequestViewId1).base("staging").build(),
                PullRequestView.builder().id(pullRequestViewId2).base(faker.animal().name()).build()
        );
        final CycleTime cycleTime1 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .pullRequestView(currentPullRequestViews.get(0))
                        .build();
        final CycleTime cycleTime2 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .pullRequestView(currentPullRequestViews.get(1))
                        .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(organization.getId(),
                        "staging"));
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommitsUntilEndDate);
        when(bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        when(cycleTimeFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("head-1").startDateRange("2022-01-03").build(),
                List.of(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                allCommitsUntilEndDate
        )).thenReturn(cycleTime1);
        when(cycleTimeFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("head-2").startDateRange("2022-01-05").build(),
                List.of(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                allCommitsUntilEndDate
        )).thenReturn(cycleTime2);
        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage = cycleTimeCurveService.computeCycleTimePieceCurveWithAverage(
                organization, teamId, startDate, endDate
        );

        // Then
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()).isNotEmpty();
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().size()).isEqualTo(2);
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(0)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date(cycleTime1.getPullRequestView().getStartDateRange())
                        .value(cycleTime1.getValue())
                        .codingTime(cycleTime1.getCodingTime())
                        .reviewTime(cycleTime1.getReviewTime())
                        .timeToDeploy(cycleTime1.getTimeToDeploy())
                        .label(cycleTime1.getPullRequestView().getHead())
                        .link(cycleTime1.getPullRequestView().getVcsUrl())
                        .build()
        );
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(1)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date(cycleTime2.getPullRequestView().getStartDateRange())
                        .value(cycleTime2.getValue())
                        .codingTime(cycleTime2.getCodingTime())
                        .reviewTime(cycleTime2.getReviewTime())
                        .timeToDeploy(cycleTime2.getTimeToDeploy())
                        .label(cycleTime2.getPullRequestView().getHead())
                        .link(cycleTime2.getPullRequestView().getVcsUrl())
                        .build()
        );
    }

    @Test
    void should_get_cycle_time_curve_data_for_tag_regex_delivery_settings() throws SymeoException {
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeFactory cycleTimeFactory = mock(CycleTimeFactory.class);
        final CycleTimeCurveService cycleTimeCurveService = new CycleTimeCurveService(
                organizationSettingsFacade,
                bffExpositionStorageAdapter,
                cycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-01-07");

        final String pullRequestViewId1 = faker.harryPotter().character() + "-1";
        final String pullRequestViewId2 = faker.harryPotter().character() + "-2";

        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("test").build(),
                        PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("main").build());
        final List<CommitView> allCommitsUntilEndDate = List.of(
                CommitView.builder().sha(faker.pokemon().name()).build(),
                CommitView.builder().sha(faker.pokemon().location()).build()
        );
        final List<TagView> tagsMatchingDeployTagRegex = List.of(
                TagView.builder().name("deploy").build(),
                TagView.builder().name(faker.funnyName().name()).build()
        );
        final CycleTime cycleTime1 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .pullRequestView(currentPullRequestViews.get(0))
                        .build();
        final CycleTime cycleTime2 =
                CycleTime.builder()
                        .value(faker.number().randomNumber())
                        .codingTime(faker.number().randomNumber())
                        .reviewTime(faker.number().randomNumber())
                        .timeToDeploy(faker.number().randomNumber())
                        .pullRequestView(currentPullRequestViews.get(1))
                        .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.builder()
                        .deliverySettings(DeliverySettings.builder()
                                .deployDetectionSettings(DeployDetectionSettings.builder()
                                        .excludeBranchRegexes(List.of("^main$"))
                                        .tagRegex("^deploy$")
                                        .build())
                                .build())
                        .build());
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, endDate))
                .thenReturn(currentPullRequestViews);
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommitsUntilEndDate);
        when(bffExpositionStorageAdapter.findTagsForTeamId(teamId))
                .thenReturn(tagsMatchingDeployTagRegex);
        when(cycleTimeFactory.computeCycleTimeForTagRegexToDeploySettings(
                PullRequestView.builder().id(pullRequestViewId1).mergeDate(stringToDate("2022-01-03")).head("test").startDateRange("2022-01-03").build(),
                List.of(tagsMatchingDeployTagRegex.get(0)),
                allCommitsUntilEndDate
        )).thenReturn(cycleTime1);
        when(cycleTimeFactory.computeCycleTimeForTagRegexToDeploySettings(
                PullRequestView.builder().id(pullRequestViewId2).mergeDate(stringToDate("2022-01-05")).head("main").startDateRange("2022-01-05").build(),
                List.of(tagsMatchingDeployTagRegex.get(0)),
                allCommitsUntilEndDate
        )).thenReturn(cycleTime2);
        final CycleTimePieceCurveWithAverage cycleTimePieceCurveWithAverage = cycleTimeCurveService.computeCycleTimePieceCurveWithAverage(
                organization, teamId, startDate, endDate
        );

        // Then
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData()).isNotEmpty();
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().size()).isEqualTo(1);
        assertThat(cycleTimePieceCurveWithAverage.getCycleTimePieceCurve().getData().get(0)).isEqualTo(
                CycleTimePieceCurve.CyclePieceCurvePoint.builder()
                        .date(cycleTime1.getPullRequestView().getStartDateRange())
                        .value(cycleTime1.getValue())
                        .codingTime(cycleTime1.getCodingTime())
                        .reviewTime(cycleTime1.getReviewTime())
                        .timeToDeploy(cycleTime1.getTimeToDeploy())
                        .label(cycleTime1.getPullRequestView().getHead())
                        .link(cycleTime1.getPullRequestView().getVcsUrl())
                        .build()
        );
    }
}
