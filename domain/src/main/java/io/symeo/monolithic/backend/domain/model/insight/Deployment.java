package io.symeo.monolithic.backend.domain.model.insight;

import io.symeo.monolithic.backend.domain.model.insight.view.PullRequestView;
import io.symeo.monolithic.backend.domain.model.platform.vcs.Commit;
import lombok.Builder;
import lombok.Value;

import java.util.*;

@Builder(toBuilder = true)
@Value
public class Deployment {

    Integer deployCount;
    Float deploysPerDay;
    Float averageTimeBetweenDeploys;
    Float lastDeploy;
    String lastDeployRepository;
    public static Optional<Deployment> computeDeploymentForPullRequestMergedOnBranchRegexSettings(List<PullRequestView> pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate,
                                                                                                 Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = pullRequestViewsMergedOnMatchedBranchesBetweenStartDateAndEndDate.size();

        return  Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .build());
    }

    public static Optional<Deployment> computeDeploymentForTagRegexToDeploySettings(List<Commit> commitsMatchingTagRegexBetweenStartDateAndEndDate,
                                                                      Long numberOfDaysBetweenStartDateAndEndDate) {
        final int deployCount = commitsMatchingTagRegexBetweenStartDateAndEndDate.size();

        return Optional.of(Deployment.builder()
                .deployCount(deployCount)
                .build());
    }
}
