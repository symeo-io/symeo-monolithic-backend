package io.symeo.monolithic.backend.infrastructure.postgres.adapter;

import io.symeo.monolithic.backend.domain.bff.model.account.Organization;
import io.symeo.monolithic.backend.domain.bff.model.account.settings.OrganizationSettings;
import io.symeo.monolithic.backend.domain.bff.port.out.OrganizationStorageAdapter;
import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.account.OrganizationSettingsRepository;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.ORGANIZATION_NAME_NOT_FOUND;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.*;

@AllArgsConstructor
@Slf4j
public class PostgresOrganizationAdapter implements OrganizationStorageAdapter {

    private final VcsOrganizationRepository vcsOrganizationRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSettingsRepository organizationSettingsRepository;

    @Override
    @Transactional(readOnly = true)
    public Organization findOrganizationById(final UUID organizationId) throws SymeoException {
        return vcsOrganizationRepository.findByOrganizationId(organizationId)
                .map(OrganizationMapper::entityToDomain)
                .orElseThrow(
                        () -> SymeoException.builder()
                                .message(String.format("Organization not found for id %s", organizationId))
                                .code(ORGANIZATION_NAME_NOT_FOUND)
                                .build()
                );

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
    public List<Organization> findAllOrganization() throws SymeoException {
        try {
            return organizationRepository.findAll().stream()
                    .map(OrganizationMapper::entityToDomain)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.error("Failed to find all organizations", e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create organizations")
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
        try {
            return organizationSettingsRepository.findByOrganizationId(organizationId).map(OrganizationMapper::settingsToDomain);
        } catch (Exception e) {
            LOGGER.error("Failed to find organizationSettings", e);
            throw SymeoException.builder()
                    .rootException(e)
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to find organizationSettings")
                    .build();
        }
    }

    @Override
    public Optional<OrganizationSettings> findOrganizationSettingsForIdAndOrganizationId(UUID organizationSettingsId,
                                                                                         UUID organizationId) {
        return organizationSettingsRepository.findByIdAndOrganizationId(organizationSettingsId, organizationId).map(OrganizationMapper::settingsToDomain);
    }
}
