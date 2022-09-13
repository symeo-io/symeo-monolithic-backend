package io.symeo.monolithic.backend.domain.port.out;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;

import java.util.List;
import java.util.UUID;

public interface JobStorage {
    Job createJob(Job job) throws SymeoException;

    Job updateJob(Job job) throws SymeoException;

    List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException;

    List<Job> findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(String jobCode,
                                                                              Organization organization, int i) throws SymeoException;

    List<Job> findLastFailedJobsForOrganizationIdAndTeamIdForEachJobCode(UUID organizationId, UUID teamId);
}
