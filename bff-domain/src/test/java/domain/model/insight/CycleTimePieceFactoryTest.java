package domain.model.insight;

import com.github.javafaker.Faker;
import io.symeo.monolithic.backend.domain.bff.model.metric.*;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.symeo.monolithic.backend.domain.helper.DateHelper.stringToDate;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CycleTimePieceFactoryTest {

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

        final CycleTimeFactory cycleTimeMock = mock(CycleTimeFactory.class);
        final CycleTimePieceFactory cycleTimePieceFactory = new CycleTimePieceFactory(cycleTimeMock);

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
                .timeToDeploy(timeToDeploy1)
                .build();
        final CycleTime cycleTime2 = CycleTime.builder()
                .value(cycleTimeValue2)
                .codingTime(codingTime2)
                .reviewTime(reviewTime2)
                .timeToDeploy(timeToDeploy2)
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
        final List<CycleTimePiece> cycleTimePieces = cycleTimePieceFactory.computeForPullRequestMergedOnBranchRegexSettings(
                pullRequestViewsToComputeCycleTime,
                pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                allCommitsUntilEndDate
        );

        // Then
        assertThat(cycleTimePieces.size()).isEqualTo(2);
        assertThat(cycleTimePieces.get(0).getId()).isEqualTo(pullRequestId1);
        assertThat(cycleTimePieces.get(0).getCycleTime()).isEqualTo(cycleTimeValue1);
        assertThat(cycleTimePieces.get(1).getId()).isEqualTo(pullRequestId2);
        assertThat(cycleTimePieces.get(1).getCycleTime()).isEqualTo(cycleTimeValue2);
    }

    @Test
    void should_compute_cycle_time_pieces_for_tags_matching_deploy_settings() throws SymeoException {
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

        final CycleTimeFactory cycleTimeMock = mock(CycleTimeFactory.class);
        final CycleTimePieceFactory cycleTimePieceFactory = new CycleTimePieceFactory(cycleTimeMock);

        final List<TagView> tagsMatchedToDeploy =
                List.of(
                        TagView.builder().commitSha(faker.pokemon().name()).name("deploy").build()
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
                .timeToDeploy(timeToDeploy1)
                .build();
        final CycleTime cycleTime2 = CycleTime.builder()
                .value(cycleTimeValue2)
                .codingTime(codingTime2)
                .reviewTime(reviewTime2)
                .timeToDeploy(timeToDeploy2)
                .build();

        // When
        when(cycleTimeMock.computeCycleTimeForTagRegexToDeploySettings(
                pullRequestViewsToComputeCycleTime.get(0),
                tagsMatchedToDeploy,
                allCommitsUntilEndDate
        )).thenReturn(cycleTime1);
        when(cycleTimeMock.computeCycleTimeForTagRegexToDeploySettings(
                pullRequestViewsToComputeCycleTime.get(1),
                tagsMatchedToDeploy,
                allCommitsUntilEndDate
        )).thenReturn(cycleTime2);
        final List<CycleTimePiece> cycleTimePieces = cycleTimePieceFactory.computeForTagRegexSettings(
                pullRequestViewsToComputeCycleTime,
                tagsMatchedToDeploy,
                allCommitsUntilEndDate
        );

        // Then
        assertThat(cycleTimePieces.size()).isEqualTo(2);
        assertThat(cycleTimePieces.get(0).getId()).isEqualTo(pullRequestId1);
        assertThat(cycleTimePieces.get(0).getCycleTime()).isEqualTo(cycleTimeValue1);
        assertThat(cycleTimePieces.get(1).getId()).isEqualTo(pullRequestId2);
        assertThat(cycleTimePieces.get(1).getCycleTime()).isEqualTo(cycleTimeValue2);
    }
}
