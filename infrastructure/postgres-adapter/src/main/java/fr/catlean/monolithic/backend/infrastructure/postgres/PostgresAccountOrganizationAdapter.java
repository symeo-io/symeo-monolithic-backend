package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.model.account.Team;
import fr.catlean.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.TeamEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.TeamRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper.domainToEntity;
import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.TeamMapper.entityToDomain;

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
                                .code("F.ORGANIZATION_NAME_NOT_FOUND")
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
                    .code("T.POSTGRES_EXCEPTION")
                    .message("Failed to create organization " + organization.getName())
                    .build();
        }
    }

}
