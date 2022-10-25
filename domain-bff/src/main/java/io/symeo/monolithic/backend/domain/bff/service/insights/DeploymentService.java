package io.symeo.monolithic.backend.domain.bff.service.insights;

import io.symeo.monolithic.backend.domain.bff.model.metric.Deployment;
import io.symeo.monolithic.backend.domain.bff.model.vcs.CommitView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.PullRequestView;
import io.symeo.monolithic.backend.domain.bff.model.vcs.TagView;

import java.util.List;
import java.util.Optional;

public class DeploymentService {

    public Optional<Deployment> buildForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                               Long numberOfDaysBetweenStartDateAndEndDate) {
        return Deployment.computeDeploymentForPullRequestMergedOnBranchRegexSettings(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate);
    }

    public Optional<Deployment> buildForTagRegexSettings(List<CommitView> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                         Long numberOfDaysBetweenStartDateAndEndDate,
                                                         List<TagView> tagsMatchingTeamIdAndDeployTagRegex) {
        return Deployment.computeDeploymentForTagRegexToDeploySettings(commitsMatchingTagRegexBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate, tagsMatchingTeamIdAndDeployTagRegex);
    }
}
