package fr.catlean.monolithic.backend.domain.job.runnable;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.JobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.model.platform.vcs.PullRequest;
import fr.catlean.monolithic.backend.domain.port.out.AccountTeamStorage;
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
    AccountTeamStorage accountTeamStorage;

    @Override
    public void run() {
        try {
            LOGGER.info("Starting to collect PRs and Histograms for organization {}", organization);
            final List<Team> teams = accountTeamStorage.findByOrganization(organization);
            final Organization organizationWithTeams = organization.toBuilder().teams(teams).build();
            final List<PullRequest> pullRequestList =
                    vcsService.collectPullRequestsForOrganization(organizationWithTeams);
            pullRequestHistogramService.savePullRequests(pullRequestList);
            LOGGER.info("End of PRs and Histograms collections for organization {}", organizationWithTeams);
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
