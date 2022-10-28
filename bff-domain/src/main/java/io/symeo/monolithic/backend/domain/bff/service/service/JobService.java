package io.symeo.monolithic.backend.domain.bff.service.service;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.bff.port.in.JobFacadeAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffJobStorage;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class JobService implements JobFacadeAdapter {
    private final BffJobStorage jobStorage;

    @Override
    public List<JobView> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String jobCode,
                                                                               Organization organization) throws SymeoException {
        return jobStorage.findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(jobCode, organization);
    }

    @Override
    public List<JobView> findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(UUID organizationId, UUID teamId) throws SymeoException {
        return jobStorage.findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(organizationId, teamId);
    }
}
