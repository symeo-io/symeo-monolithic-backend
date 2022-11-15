package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.account.OrganizationSettingsEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import io.symeo.monolithic.backend.job.domain.model.organization.OrganizationSettingsView;
import io.symeo.monolithic.backend.job.domain.model.vcs.VcsOrganization;
import io.symeo.monolithic.backend.job.domain.port.out.VcsOrganizationStorageAdapter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.*;

@AllArgsConstructor
@Slf4j
public class PostgresOrganizationAdapter implements OrganizationStorageAdapter, VcsOrganizationStorageAdapter {

    private final VcsOrganizationRepository vcsOrganizationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSettingsRepository organizationSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findOrganizationById(final UUID organizationId) throws SymeoException {
        return organizationRepository.findById(organizationId)
                .map(OrganizationMapper::entityToDomain);

    }

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        try {
            final VcsOrganizationEntity vcsOrganizationEntity = vcsDomainToEntity(organization);
            return entityToDomain(vcsOrganizationRepository.save(vcsOrganizationEntity));
        } catch (Exception e) {
            LOGGER.error("Failed to create organization {}", organization, e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create organization " + organization.getName())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VcsOrganization> findAllVcsOrganization() throws SymeoException {
        try {
            return vcsOrganizationRepository.findAll().stream()
                    .map(OrganizationMapper::vcsEntityToDataProcessingDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to find all vcsOrganizations", e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find all vcsOrganizations")
                    .build();
        }
    }

    @Override
    public void saveOrganizationSettings(OrganizationSettings organizationSettings) throws SymeoException {
        try {
            organizationSettingsRepository.save(settingsToEntity(organizationSettings));
        } catch (Exception e) {
            LOGGER.error("Failed to save organizationSettings", e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to save organizationSettings")
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrganizationSettings> findOrganizationSettingsForOrganizationId(UUID organizationId) throws SymeoException {

        Optional<OrganizationSettingsEntity> optionalOrganizationSettings =
                organizationSettingsRepository.findByOrganizationId(organizationId);
        if (optionalOrganizationSettings.isPresent()) {
            return Optional.of(OrganizationMapper.settingsToDomain(optionalOrganizationSettings.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<OrganizationSettings> findOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId,
                                                                                         UUID organizationId) throws SymeoException {
        Optional<OrganizationSettingsEntity> optionalOrganizationSettings =
                organizationSettingsRepository.findByIdAndOrganizationId(organizationSettingsId, organizationId);
        if (optionalOrganizationSettings.isPresent()) {
            return Optional.of(OrganizationMapper.settingsToDomain(optionalOrganizationSettings.get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public OrganizationSettingsView findOrganizationSettingsViewForOrganizationId(UUID organizationId) throws SymeoException {
        return organizationSettingsRepository.findByOrganizationId(organizationId)
                .map(OrganizationMapper::settingsToView)
                .orElseThrow(
                        () -> SymeoException.builder()
                                .code(SymeoExceptionCode.ORGANIZATION_SETTINGS_NOT_FOUND)
                                .message(String.format("OrganizationSettings not found for organizationId %s",
                                        organizationId))
                                .build()
                );

    }
}
