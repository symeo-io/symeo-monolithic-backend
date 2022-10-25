package io.symeo.monolithic.backend.domain.bff.model.metric;

import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder(toBuilder = true)
@Value
@Slf4j
public class CycleTimePiece {

    String id;
    Date creationDate;
    Date mergeDate;
    String state;
    String vcsUrl;
    String title;
    String author;
    String repository;
    Long cycleTime;
    Long codingTime;
    Long reviewTime;
    Long deployTime;

    public static List<CycleTimePiece> computeForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                                        List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                        List<CommitView> allCommitsUntilEndDate) {
        List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        for (PullRequestView pullRequestView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTimeForPullRequestView =
                    CycleTime.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(pullRequestView,
                            pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate, allCommitsUntilEndDate);

            cycleTimePieces.add(CycleTimePiece.builder()
                    .id(pullRequestView.getId())
                    .creationDate(pullRequestView.getCreationDate())
                    .mergeDate(pullRequestView.getMergeDate())
                    .state(pullRequestView.getStatus())
                    .vcsUrl(pullRequestView.getVcsUrl())
                    .title(pullRequestView.getTitle())
                    .author(pullRequestView.getAuthorLogin())
                    .repository(pullRequestView.getRepository())
                    .cycleTime(cycleTimeForPullRequestView.getValue())
                    .codingTime(cycleTimeForPullRequestView.getCodingTime())
                    .reviewTime(cycleTimeForPullRequestView.getReviewTime())
                    .deployTime(cycleTimeForPullRequestView.getDeployTime())
                    .build());
        }
        return cycleTimePieces;
    }

    public static List<CycleTimePiece> computeForTagRegexSettings(List<PullRequestView> pullRequestViewsToComputeCycleTime,
                                                                  List<TagView> tagsMatchingDeployTagRegex,
                                                                  List<CommitView> allCommitsUntilEndDate) {
        List<CycleTimePiece> cycleTimePieces = new ArrayList<>();
        for (PullRequestView pullRequestView : pullRequestViewsToComputeCycleTime) {
            final CycleTime cycleTimeForTagRegexSettings =
                    CycleTime.computeCycleTimeForTagRegexToDeploySettings(pullRequestView,
                            tagsMatchingDeployTagRegex, allCommitsUntilEndDate);

            cycleTimePieces.add(CycleTimePiece.builder()
                    .id(pullRequestView.getId())
                    .creationDate(pullRequestView.getCreationDate())
                    .mergeDate(pullRequestView.getMergeDate())
                    .state(pullRequestView.getStatus())
                    .vcsUrl(pullRequestView.getVcsUrl())
                    .title(pullRequestView.getTitle())
                    .author(pullRequestView.getAuthorLogin())
                    .repository(pullRequestView.getRepository())
                    .cycleTime(cycleTimeForTagRegexSettings.getValue())
                    .codingTime(cycleTimeForTagRegexSettings.getCodingTime())
                    .reviewTime(cycleTimeForTagRegexSettings.getReviewTime())
                    .deployTime(cycleTimeForTagRegexSettings.getDeployTime())
                    .build());
        }
        return cycleTimePieces;
    }
}
