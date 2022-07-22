package fr.catlean.monolithic.backend.domain.port.out;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.job.Job;

public interface JobStorage {
    Job createJob(Job job) throws CatleanException;

    Job updateJob(Job job) throws CatleanException;
}
