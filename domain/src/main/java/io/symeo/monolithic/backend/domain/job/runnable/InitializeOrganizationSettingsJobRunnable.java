package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.service.OrganizationSettingsService;
import lombok.Builder;

import java.util.List;

@Builder
public class InitializeOrganizationSettingsJobRunnable implements JobRunnable {

    public static final String JOB_CODE = "INITIALIZE_ORGANIZATION_SETTINGS_JOB";
    private final OrganizationSettingsService organizationSettingsService;
    private final Organization organization;

    @Override
    public void run() throws SymeoException {
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

    @Override
    public List<Task> getTasks() {
        return null;
    }

    @Override
    public void setTasks(List<Task> tasks) {

    }
}
