package io.symeo.monolithic.backend.domain.port.in;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;

import java.util.List;
import java.util.UUID;

public interface JobFacadeAdapter {

    List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException;

    List<Job> findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(UUID organizationId, UUID teamId) throws SymeoException;
}
