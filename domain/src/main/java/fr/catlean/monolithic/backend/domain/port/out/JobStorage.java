package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;
import fr.catlean.monolithic.backend.domain.model.account.Organization;

import java.util.List;

public interface JobStorage {
    Job createJob(Job job) throws CatleanException;

    Job updateJob(Job job) throws CatleanException;

    List<Job> findAllJobsByCodeAndOrganizationOrderByUpdateDateDesc(String code, Organization organization) throws CatleanException;

    List<Job> findLastJobsForCodeAndOrganizationAndLimitOrderByUpdateDateDesc(String jobCode, Organization organization, int i) throws CatleanException;
}
