package io.symeo.monolithic.backend.domain.service.job;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.domain.port.out.JobStorage;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JobService implements JobFacadeAdapter {

    private final JobStorage jobStorage;

    @Override
    public List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException {
        return jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(code, organization);
    }

    @Override
    public List<Job> findLastJobsForCodeAndOrganizationAndLimit(String jobCode, Organization organization,
                                                                int numberOfJobToFind) throws SymeoException {
        return jobStorage.findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(jobCode, organization,
                numberOfJobToFind);
    }

    public List<Job> findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(final UUID organizationId,
                                                                                final UUID teamId) {
        return jobStorage.findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(organizationId, teamId);
    }


}
