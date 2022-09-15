package io.symeo.monolithic.backend.domain.job.runnable;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.job.JobRunnable;
import io.symeo.monolithic.backend.domain.job.Task;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.service.OrganizationSettingsService;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class InitializeOrganizationSettingsJobRunnable extends AbstractTasksRunnable<Organization> implements JobRunnable {

    public static final String JOB_CODE = "INITIALIZE_ORGANIZATION_SETTINGS_JOB";
    @NonNull OrganizationSettingsService organizationSettingsService;
    @NonNull AccountOrganizationStorageAdapter accountOrganizationStorageAdapter;
    @NonNull UUID organizationId;

    @Override
    public void initializeTasks() throws SymeoException {
        final Organization organization =
                accountOrganizationStorageAdapter.findOrganizationById(organizationId);
        this.tasks = new ArrayList<>(List.of(
                Task.newTaskForInput(organization)
        ));
    }

    @Override
    public void run() throws SymeoException {
        executeAllTasks(this::initializeOrganizationSettingsForOrganization);
    }

    private void initializeOrganizationSettingsForOrganization(Organization organization) throws SymeoException {
        organizationSettingsService.initializeOrganizationSettingsForOrganization(organization);
    }

    @Override
    public String getCode() {
        return JOB_CODE;
    }

}
