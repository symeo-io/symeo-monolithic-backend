package io.symeo.monolithic.backend.domain.bff.port.out;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;

import java.util.List;
import java.util.UUID;

public interface BffJobStorage {

    List<JobView> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws SymeoException;

    List<JobView> findLastJobsForCodeAndOrganizationIdAndLimitAndTeamIdOrderByUpdateDateDesc(String jobCode,
                                                                                             UUID organizationId,
                                                                                             UUID teamId, int i) throws SymeoException;

    List<JobView> findLastTwoJobsInProgressOrFinishedForVcsDataCollectionJob(UUID organizationId, UUID teamId) throws SymeoException;
}
