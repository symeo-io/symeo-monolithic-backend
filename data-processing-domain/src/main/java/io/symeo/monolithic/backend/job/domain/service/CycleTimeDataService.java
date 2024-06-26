package io.symeo.monolithic.backend.job.domain.service;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.job.domain.model.vcs.*;
import io.symeo.monolithic.backend.job.domain.port.out.DataProcessingExpositionStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.DateUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.time.DateUtils.*;

@AllArgsConstructor
@Slf4j
public class CycleTimeDataService {

    private final DataProcessingExpositionStorageAdapter dataProcessingExpositionStorageAdapter;
    private final CycleTimeDataFactory cycleTimeFactory;
    public List<CycleTime> computeCycleTimesForRepository(Repository repository, List<PullRequest> pullRequests,
                                                                 String deployDetectionType,
                                                                 String pullRequestMergedOnBranchRegex,
                                                                 String tagRegex,
                                                                 List<String> excludedBranchRegexes) throws SymeoException {

        if (deployDetectionType.equals("pull_request")) {
            return computeCycleTimesForPullRequestsMergedOnBranchRegex(
                    repository, pullRequests, pullRequestMergedOnBranchRegex, excludedBranchRegexes);
        } else if (deployDetectionType.equals("tag")) {
            return computeCycleTimesForTagRegexToDeploySettings(
                    repository, pullRequests, tagRegex, excludedBranchRegexes);
        } else {
            return List.of();
        }
    }

    private List<CycleTime> computeCycleTimesForPullRequestsMergedOnBranchRegex(Repository repository,
                                                                                       List<PullRequest> pullRequests,
                                                                                       String pullRequestMergedOnBranchRegex,
                                                                                       List<String> excludedBranchRegexes) throws SymeoException {
        final List<PullRequest> pullRequestsToComputeCycleTime =
                pullRequests
                        .stream()
                        .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                        .toList();
        final List<Commit> commitsForRepository = dataProcessingExpositionStorageAdapter.readAllCommitsForRepositoryId(repository.getId());
        final Pattern branchPattern = Pattern.compile(pullRequestMergedOnBranchRegex);

        final List<PullRequest> pullRequestsMergedOnMatchedBranches =
                dataProcessingExpositionStorageAdapter.readMergedPullRequestsForRepositoryIdUntilEndDate(repository.getId(), round(new Date(), Calendar.MINUTE))
                        .stream().filter(pullRequest -> branchPattern.matcher(pullRequest.getBase()).find()).toList();

        return pullRequestsToComputeCycleTime
                .stream()
                .map(pullRequest -> cycleTimeFactory.computeCycleTimeForMergeOnPullRequestMatchingDeliverySettings(
                        pullRequest,
                        pullRequestsMergedOnMatchedBranches,
                        commitsForRepository
                )).toList();
    }

    private List<CycleTime> computeCycleTimesForTagRegexToDeploySettings(Repository repository,
                                                                                List<PullRequest> pullRequests,
                                                                                String tagRegex,
                                                                                List<String> excludedBranchRegexes) throws SymeoException {
        final List<PullRequest> pullRequestsToComputeCycleTime =
                pullRequests
                        .stream()
                        .filter(pullRequest -> excludePullRequest(pullRequest, excludedBranchRegexes))
                        .toList();
        final List<Commit> commitsForRepository = dataProcessingExpositionStorageAdapter.readAllCommitsForRepositoryId(repository.getId());
        final Pattern tagPattern = Pattern.compile(tagRegex);

        final List<Tag> tagsMatchingDeployTagRegex =
                dataProcessingExpositionStorageAdapter.readTagsForRepositoryId(repository.getId())
                        .stream().filter(tag -> tagPattern.matcher(tag.getName()).find()).toList();

        return pullRequestsToComputeCycleTime
                .stream()
                .map(pullRequest -> cycleTimeFactory.computeCycleTimeForTagRegexToDeploySettings(
                        pullRequest,
                        tagsMatchingDeployTagRegex,
                        commitsForRepository
                )).toList();
    }

    private static boolean excludePullRequest(PullRequest pullRequest, List<String> excludedBranchRegexes) {
        return excludedBranchRegexes.isEmpty() || excludedBranchRegexes.stream().noneMatch(
                regex -> Pattern.compile(regex).matcher(pullRequest.getHead()).find()
        );
    }
}
