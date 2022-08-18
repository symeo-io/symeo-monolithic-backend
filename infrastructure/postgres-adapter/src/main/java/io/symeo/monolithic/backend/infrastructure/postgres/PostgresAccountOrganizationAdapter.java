package io.symeo.monolithic.backend.infrastructure.postgres;

import io.symeo.monolithic.backend.domain.exception.SymeoException;
import io.symeo.monolithic.backend.domain.model.account.Organization;
import io.symeo.monolithic.backend.domain.port.out.AccountOrganizationStorageAdapter;
import io.symeo.monolithic.backend.infrastructure.postgres.entity.exposition.VcsOrganizationEntity;
import io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper;
import io.symeo.monolithic.backend.infrastructure.postgres.repository.exposition.VcsOrganizationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.ORGANIZATION_NAME_NOT_FOUND;
import static io.symeo.monolithic.backend.domain.exception.SymeoExceptionCode.POSTGRES_EXCEPTION;
import static io.symeo.monolithic.backend.infrastructure.postgres.mapper.account.OrganizationMapper.entityToDomain;

@AllArgsConstructor
@Slf4j
public class PostgresAccountOrganizationAdapter implements AccountOrganizationStorageAdapter {

    private final VcsOrganizationRepository vcsOrganizationRepository;

    @Override
    @Transactional(readOnly = true)
    public Organization findVcsOrganizationForName(String organizationName) throws SymeoException {

        return vcsOrganizationRepository.findByName(organizationName)
                .map(OrganizationMapper::entityToDomain)
                .orElseThrow(
                        () -> SymeoException.builder()
                                .message(String.format("Vcs Organization not found for name %s", organizationName))
                                .code(ORGANIZATION_NAME_NOT_FOUND)
                                .build()
                );

    }

    @Override
    public Organization createOrganization(Organization organization) throws SymeoException {
        try {
            final VcsOrganizationEntity vcsOrganizationEntity = OrganizationMapper.vcsDomainToEntity(organization);
            return entityToDomain(vcsOrganizationRepository.save(vcsOrganizationEntity));
        } catch (Exception e) {
            LOGGER.error("Failed to create organization {}", organization, e);
            throw SymeoException.builder()
                    .code(POSTGRES_EXCEPTION)
                    .message("Failed to create organization " + organization.getName())
                    .build();
        }
    }

}
