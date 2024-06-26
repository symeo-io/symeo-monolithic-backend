package io.symeo.monolithic.backend.domain.bff.service.organization;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.model.vcs.RepositoryView;
import io.symeo.monolithic.backend.domain.bff.port.in.OrganizationSettingsFacade;
import io.symeo.monolithic.backend.domain.bff.port.out.BffExpositionStorageAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.BffSymeoDataProcessingJobApiAdapter;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class OrganizationSettingsService implements OrganizationSettingsFacade {

    private final BffExpositionStorageAdapter bffExpositionStorageAdapter;
    private final OrganizationStorageAdapter organizationStorageAdapter;
    private final BffSymeoDataProcessingJobApiAdapter bffSymeoDataProcessingJobApiAdapter;

    public void initializeOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        final Optional<OrganizationSettings> optionalOrganizationSettings =
                organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId());
        if (optionalOrganizationSettings.isEmpty()) {
            final OrganizationSettings organizationSettings =
                    OrganizationSettings.initializeFromOrganizationId(organization.getId());
            organizationStorageAdapter.saveOrganizationSettings(organizationSettings);
        }
    }

    @Override
    public OrganizationSettings getOrganizationSettingsForOrganization(final Organization organization) throws SymeoException {
        return organizationStorageAdapter.findOrganizationSettingsForOrganizationId(organization.getId())
                .orElseThrow(() ->
                        SymeoException.builder()
                                .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                                .message(String.format("OrganizationSettings not found for organizationId %s",
                                        organization.getId()))
                                .build()
                );
    }

    @Override
    public void updateOrganizationSettings(final Organization organization,
                                           final OrganizationSettings organizationSettings) throws SymeoException {
        final Optional<OrganizationSettings> organizationSettingsToUpdate =
                getOrganizationSettingsForIdAndOrganizationId(organizationSettings.getId(),
                        organizationSettings.getOrganizationId());
        if (organizationSettingsToUpdate.isPresent()) {
            organizationStorageAdapter.saveOrganizationSettings(organizationSettings);
            final List<String> repositoryIds =
                    bffExpositionStorageAdapter.readRepositoriesForOrganization(organization)
                            .stream()
                            .map(RepositoryView::getId)
                            .toList();
            bffSymeoDataProcessingJobApiAdapter.startUpdateCycleTimesDataProcessingJobForOrganizationIdAndRepositoryIdsAndOrganizationSettings(
                    repositoryIds,
                    organizationSettings
            );
        } else {
            throw SymeoException.builder()
                    .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                    .message(String.format("OrganizationSettings not found for organizationSettingsId %s or user not " +
                                    "allowed to modify organizationSettings",
                            organizationSettings.getId()))
                    .build();
        }
    }

    @Override
    public Optional<OrganizationSettings> getOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId,
                                                                                        UUID organizationId) throws SymeoException {
        return organizationStorageAdapter.findOrganizationSettingsForIdAndOrganizationId(organizationSettingsId,
                organizationId);
    }
}
