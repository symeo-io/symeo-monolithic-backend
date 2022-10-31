package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface JobFacadeAdapter {
    List<JobView> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String jobCode, Organization organization) throws SymeoException;

    List<JobView> findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(UUID id, UUID teamId) throws SymeoException;
}
