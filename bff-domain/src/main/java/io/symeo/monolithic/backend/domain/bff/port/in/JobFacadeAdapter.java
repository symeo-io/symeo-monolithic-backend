package io.symeo.monolithic.backend.domain.bff.port.in;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.job.JobView;

import java.util.List;

public interface JobFacadeAdapter {
    List<JobView> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String jobCode, Organization organization);
}
