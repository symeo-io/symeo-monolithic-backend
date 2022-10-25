package io.symeo.monolithic.backend.application.rest.api.adapter.mapper;

import io.symeo.monolithic.backend.domain.bff.model.job.JobView;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.frontend.contract.api.model.JobContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsContract;
import io.symeo.monolithic.backend.frontend.contract.api.model.LastJobsResponseContract;

import java.math.BigDecimal;
import java.util.List;

import static io.symeo.monolithic.backend.application.rest.api.adapter.mapper.SymeoErrorContractMapper.exceptionToContract;
import static io.symeo.monolithic.backend.domain.helper.DateHelper.dateTimeToString;
import static java.util.Objects.isNull;

public interface JobContractMapper {

    static LastJobsResponseContract domainToContract(final List<JobView> jobs) {
        final LastJobsResponseContract lastJobsResponseContract = new LastJobsResponseContract();
        lastJobsResponseContract.setJobs(
                jobsToContract(jobs)
        );
        return lastJobsResponseContract;
    }

    static LastJobsResponseContract errorToContract(final SymeoException symeoException) {
        final LastJobsResponseContract lastJobsResponseContract = new LastJobsResponseContract();
        lastJobsResponseContract.setErrors(List.of(exceptionToContract(symeoException)));
        return lastJobsResponseContract;
    }

    private static LastJobsContract jobsToContract(final List<JobView> jobs) {
        final LastJobsContract lastJobsContract = new LastJobsContract();
        if (jobs.size() == 0) {
            return lastJobsContract;
        }
        lastJobsContract.setCurrentJob(jobToContract(jobs.get(0)));
        if (jobs.size() == 2) {
            lastJobsContract.setPreviousJob(jobToContract(jobs.get(1)));
        }
        return lastJobsContract;
    }

    private static JobContract jobToContract(final JobView job) {
        final JobContract jobContract = new JobContract();
        jobContract.setCode(job.getCode());
        jobContract.setStatus(job.getStatus());
        jobContract.setId(job.getId());
        jobContract.setCreationDate(dateTimeToString(job.getCreationDate()));
        jobContract.setEndDate(isNull(job.getEndDate()) ? null :
                dateTimeToString(job.getEndDate()));
        jobContract.setProgressionPercentage(BigDecimal.valueOf(job.getProgressionPercentage()));
        return jobContract;
    }
}
