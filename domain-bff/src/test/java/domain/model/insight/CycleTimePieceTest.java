package domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTime;
import io.symeo.monolithic.backend.domain.bff.model.metric.CycleTimePiece;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CycleTimePieceTest {

    private static final Faker faker = new Faker();

    @Test
    void should_compute_cycle_time_pieces_for_pull_request_merged_on_branch_regex() throws SymeoException {
        // Given
        final String pullRequestId1 = faker.gameOfThrones().character() + "-1";
        final String pullRequestId2 = faker.gameOfThrones().character() + "-2";

        final Long cycleTimeValue1 = faker.number().randomNumber();
        final Long cycleTimeValue2 = faker.number().randomNumber();
        final Long codingTime1 = faker.number().randomNumber();
        final Long codingTime2 = faker.number().randomNumber();
        final Long reviewTime1 = faker.number().randomNumber();
        final Long reviewTime2 = faker.number().randomNumber();
        final Long timeToDeploy1 = faker.number().randomNumber();
        final Long timeToDeploy2 = faker.number().randomNumber();

        final CycleTime cycleTimeMock = mock(CycleTime.class);

        final List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate =
                List.of(
                        PullRequestView.builder().id(faker.gameOfThrones().character()).build()
                );

        final List<CommitView> allCommitsUntilEndDate = List.of(
                CommitView.builder().sha(faker.dragonBall().character()).build()
        );

        final List<PullRequestView> pullRequestViewsToComputeCycleTime =
                List.of(
                        PullRequestView.builder()
                                .id(pullRequestId1)
                                .creationDate(stringToDate("2022-01-02"))
                                .mergeDate(stringToDate("2022-01-03"))
                                .status("merge")
                                .vcsUrl(faker.pokemon().name())
                                .title(faker.dragonBall().character())
                                .authorLogin(faker.ancient().god())
                                .repository(faker.rickAndMorty().character())
                                .build(),
                        PullRequestView.builder()
                                .id(pullRequestId2)
                                .creationDate(stringToDate("2022-01-03"))
                                .mergeDate(stringToDate("2022-01-04"))
                                .status("open")
                                .vcsUrl(faker.pokemon().name())
                                .title(faker.dragonBall().character())
                                .authorLogin(faker.ancient().god())
                                .repository(faker.rickAndMorty().character())
                                .build()
                );

        final CycleTime cycleTime1 = CycleTime.builder()
                .value(cycleTimeValue1)
                .codingTime(codingTime1)
                .reviewTime(reviewTime1)
                .deployTime(timeToDeploy1)
                .build();
        final CycleTime cycleTime2 = CycleTime.builder()
                .value(cycleTimeValue2)
                .codingTime(codingTime2)
                .reviewTime(reviewTime2)
                .deployTime(timeToDeploy2)
                .build();

        // When

        when(cycleTimeMock.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequestViewsToComputeCycleTime.get(0),
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                allCommitsUntilEndDate
        )).thenReturn(cycleTime1);
        when(cycleTimeMock.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                pullRequestViewsToComputeCycleTime.get(1),
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                allCommitsUntilEndDate
        )).thenReturn(cycleTime2);
        final List<CycleTimePiece> cycleTimePieces = CycleTimePiece.computeForPullRequestMergedOnBranchRegexSettings(
                pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                allCommitsUntilEndDate
        );

        // Then
        assertThat(cycleTimePieces.size()).isEqualTo(2);
        assertThat(cycleTimePieces.get(0).getId()).isEqualTo(pullRequestId1);
        assertThat(cycleTimePieces.get(1).getId()).isEqualTo(pullRequestId2);
    }
}
