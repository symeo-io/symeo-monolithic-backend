package io.symeo.monolithic.backend.domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class LeadTimeTest {

    private final static Faker faker = new Faker();


    @Test
    void should_return_null_deploy_time_for_pr_merged_on_branches_not_used_to_deploy() throws SymeoException {
        // Given
        final OrganizationSettings organizationSettings =
                OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(UUID.randomUUID(), "^staging$");
        final String mergeCommitSha = faker.ancient().god();
        final PullRequestView pullRequestView = PullRequestView.builder()
                .creationDate(stringToDate("2022-02-01"))
                .mergeDate(stringToDate("2022-03-01"))
                .mergeCommitSha(mergeCommitSha)
                .comments(List.of())
                .commits(List.of(
                        Commit.builder().sha(faker.gameOfThrones().character())
                                .date(stringToDate("2022-02-15"))
                                .build()
                ))
                .build();
        final List<Commit> allCommits = List.of(
                Commit.builder()
                        .build());

        // When
        final LeadTime leadTime = LeadTime.computeLeadTimeForDeliverySettingsAndPullRequestViewAndAllCommits(
                organizationSettings,
                pullRequestView,
                allCommits
        );

        // Then
        assertThat(leadTime.getDeployTime()).isNull();
    }

    @Test
    void should_return_deploy_time_for_pr_merged_directly_on_a_branch_used_to_deploy() throws SymeoException {
        // Given
        final OrganizationSettings organizationSettings =
                OrganizationSettings.initializeFromOrganizationIdAndDefaultBranch(UUID.randomUUID(), "^main$");
        final String mergeCommitSha = faker.ancient().god();
        final Commit mergeCommit = Commit.builder()
                .sha(mergeCommitSha)
                .date(stringToDate("2022-02-16"))
                .build();
        final PullRequestView pullRequestView = PullRequestView.builder()
                .creationDate(stringToDate("2022-02-01"))
                .mergeDate(stringToDate("2022-03-01"))
                .mergeCommitSha(mergeCommitSha)
                .comments(List.of())
                .commits(List.of(
                        Commit.builder().sha(faker.gameOfThrones().character())
                                .date(stringToDate("2022-02-15"))
                                .build(),
                        mergeCommit
                ))
                .build();
        final List<Commit> allCommits = List.of(
                mergeCommit);

        // When
        final LeadTime leadTime = LeadTime.computeLeadTimeForDeliverySettingsAndPullRequestViewAndAllCommits(
                organizationSettings,
                pullRequestView,
                allCommits
        );

        // Then
        assertThat(leadTime.getDeployTime()).isNull();
    }


}
