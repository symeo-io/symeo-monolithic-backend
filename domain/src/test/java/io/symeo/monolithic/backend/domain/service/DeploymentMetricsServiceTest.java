package io.symeo.monolithic.backend.domain.service;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.helper.DateHelper;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.model.account.settings.DeliverySettings;
import io.symeo.monolithic.backend.domain.model.account.settings.DeployDetectionSettings;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.Deployment;
import io.symeo.monolithic.backend.domain.model.insight.DeploymentMetrics;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Tag;
import io.symeo.monolithic.backend.domain.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.port.out.ExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.service.insights.DeploymentMetricsService;
import io.symeo.monolithic.backend.domain.service.insights.DeploymentService;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfDaysBetweenStartDateAndEndDate;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeploymentMetricsServiceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_get_empty_deployment_metrics_for_wrong_delivery_settings() throws SymeoException {
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final DeploymentService deploymentService = mock(DeploymentService.class);
        final DeploymentMetricsService deploymentMetricsService = new DeploymentMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                deploymentService
        );
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
        final Optional<DeploymentMetrics> optionalDeploymentMetrics =
                deploymentMetricsService.computeDeploymentMetricsForTeamIdFromStartDateToEndDate(
                        organization,
                        teamId,
                        startDate,
                        endDate
                );

        // Then
        assertThat(optionalDeploymentMetrics).isEmpty();
    }

    @Test
    void should_get_deployment_metrics_given_merged_on_branch_delivery_settings() throws SymeoException {
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final DeploymentService deploymentService = mock(DeploymentService.class);
        final DeploymentMetricsService deploymentMetricsService = new DeploymentMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                deploymentService
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Long numberOfDaysBetweenStartDateAndEndDate = getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);
        final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());
        final String fakeDeployLink = faker.gameOfThrones().character();

        final List<PullRequestView> currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.cat().name()).base("staging").mergeDate(stringToDate("2022-01-15")).build(),
                PullRequestView.builder().id(faker.dog().name()).base(faker.pokemon().name() + "-1").mergeDate(stringToDate("2022-01-22")).build()
        );
        final List<PullRequestView> previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate = List.of(
                PullRequestView.builder().id(faker.cat().name()).base("staging").mergeDate(stringToDate("2021-12-24")).build(),
                PullRequestView.builder().id(faker.dog().name()).base(faker.pokemon().name() + "-2").mergeDate(stringToDate("2021-12-12")).build()
        );

        final Deployment deployment1 =
                Deployment.builder()
                        .deployCount(20)
                        .deploysPerDay(0.1f)
                        .averageTimeBetweenDeploys(4.6f)
                        .lastDeployDuration(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .lastDeployLink(fakeDeployLink + "-current")
                        .build();
        final Deployment deployment2 =
                Deployment.builder()
                        .deployCount(10)
                        .deploysPerDay(0.3f)
                        .averageTimeBetweenDeploys(3.2f)
                        .lastDeployDuration(0.7f)
                        .lastDeployRepository("test-repo-2")
                        .lastDeployLink(fakeDeployLink + "-previous")
                        .build();

        // When
        when(organizationSettingsFacade.getOrganizationSettingsForOrganization(organization))
                .thenReturn(OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(
                        organization.getId(),
                        "^staging$"
                ));

        when(expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, startDate, endDate))
                .thenReturn(currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);
        when(expositionStorageAdapter.readMergedPullRequestsForTeamIdBetweenStartDateAndEndDate(teamId, previousStartDate, startDate))
                .thenReturn(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate);

        when(deploymentService.buildForPullRequestMergedOnBranchRegexSettings(
                List.of(currentPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)), numberOfDaysBetweenStartDateAndEndDate))
                .thenReturn(Optional.of(deployment1));

        when(deploymentService.buildForPullRequestMergedOnBranchRegexSettings(
                List.of(previousPullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.get(0)), numberOfDaysBetweenStartDateAndEndDate))
                .thenReturn(Optional.of(deployment2));

        final Optional<DeploymentMetrics> optionalDeploymentMetrics =
                deploymentMetricsService.computeDeploymentMetricsForTeamIdFromStartDateToEndDate(organization, teamId, startDate, endDate);

        // Then
        assertThat(optionalDeploymentMetrics).isNotNull();
        assertThat(optionalDeploymentMetrics).isPresent();
        assertThat(optionalDeploymentMetrics).get().isEqualTo(
                DeploymentMetrics.builder()
                        .deployCount(20)
                        .deployCountTendencyPercentage(100f)
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .deploysPerDay(0.1f)
                        .deploysPerDayTendencyPercentage(-66.7f)
                        .averageTimeBetweenDeploys(4.6f)
                        .averageTimeBetweenDeploysTendencyPercentage(43.7f)
                        .lastDeployDuration(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .lastDeployLink(fakeDeployLink + "-current")
                        .build()
        );
    }

    @Test
    void should_get_deployment_metrics_given_tag_to_deploy_regex_delivery_settings() throws SymeoException {
        final ExpositionStorageAdapter expositionStorageAdapter = mock(ExpositionStorageAdapter.class);
        final OrganizationSettingsFacade organizationSettingsFacade = mock(OrganizationSettingsFacade.class);
        final DeploymentService deploymentService = mock(DeploymentService.class);
        final DeploymentMetricsService deploymentMetricsService = new DeploymentMetricsService(
                expositionStorageAdapter,
                organizationSettingsFacade,
                deploymentService
        );
        final Organization organization = Organization.builder().id(UUID.randomUUID()).build();
        final UUID teamId = UUID.randomUUID();
        final Date startDate = stringToDate("2022-01-01");
        final Date endDate = stringToDate("2022-02-01");
        final Long numberOfDaysBetweenStartDateAndEndDate = getNumberOfDaysBetweenStartDateAndEndDate(startDate, endDate);
        final Date previousStartDate = DateHelper.getPreviousStartDateFromStartDateAndEndDate(startDate, endDate, organization.getTimeZone());

        final String fakeDeployLink = faker.gameOfThrones().character();

        final String fakeCommitSha1 = faker.pokemon().name() + "-1";
        final String fakeCommitSha2 = faker.pokemon().name() + "-2";
        final String fakeCommitSha3 = faker.pokemon().name() + "-3";
        final String fakeCommitSha4 = faker.pokemon().name() + "-4";
        final String fakeCommitSha5 = faker.pokemon().name() + "-5";

        final List<Tag> tagsMatchingDeployTagRegex = List.of(
                Tag.builder().name("deploy").commitSha(fakeCommitSha1).build(),
                Tag.builder().name("deploy").commitSha(fakeCommitSha2).build(),
                Tag.builder().name("deploy").commitSha(fakeCommitSha3).build(),
                Tag.builder().name(faker.funnyName().name() + "-1").commitSha(fakeCommitSha4).build(),
                Tag.builder().name(faker.funnyName().name() + "-2").commitSha(fakeCommitSha5).build()
        );

        final List<Commit> currentCommitsMatchingTagRegexBetweenStartDateAndEndDate = List.of(
                Commit.builder().sha(fakeCommitSha1).date(stringToDate("2022-01-15")).build(),
                Commit.builder().sha(fakeCommitSha2).date(stringToDate("2022-01-25")).build()
        );
        final List<Commit> previousCommitMatchingTagRegexBetweenStartDateAndEndDate = List.of(
                Commit.builder().sha(fakeCommitSha3).date(stringToDate("2021-12-22")).build()
        );

        final Deployment deployment1 =
                Deployment.builder()
                        .deployCount(20)
                        .deploysPerDay(0.1f)
                        .averageTimeBetweenDeploys(4.6f)
                        .lastDeployDuration(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .lastDeployLink(fakeDeployLink + "-current")
                        .build();
        final Deployment deployment2 =
                Deployment.builder()
                        .deployCount(15)
                        .deploysPerDay(0.3f)
                        .averageTimeBetweenDeploys(3.2f)
                        .lastDeployDuration(0.7f)
                        .lastDeployRepository("test-repo-2")
                        .lastDeployLink(fakeDeployLink + "-previous")
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
        when(expositionStorageAdapter.findTagsForTeamId(teamId))
                .thenReturn(tagsMatchingDeployTagRegex);

        when(expositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(
                tagsMatchingDeployTagRegex.subList(0, 3).stream()
                        .map(Tag::getCommitSha)
                        .toList(), startDate, endDate
        )).thenReturn(currentCommitsMatchingTagRegexBetweenStartDateAndEndDate);

        when(expositionStorageAdapter.readCommitsMatchingShaListBetweenStartDateAndEndDate(
                tagsMatchingDeployTagRegex.subList(0, 3).stream()
                        .map(Tag::getCommitSha)
                        .toList(), previousStartDate, startDate
        )).thenReturn(previousCommitMatchingTagRegexBetweenStartDateAndEndDate);

        when(deploymentService.buildForTagRegexSettings(currentCommitsMatchingTagRegexBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate,
                tagsMatchingDeployTagRegex)).thenReturn(Optional.of(deployment1));
        when(deploymentService.buildForTagRegexSettings(previousCommitMatchingTagRegexBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate,
                tagsMatchingDeployTagRegex)).thenReturn(Optional.of(deployment2));

        final Optional<DeploymentMetrics> optionalDeploymentMetrics =
                deploymentMetricsService.computeDeploymentMetricsForTeamIdFromStartDateToEndDate(organization, teamId, startDate, endDate);

        // Then
        assertThat(optionalDeploymentMetrics).isNotNull();
        assertThat(optionalDeploymentMetrics).isPresent();
        assertThat(optionalDeploymentMetrics.get()).isEqualTo(
                DeploymentMetrics.builder()
                        .currentStartDate(startDate)
                        .currentEndDate(endDate)
                        .previousStartDate(previousStartDate)
                        .previousEndDate(startDate)
                        .deployCount(20)
                        .deployCountTendencyPercentage(33.3f)
                        .deploysPerDay(0.1f)
                        .deploysPerDayTendencyPercentage(-66.7f)
                        .averageTimeBetweenDeploys(4.6f)
                        .averageTimeBetweenDeploysTendencyPercentage(43.7f)
                        .lastDeployDuration(1.4f)
                        .lastDeployRepository("test-repo-1")
                        .lastDeployLink(fakeDeployLink + "-current")
                        .build()
        );
    }
}
