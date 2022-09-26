package io.symeo.monolithic.backend.domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.getNumberOfMinutesBetweenDates;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.Assertions.assertThat;

public class LeadTimeTest {

    private final static Faker faker = new Faker();


//    @Test
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
        final LeadTime leadTime = LeadTime.computeLeadTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequestView,
                List.of(),
                allCommits
        );

        // Then
        assertThat(leadTime.getDeployTime()).isNull();
    }

//    @Test
    void should_return_deploy_time_for_pr_merged_directly_on_a_branch_used_to_deploy() throws SymeoException {
        // Given
        final String mergeCommitSha = faker.ancient().god();
        final Commit mergeCommit = Commit.builder()
                .sha(mergeCommitSha)
                .date(stringToDate("2022-02-16"))
                .build();
        final String sha1 = faker.rickAndMorty().character() + "-1";
        final String sha2 = faker.rickAndMorty().character() + "-2";
        final String commitMergeToDeploySha = faker.rickAndMorty().character() + "-3";
        final String sha4 = faker.rickAndMorty().character() + "-4";
        final Date deployDate = stringToDate("2022-04-15");
        final Commit commitMergeToDeploy = Commit.builder()
                .sha(commitMergeToDeploySha)
                .date(deployDate)
                .parentShaList(List.of(sha4))
                .build();
        final List<Commit> allCommits = List.of(
                Commit.builder()
                        .sha(sha1)
                        .parentShaList(List.of(sha2, faker.name().firstName()))
                        .build(),
                Commit.builder()
                        .sha(sha2)
                        .parentShaList(List.of(commitMergeToDeploySha, faker.name().lastName()))
                        .build(),
                commitMergeToDeploy,
                Commit.builder()
                        .sha(sha4)
                        .parentShaList(List.of(mergeCommitSha, faker.name().name()))
                        .build(),
                mergeCommit
        );
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
        final List<PullRequestView> pullRequestViewsMatchingDeliverySettings = List.of(
                PullRequestView.builder()
                        .creationDate(stringToDate("2022-04-01"))
                        .mergeDate(deployDate)
                        .mergeCommitSha(mergeCommitSha)
                        .comments(List.of())
                        .commits(List.of(
                                Commit.builder().sha(faker.gameOfThrones().character())
                                        .date(stringToDate("2022-02-15"))
                                        .build(),
                                commitMergeToDeploy
                        ))
                        .mergeCommitSha(commitMergeToDeploy.getSha())
                        .build()
        );

        // When
        final LeadTime leadTime = LeadTime.computeLeadTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequestView,
                pullRequestViewsMatchingDeliverySettings,
                allCommits
        );

        // Then
        assertThat(leadTime.getDeployTime()).isEqualTo(getNumberOfMinutesBetweenDates(mergeCommit.getDate(),
                commitMergeToDeploy.getDate()));
    }


}
