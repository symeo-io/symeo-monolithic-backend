package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.Job;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.frontend.contract.api.model.JobContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsResponseContract;

import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.exceptionToContract;
import static java.util.Objects.isNull;

public interface JobContractMapper {

    static LastJobsResponseContract domainToContract(final List<Job> jobs, final Organization organization) {
        final LastJobsResponseContract lastJobsResponseContract = new LastJobsResponseContract();
        lastJobsResponseContract.setJobs(
                jobsToContract(jobs, organization)
        );
        return lastJobsResponseContract;
    }

    static LastJobsResponseContract errorToContract(final SymeoException symeoException) {
        final LastJobsResponseContract lastJobsResponseContract = new LastJobsResponseContract();
        lastJobsResponseContract.setErrors(List.of(exceptionToContract(symeoException)));
        return lastJobsResponseContract;
    }

    private static LastJobsContract jobsToContract(final List<Job> jobs, final Organization organization) {
        final LastJobsContract lastJobsContract = new LastJobsContract();
        lastJobsContract.setCurrentJob(jobToContract(jobs.get(0), organization));
        if (jobs.size() == 2) {
            lastJobsContract.setPreviousJob(jobToContract(jobs.get(1), organization));
        }
        return lastJobsContract;
    }

    private static JobContract jobToContract(final Job job, final Organization organization) {
        final JobContract jobContract = new JobContract();
        jobContract.setCode(job.getCode());
        jobContract.setStatus(job.getStatus());
        jobContract.setId(job.getId());
        jobContract.setCreationDate(job.getCreationDate().toInstant().atZone(organization.getTimeZone().toZoneId()).toLocalDate());
        jobContract.setEndDate(isNull(job.getEndDate()) ? null :
                job.getEndDate().toInstant().atZone(organization.getTimeZone().toZoneId()).toLocalDate());
        return jobContract;
    }
}
