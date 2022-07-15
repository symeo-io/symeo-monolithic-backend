package fr.catlean.monolithic.backend.infrastructure.postgres;

import fr.catlean.monolithic.backend.domain.exception.CatleanException;
import fr.catlean.monolithic.backend.domain.model.account.Organization;
import fr.catlean.monolithic.backend.domain.port.out.OrganizationStorageAdapter;
import fr.catlean.monolithic.backend.infrastructure.postgres.entity.account.OrganizationEntity;
import fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import fr.catlean.monolithic.backend.infrastructure.postgres.repository.account.OrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static fr.catlean.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.domainToEntity;
import static java.util.UUID.randomUUID;

@AllArgsConstructor
@Slf4j
public class PostgresOrganizationAdapter implements OrganizationStorageAdapter {

    private final OrganizationRepository organizationRepository;

    @Override
    public Organization findOrganizationForName(String organizationName) throws CatleanException {
        return null;
    }

    @Override
    public Organization createOrganization(Organization organization) throws CatleanException {
        final OrganizationEntity organizationEntity = domainToEntity(organization);
        organizationEntity.setId(randomUUID().toString());
        try {
            return OrganizationMapper.entityToDomain(organizationRepository.save(organizationEntity));
        } catch (Exception e) {
            LOGGER.error("Failed to create organization {}", organization);
            throw CatleanException.builder()
                    .code("T.POSTGRES_EXCEPTION")
                    .message("Failed to create organization " + organization.getName())
                    .build();
        }
    }
}
