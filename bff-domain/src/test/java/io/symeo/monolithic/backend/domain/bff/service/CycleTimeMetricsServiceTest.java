package io.symeo.monolithic.backend.domain.bff.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.DeployDetectionTypeDomainEnum;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.metric.*;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeMetricsService;
import io.symeo.monolithic.backend.domain.bff.service.insights.CycleTimeService;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CycleTimeMetricsServiceTest {


    private static final Faker faker = new Faker();

    @Test
    void should_get_cycle_time_metrics_given_merged_on_branch_delivery_settings() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Date previousStartDate =
                getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("head-1").build());
        final List<PullRequestView> previousPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("head-2").build()
                );
        final List<CommitView> allCommits = List.of(
                CommitView.builder().sha(faker.pokemon().name()).build(),
                CommitView.builder().sha(faker.pokemon().location()).build()
        );
        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("main").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.animal().name()).build()
        );
        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("main").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.pokemon().name()).build()
        );
        final AverageCycleTime averageCycleTime1 = AverageCycleTime.builder()
                .averageTimeToDeploy(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageValue(5.0F)
                .build();
        final AverageCycleTime averageCycleTime2 = AverageCycleTime.builder()
                .averageTimeToDeploy(2.0F)
                .averageCodingTime(3.0F)
                .averageReviewTime(4.0F)
                .averageValue(6.0F)
                .build();


        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationId(organization.getId()));
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
                .thenReturn(
                        previousPullRequestViews
                );
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommits);
        when(bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                startDate, endDate))
                .thenReturn(
                        pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate
                );
        when(bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
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
                        .averageTimeToDeploy(1.0F)
                        .averageCodingTime(2.0F)
                        .averageReviewTime(3.0F)
                        .average(5.0F)
                        .averageCodingTimePercentageTendency(-33.3F)
                        .averageTendencyPercentage(-16.7F)
                        .averageReviewTimePercentageTendency(-25.0F)
                        .averageTimeToDeployPercentageTendency(-50F)
                        .previousEndDate(startDate)
                        .previousStartDate(previousStartDate)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .build()
        );
    }

    @Test
    void should_get_cycle_time_pieces_given_merged_on_branch_delivery_settings() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        final int pageIndex = faker.number().randomDigit();
        final int pageSize = faker.number().randomDigit();
        final String sortBy = "vcs_repository";
        final String sortDir = "desc";

        final int countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination = faker.number().randomDigit();

        final List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted =
                List.of(
                        PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("main").build()
                );
        final List<CommitView> allCommitsUntilEndDate =
                List.of(
                        CommitView.builder().sha(faker.pokemon().name() + "-1").build(),
                        CommitView.builder().sha(faker.pokemon().name() + "-2").build()
                );
        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.animal().name()).base("main").build(),
                PullRequestView.builder().id(faker.animal().name()).base(faker.animal().name()).build()
        );
        final String cycleTimePieceId1 = faker.name().firstName() + "-1";

        final List<CycleTimePiece> cycleTimePiecesForPage =
                List.of(
                        CycleTimePiece.builder().id(cycleTimePieceId1).build()
                );

        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationId(organization.getId()));
        when(bffExpositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                startDate, endDate))
                .thenReturn(countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination);
        when(bffExpositionStorageAdapter.findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(
                teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir))
                .thenReturn(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommitsUntilEndDate);
        when(bffExpositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId,
                startDate, endDate))
                .thenReturn(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        when(cycleTimeService.buildCycleTimePiecesForPullRequestsMergedOnBranchRegexSettings(
                List.of(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted.get(0)),
                List.of(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)),
                allCommitsUntilEndDate))
                .thenReturn(cycleTimePiecesForPage);

        final CycleTimePiecePage cycleTimePiecePage =
                cycleTimeMetricsService.computeCycleTimePiecesForTeamIdFromStartDateToEndDate(organization,
                        teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir);

        // Then
        assertThat(cycleTimePiecePage.getTotalNumberOfPieces()).isEqualTo(countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination);
        assertThat(cycleTimePiecePage.getTotalNumberOfPages()).isEqualTo(
                (int) Math.ceil(1.0f * countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination / pageSize));
        assertThat(cycleTimePiecePage.getCycleTimePieces()).isEqualTo(cycleTimePiecesForPage);
    }


    @Test
    void should_get_cycle_time_metrics_given_tag_to_deploy_regex_delivery_settings() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Date previousStartDate =
                getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final List<PullRequestView> currentPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("main").build());
        final List<PullRequestView> previousPullRequestViews =
                List.of(PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("staging").build()
                );
        final List<CommitView> allCommits = List.of(
                CommitView.builder().sha(faker.pokemon().name()).build(),
                CommitView.builder().sha(faker.pokemon().location()).build()
        );
        final List<TagView> tags = List.of(
                TagView.builder().name("deploy").build(),
                TagView.builder().name(faker.funnyName().name()).build()
        );
        final AverageCycleTime averageCycleTime2 = AverageCycleTime.builder()
                .averageTimeToDeploy(1.0F)
                .averageCodingTime(2.0F)
                .averageReviewTime(3.0F)
                .averageValue(5.0F)
                .build();
        final AverageCycleTime averageCycleTime1 = AverageCycleTime.builder()
                .averageTimeToDeploy(2.0F)
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
                                                .deployDetectionType(DeployDetectionTypeDomainEnum.TAG)
                                                .build())
                                .build()
                        ).build());
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId,
                endDate))
                .thenReturn(
                        currentPullRequestViews
                );
        when(bffExpositionStorageAdapter.readPullRequestsWithCommitsForTeamIdUntilEndDate(teamId, startDate))
                .thenReturn(
                        previousPullRequestViews
                );
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommits);
        when(bffExpositionStorageAdapter.findTagsForTeamId(teamId)).thenReturn(
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
                        .averageTimeToDeploy(2.0F)
                        .averageCodingTime(3.0F)
                        .averageReviewTime(4.0F)
                        .average(6.0F)
                        .averageCodingTimePercentageTendency(50.0F)
                        .averageTendencyPercentage(20.0F)
                        .averageReviewTimePercentageTendency(33.3F)
                        .averageTimeToDeployPercentageTendency(100.0F)
                        .previousEndDate(startDate)
                        .previousStartDate(previousStartDate)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .build()
        );
    }

    @Test
    void should_get_cycle_time_pieces_given_tag_to_deploy_regex_delivery_settings() throws SymeoException {
        // Given
        final BffExpositionStorageAdapter bffExpositionStorageAdapter = mock(BffExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final CycleTimeService cycleTimeService = mock(CycleTimeService.class);
        final AverageCycleTimeFactory averageCycleTimeFactory = mock(AverageCycleTimeFactory.class);
        final CycleTimeMetricsService cycleTimeMetricsService = new CycleTimeMetricsService(
                bffExpositionStorageAdapter,
                organizationSettingsFacade,
                cycleTimeService,
                averageCycleTimeFactory
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");

        final int pageIndex = faker.number().randomDigit();
        final int pageSize = faker.number().randomDigit();
        final String sortBy = "vcs_repository";
        final String sortDir = "desc";

        final int countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination = faker.number().randomDigit();

        final List<PullRequestView> pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted =
                List.of(
                        PullRequestView.builder().id(faker.animal().name()).head(faker.ancient().god()).build(),
                        PullRequestView.builder().id(faker.animal().name()).head("main").build()
                );
        final List<CommitView> allCommitsUntilEndDate =
                List.of(
                        CommitView.builder().sha(faker.pokemon().name() + "-1").build(),
                        CommitView.builder().sha(faker.pokemon().name() + "-2").build()
                );
        final List<TagView> tagsMatchingDeployTagRegex = List.of(
                TagView.builder().name("deploy").build(),
                TagView.builder().name(faker.funnyName().name()).build()
        );
        final String cycleTimePieceId1 = faker.name().firstName() + "-1";
        final String cycleTimePieceId2 = faker.name().firstName() + "-2";

        final List<CycleTimePiece> cycleTimePiecesForPage =
                List.of(
                        CycleTimePiece.builder().id(cycleTimePieceId1).build(),
                        CycleTimePiece.builder().id(cycleTimePieceId2).build()
                );

        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.builder()
                        .deliverySettings(DeliverySettings.builder().deployDetectionSettings(
                                        DeployDetectionSettings.builder()
                                                .excludeBranchRegexes(List.of("^main$"))
                                                .tagRegex("^deploy$")
                                                .deployDetectionType(DeployDetectionTypeDomainEnum.TAG)
                                                .build())
                                .build()
                        ).build());
        when(bffExpositionStorageAdapter.countPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination(teamId,
                startDate, endDate))
                .thenReturn(countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination);
        when(bffExpositionStorageAdapter.findAllPullRequestViewByTeamIdUntilEndDatePaginatedAndSorted(
                teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir))
                .thenReturn(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted);
        when(bffExpositionStorageAdapter.readAllCommitsForTeamId(teamId))
                .thenReturn(allCommitsUntilEndDate);
        when(bffExpositionStorageAdapter.findTagsForTeamId(teamId))
                .thenReturn(tagsMatchingDeployTagRegex);
        when(cycleTimeService.buildCycleTimePiecesForTagRegexSettings(
                List.of(pullRequestViewsForTeamIdAndStartDateAndEndDateAndPaginationSorted.get(0)),
                List.of(tagsMatchingDeployTagRegex.get(0)),
                allCommitsUntilEndDate))
                .thenReturn(cycleTimePiecesForPage);

        final CycleTimePiecePage cycleTimePiecePage =
                cycleTimeMetricsService.computeCycleTimePiecesForTeamIdFromStartDateToEndDate(organization,
                        teamId, startDate, endDate, pageIndex, pageSize, sortBy, sortDir);

        // Then
        assertThat(cycleTimePiecePage.getTotalNumberOfPieces()).isEqualTo(countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination);
        assertThat(cycleTimePiecePage.getTotalNumberOfPages()).isEqualTo(
                (int) Math.ceil(1.0f * countOfPullRequestViewsForTeamIdAndStartDateAndEndDateAndPagination / pageSize));
        assertThat(cycleTimePiecePage.getCycleTimePieces()).isEqualTo(cycleTimePiecesForPage);
    }


}
