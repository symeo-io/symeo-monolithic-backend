package fr.catlean.monolithic.backend.domain.port.in;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.model.account.Organization;

import java.util.List;

public interface JobFacadeAdapter {

    List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws CatleanException;

    List<Job> findLastJobsForCodeAndOrganizationAndLimit(String jobCode, Organization organization, int i) throws CatleanException;
}
