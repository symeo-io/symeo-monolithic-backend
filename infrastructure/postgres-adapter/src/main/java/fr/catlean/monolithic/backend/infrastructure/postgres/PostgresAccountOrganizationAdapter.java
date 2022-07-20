package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.ORGANIZATION_NAME_NOT_FOUND;
import static fr.catlean.monolithic.backend.domain.exception.CatleanExceptionCode.POSTGRES_EXCEPTION;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.domainToEntity;

@AllArgsConstructor
@Slf4j
public class PostgresAccountOrganizationAdapter implements AccountOrganizationStorageAdapter {

    private final OrganizationRepository organizationRepository;

    @Override
    public Organization findOrganizationForName(String organizationName) throws CatleanException {
        return organizationRepository.findByName(organizationName)
                .map(OrganizationMapper::entityToDomain)
                .orElseThrow(
                        () -> CatleanException.builder()
                                .message(String.format("Organization not found for name %s", organizationName))
                                .code(ORGANIZATION_NAME_NOT_FOUND)
                                .build()
                );

    }

    @Override
    public Organization createOrganization(Organization organization) throws CatleanException {
        try {
            final OrganizationEntity organizationEntity = domainToEntity(organization);
            return OrganizationMapper.entityToDomain(organizationRepository.save(organizationEntity));
        } catch (Exception e) {
            LOGGER.error("Failed to create organization {}", organization, e);
            throw CatleanException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create organization " + organization.getName())
                    .build();
        }
    }

}
