package fr.catlean.monolithic.backend.domain.job.runnable;

import com.github.javafaker.Cat;
import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.JobRunnable;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.service.platform.vcs.VcsService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Value
@Builder
@Slf4j
public class CollectPullRequestsJobRunnable implements JobRunnable {
    VcsService vcsService;
    Organization organization;

    @Override
    public void run() throws CatleanException {
        try {
            LOGGER.info("Starting to collect PRs and Histograms for organization {}", organization);
            vcsService.collectPullRequestsForOrganization(organization);
            LOGGER.info("End of PRs and Histograms collections for organization {}", organization);
        } catch (CatleanException e) {
            LOGGER.error("Error while collecting PRs for organization {}", organization, e);
            throw e;
        }

    }

    public static final String JOB_CODE = "COLLECT_PULL_REQUESTS_FOR_ORGANIZATION_JOB";

    @Override
    public String getCode() {
        return JOB_CODE;
    }
}
