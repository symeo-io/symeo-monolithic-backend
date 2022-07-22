package fr.catlean.monolithic.backend.domain.job.runnable;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.JobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.service.insights.PullRequestHistogramService;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@AllArgsConstructor
@Value
@Builder
@Slf4j
public class CollectPullRequestsJobRunnable implements JobRunnable {
    VcsService vcsService;
    Organization organization;
    PullRequestHistogramService pullRequestHistogramService;

    @Override
    public void run() {
        try {
            final List<PullRequest> pullRequestList =
                    vcsService.collectPullRequestsForOrganization(organization);
            pullRequestHistogramService.savePullRequests(pullRequestList);
            pullRequestHistogramService.computeAndSavePullRequestSizeHistogram(pullRequestList, organization);
            pullRequestHistogramService.computeAndSavePullRequestTimeHistogram(pullRequestList, organization);
        } catch (CatleanException e) {
            LOGGER.error("Error while collection PRs for organization {}", organization, e);
        }

    }

    public static final String JOB_CODE = "COLLECT_PULL_REQUESTS_FOR_ORGANIZATION_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }
}