package io.symeo.monolithic.backend.domain.service.insights;

import io.symeo.monolithic.backend.domain.model.insight.Deployment;
import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;

import java.util.List;
import java.util.Optional;

import static io.symeo.monolithic.backend.domain.model.insight.Deployment.*;

public class DeploymentService {

    public Optional<Deployment> buildForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                      Long numberOfDaysBetweenStartDateAndEndDate) {
        return computeDeploymentForPullRequestMergedOnBranchRegexSettings(pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate);
    }

    public Optional<Deployment> buildForTagRegexSettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                Long numberOfDaysBetweenStartDateAndEndDate) {
        return computeDeploymentForTagRegexToDeploySettings(commitsMatchingTagRegexBetweenStartDateAndEndDate,
                numberOfDaysBetweenStartDateAndEndDate);
    }
}
