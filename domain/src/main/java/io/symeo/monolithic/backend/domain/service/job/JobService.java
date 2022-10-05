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
    public List<Job> findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(UUID organizationId, UUID teamId) throws SymeoException {
        return jobStorage.findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(organizationId, teamId);
    }

}
